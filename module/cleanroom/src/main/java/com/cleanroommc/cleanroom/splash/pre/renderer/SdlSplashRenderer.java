/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre.renderer;

import com.cleanroommc.cleanroom.splash.pre.PreSplashState;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.ui.font.McTtfFontData;
import com.cleanroommc.kirino.ui.simplegui.SimpleGuiRuntime;
import com.cleanroommc.kirino.ui.simpletext.ST_Config;
import com.cleanroommc.kirino.ui.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DArrayGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextProducer;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextRenderer;
import com.cleanroommc.kirino.ui.simpletext.backend.FreeTypeFontHandle;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;
import com.cleanroommc.mcttf.glyph.GlyphBuilder;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourceIndexFolder;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.freetype.FT_Face;
import org.lwjgl.util.freetype.FreeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.cleanroommc.cleanroom.splash.pre.renderer.PreSplashLogger.LOGGER;

public final class SdlSplashRenderer implements AutoCloseable {

    /**
     * FreeType & GL agnostic async font loader.
     */
    private static final class FontLoader implements AutoCloseable {

        private static final int LOAD_TIMEOUT_SECOND = 30;
        private static final int THREAD_STOP_TIMEOUT_SECOND = 2;

        private final Thread thread;
        private final long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(LOAD_TIMEOUT_SECOND);
        private byte @Nullable [] data;
        private boolean finished;
        private boolean closed;

        private FontLoader(File assetsDirectory, @Nullable String assetIndex) {
            thread = new Thread(() -> load(assetsDirectory, assetIndex), "Pre Splash Font Loader");
            thread.setDaemon(true);
        }

        private synchronized boolean isFinished() {
            if (!finished && deadline - System.nanoTime() <= 0) {
                LOGGER.warn("Pre splash Minecraft font loading timed out after {} seconds.", LOAD_TIMEOUT_SECOND);
                cancel();
            }

            return finished;
        }

        @SuppressWarnings("DataFlowIssue")
        private synchronized byte @Nullable [] take() {
            byte[] data = this.data;
            this.data = null;
            return data;
        }

        @SuppressWarnings("DataFlowIssue")
        private void load(File assetsDirectory, @Nullable String assetIndex) {
            StopWatch stopWatch = StopWatch.createStarted();
            byte[] data = null;

            try {
                synchronized (this) {
                    if (closed || isFinished()) {
                        return;
                    }
                }

                ResourceIndex index = assetIndex == null
                        ? new ResourceIndexFolder(assetsDirectory)
                        : new ResourceIndex(assetsDirectory, assetIndex);

                synchronized (this) {
                    if (closed || isFinished()) {
                        return;
                    }
                }

                data = McTtfFontData.loadDefault(new DefaultResourcePack(index), "pre_splash");
            } catch (Throwable failure) {
                LOGGER.warn("Could not prepare the pre splash Minecraft font.", failure);
            } finally {
                stopWatch.stop();
                synchronized (this) {
                    if (!closed && !isFinished()) {
                        this.data = data;
                        if (data != null) {
                            LOGGER.info("Pre splash Minecraft font loaded in {} ms.", stopWatch.getTime(TimeUnit.MILLISECONDS));
                        }
                    }
                    finished = true;
                }
            }
        }

        private void cancel() {
            synchronized (this) {
                closed = true;
                finished = true;
                data = null;
            }

            thread.interrupt();
        }

        @Override
        public void close() {
            cancel();
            if (Thread.currentThread() == thread) {
                return;
            }

            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(THREAD_STOP_TIMEOUT_SECOND);
            boolean interrupted = false;
            while (thread.isAlive()) {
                long remaining = deadline - System.nanoTime();
                if (remaining <= 0) {
                    LOGGER.warn("Pre splash font loader did not stop within {} seconds.", THREAD_STOP_TIMEOUT_SECOND);
                    break;
                }
                try {
                    TimeUnit.NANOSECONDS.timedJoin(thread, remaining);
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private static final ResourceLocation FALLBACK_FONT = new ResourceLocation("kirino:fonts/jetbrains/jetbrains_mono_nl_regular.ttf");
    private static final long ENTRY_ANIMATION_MILLIS = 1000;

    private final StopWatch entryAnimationStopWatch = new StopWatch();

    private final GLResourceManager.ResourceScope scope;
    private final FontLoader fontLoader;
    private final ImmediateShaderAccess shaders;
    private final VAO dummyVao;
    private final Runnable startupReady;
    private final @Nullable SimpleGuiRuntime gui;
    private final @Nullable FreeTypeManager freeType;
    private @Nullable ByteBuffer mcFontData;
    private @Nullable SimpleTextRuntime text;
    private @Nullable PreSplashState state;
    private boolean fontResolved;
    private boolean startupSignaled;

    public SdlSplashRenderer(File assetsDirectory, @Nullable String assetIndex, Runnable startupReady) throws Exception {
        LOGGER.info("Pre Splash Screen Backend: SDL + GL46");

        Preconditions.checkNotNull(assetsDirectory);
        Preconditions.checkNotNull(startupReady);

        this.startupReady = startupReady;

        fontLoader = new FontLoader(assetsDirectory, assetIndex);
        scope = GLResourceManager.openScope();

        try {
            fontLoader.thread.start();

            shaders = new ImmediateShaderAccess(rl -> {
                String path = "/assets/" + rl.getNamespace() + "/" + rl.getPath();
                try (InputStream input = SdlSplashRenderer.class.getResourceAsStream(path)) {
                    Preconditions.checkNotNull(input, "Missing pre splash shader: %s", path);

                    return new String(input.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not load pre splash shader " + path, e);
                }
            });

            AttributeLayout layout = new AttributeLayout();
            layout.push(new Stride(0));
            dummyVao = new VAO(layout, null, (VBOView @Nullable []) null);

            float uiWidth = PreSplashLayout.WIDTH / PreSplashLayout.UI_SCALE;
            float uiHeight = PreSplashLayout.HEIGHT / PreSplashLayout.UI_SCALE;
            gui = new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight);

            var constructor = FreeTypeManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FreeTypeManager freeType = constructor.newInstance();
            freeType.loggerName = "pre_splash";
            freeType.init();
            this.freeType = freeType;
            text = createText(freeType.load(FALLBACK_FONT, 0, 48), false);

            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        } catch (Throwable failure) {
            try {
                close();
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private void resolveFont() {
        if (fontResolved || !fontLoader.isFinished()) {
            return;
        }

        fontResolved = true;
        byte[] bytes = fontLoader.take();
        if (bytes == null) {
            LOGGER.warn("Could not load the pre splash Minecraft font.");
            return;
        }

        Preconditions.checkNotNull(freeType);

        ByteBuffer data = null;
        SimpleTextRuntime replacement;
        try {
            data = MemoryUtil.memAlloc(bytes.length);
            data.put(bytes).flip();
            replacement = createText(freeType.load(data, 0, 48), true);
        } catch (Throwable failure) {
            if (data != null) {
                try {
                    freeType.unload(data);
                } catch (Throwable cleanup) {
                    failure.addSuppressed(cleanup);
                } finally {
                    MemoryUtil.memFree(data);
                }
            }
            LOGGER.warn("Could not initialize pre splash Minecraft text.", failure);
            return;
        }

        SimpleTextRuntime previous = text;
        text = replacement;
        mcFontData = data;

        try {
            if (previous != null) {
                previous.close();
            }
        } catch (Throwable failure) {
            LOGGER.warn("Could not dispose the previous pre splash text runtime.", failure);
        } finally {
            freeType.unload(FALLBACK_FONT);
        }
    }

    private SimpleTextRuntime createText(FT_Face face, boolean minecraftFont) {
        ST_Config config = new ST_Config(
                ST_FontBackendType.FREE_TYPE,
                48,
                16,
                12,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);

        float uiWidth = PreSplashLayout.WIDTH / PreSplashLayout.UI_SCALE;
        float uiHeight = PreSplashLayout.HEIGHT / PreSplashLayout.UI_SCALE;
        SimpleGuiRuntime underline = null;
        SimpleGuiRuntime strikethrough = null;

        try {
            underline = new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight);
            strikethrough = new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight);
            return new SimpleTextRuntime(
                    (_, _) -> new FreeTypeFontHandle(face),
                    runtime -> {
                        DefaultTextRenderer renderer = new DefaultTextRenderer(runtime, new Tex2DArrayGlyphAtlas(1024, 1024), shaders, 256, uiWidth, uiHeight);
                        renderer.loggerName = "pre_splash";
                        return renderer;
                    },
                    runtime -> minecraftFont
                            ? new DefaultTextProducer(runtime, config.pixelSize(), GlyphBuilder.LINE_HEIGHT_PIXELS)
                            : new DefaultTextProducer(runtime, config.pixelSize()),
                    shaders,
                    underline,
                    strikethrough,
                    config,
                    null);
        } catch (Throwable failure) {
            try {
                if (strikethrough != null) {
                    strikethrough.close();
                }
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            try {
                if (underline != null) {
                    underline.close();
                }
            } catch (Throwable cleanup) {
                failure.addSuppressed(cleanup);
            }
            throw failure;
        }
    }

    public void update(PreSplashState state) {
        if (this.state == state) {
            return;
        }

        this.state = state;
    }

    public void render(int width, int height) {
        if (state == null) {
            return;
        }

        float scale = Math.min((float) width / PreSplashLayout.WIDTH, (float) height / PreSplashLayout.HEIGHT);
        int viewportWidth = Math.round(PreSplashLayout.WIDTH * scale);
        int viewportHeight = Math.round(PreSplashLayout.HEIGHT * scale);
        GL11.glViewport((width - viewportWidth) / 2, (height - viewportHeight) / 2, viewportWidth, viewportHeight);

        int background = PreSplashLayout.BACKGROUND;
        GL11.glClearColor(((background >> 16) & 255) / 255f, ((background >> 8) & 255) / 255f, (background & 255) / 255f, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        if (entryAnimationStopWatch.isStarted() && entryAnimationStopWatch.getTime(TimeUnit.MILLISECONDS) >= ENTRY_ANIMATION_MILLIS) {
            resolveFont();
        }

        SimpleTextRuntime text = Preconditions.checkNotNull(this.text);
        text.begin();
        text.append(state.title(), 48, 106, 32, 0xFFF1F4FA);
        text.append(state.message(), 48, 190, 16, 0xFFB9C7DB);
        text.append(state.details(), 48, 286, 16, 0xFF8998AE);
        text.endDraw();

        if (!entryAnimationStopWatch.isStarted()) {
            entryAnimationStopWatch.start();
        }

        if (!fontResolved) {
            double seconds = entryAnimationStopWatch.getTime(TimeUnit.MILLISECONDS) / 1000.0;
            float x = 48 + (float) (0.5 - Math.cos(seconds * Math.PI) * 0.5) * 208;
            SimpleGuiRuntime gui = Preconditions.checkNotNull(this.gui);
            gui.begin().append(stream -> stream.rect(x, 232, 40, 6, 0xFF8998AE)).endDraw();
        }

        if (fontResolved && !startupSignaled) {
            startupSignaled = true;
            startupReady.run();
        }
    }

    @Override
    public void close() throws Exception {
        fontLoader.close();
        try {
            if (text != null) {
                text.close();
            }
        } finally {
            try {
                if (gui != null) {
                    gui.close();
                }
            } finally {
                try {
                    if (freeType != null) {
                        freeType.destroy();
                    }
                } finally {
                    try {
                        if (mcFontData != null) {
                            MemoryUtil.memFree(mcFontData);
                        }
                    } finally {
                        scope.close();
                    }
                }
            }
        }
    }
}

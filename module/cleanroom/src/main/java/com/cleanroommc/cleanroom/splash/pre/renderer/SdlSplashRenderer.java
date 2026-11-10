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
import com.cleanroommc.kirino.ui.simplegui.SimpleGuiRuntime;
import com.cleanroommc.kirino.ui.simpletext.ST_Config;
import com.cleanroommc.kirino.ui.simpletext.ST_FontBackendType;
import com.cleanroommc.kirino.ui.simpletext.SimpleTextRuntime;
import com.cleanroommc.kirino.ui.simpletext.atlas.Tex2DArrayGlyphAtlas;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextProducer;
import com.cleanroommc.kirino.ui.simpletext.backend.DefaultTextRenderer;
import com.cleanroommc.kirino.ui.simpletext.backend.FreeTypeFontHandle;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;
import com.cleanroommc.kirino.ui.simpletext.text.StyledText;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.cleanroommc.kirino.utils.ResourceLocation;
import com.cleanroommc.mcttf.glyph.GlyphBuilder;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.time.StopWatch;
import org.jspecify.annotations.Nullable;

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

    private static final ResourceLocation FALLBACK_FONT = new ResourceLocation("kirino:fonts/jetbrains/jetbrains_mono_nl_regular.ttf");
    private static final String ENGLISH_CHARACTERS = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    private static final long EARLY_TITLE_INOUT_ANIM_MILLIS = 200;
    private static final long EARLY_TITLE_HOLD_MILLIS = 650;
    private static final long INTRO_FADE_OUT_ANIM_MILLIS = 200;
    private static final long POST_INTRO_MASK_FADE_OUT_MILLIS = 150;
    private static final long MAX_TRANSITION_FRAME_MILLIS = 33;
    private static final long LOADING_BAR_CYCLE_MILLIS = 1400;
    private static final long LOADING_BAR_FADE_OUT_MILLIS = 200;
    static final long LOGO_ANIM_DURATION_MILLIS = 1200;

    private final Runnable startupReady;

    private final GLResourceManager.ResourceScope scope;
    private final AsyncFontLoader fontLoader;
    private final ImmediateShaderAccess shaders;
    private final VAO dummyVao;
    private final @Nullable SimpleGuiRuntime gui;
    private final @Nullable LogoAnimationDrawer logoAnimation;
    private final @Nullable FreeTypeManager freeType;

    private final StopWatch mainStopWatch = new StopWatch();
    private final StopWatch earlyTitleStopWatch = new StopWatch();
    private final StopWatch titleHoldStopWatch = new StopWatch();
    private final StopWatch loadingBarFadeOutStopWatch = new StopWatch();
    private final StopWatch introFadeOutStopWatch = new StopWatch();
    private final StopWatch postIntroMaskStopWatch = new StopWatch();
    private long introFadeOutFrameMillis;
    private long introFadeOutElapsedMillis;
    private long postIntroMaskFrameMillis;
    private long postIntroMaskElapsedMillis;

    private @Nullable ByteBuffer mcFontData;
    private @Nullable PreSplashState state;
    private @Nullable SimpleTextRuntime text;
    private @Nullable SimpleTextRuntime introText;

    private enum IntroStage {
        LOGO,
        TITLE_WARMUP,
        TITLE_FADE_IN,
        TITLE_HOLD,
        FONT_LOADING,
        BAR_FADE_OUT,
        ENGLISH_WARMUP,
        FADE_OUT,
        FINISHED
    }

    // states
    private boolean fontResolved;
    private boolean fontResolveFailed = true;
    private boolean earlyTitleFallback;
    private boolean fontLoadingAtEntry;
    private boolean englishWarmedUp;
    private IntroStage introStage = IntroStage.LOGO;

    private boolean startupSignaled;

    public SdlSplashRenderer(File assetsDirectory, @Nullable String assetIndex, Runnable startupReady) throws Exception {
        LOGGER.info("Pre Splash Screen Backend: SDL + GL46");

        Preconditions.checkNotNull(assetsDirectory);
        Preconditions.checkNotNull(startupReady);

        this.startupReady = startupReady;

        fontLoader = new AsyncFontLoader(assetsDirectory, assetIndex);
        scope = GLResourceManager.openScope();

        try {
            fontLoader.thread.start();

            shaders = ImmediateShaderAccess.fromSourceLoader(rl -> {
                try (InputStream input = MinecraftResourceUtils.getEarlyInputStream(rl)) {
                    return new String(input.readAllBytes(), StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("Could not load pre splash shader " + rl, e);
                }
            });

            AttributeLayout layout = new AttributeLayout();
            layout.push(new Stride(0));
            dummyVao = new VAO(layout, null, (VBOView @Nullable []) null);

            gui = new SimpleGuiRuntime(shaders, dummyVao, PreSplashLayout.WIDTH, PreSplashLayout.HEIGHT);

            logoAnimation = new LogoAnimationDrawer(shaders, dummyVao);

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

    /**
     * Loads Minecraft font into {@link #text}.
     *
     * @return Whether Minecraft font is ready
     */
    @SuppressWarnings("DataFlowIssue")
    private boolean resolveFont() {
        if (fontResolved) {
            return !fontResolveFailed;
        }
        if (!fontLoader.isFinished()) {
            return false;
        }

        fontResolved = true;
        byte[] bytes = fontLoader.take();
        if (bytes == null) {
            LOGGER.warn("Could not load the pre splash Minecraft font.");
            return false;
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
            return false;
        }

        SimpleTextRuntime previous = Preconditions.checkNotNull(text);
        text = replacement;
        mcFontData = data;
        fontResolveFailed = false;

        if (previous != introText) {
            try {
                previous.close();
            } catch (Throwable failure) {
                LOGGER.warn("Could not dispose the previous pre splash text runtime.", failure);
            } finally {
                freeType.unload(FALLBACK_FONT);
            }
        }

        return true;
    }

    private SimpleTextRuntime createText(FT_Face face, boolean minecraftFont) {
        ST_Config config = new ST_Config(
                ST_FontBackendType.FREE_TYPE,
                48,
                16,
                12,
                FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);

        SimpleGuiRuntime underline = null;
        SimpleGuiRuntime strikethrough = null;

        try {
            underline = new SimpleGuiRuntime(shaders, dummyVao, PreSplashLayout.WIDTH, PreSplashLayout.HEIGHT);
            strikethrough = new SimpleGuiRuntime(shaders, dummyVao, PreSplashLayout.WIDTH, PreSplashLayout.HEIGHT);
            return new SimpleTextRuntime(
                    _ -> new FreeTypeFontHandle(face),
                    runtime -> {
                        DefaultTextRenderer renderer = new DefaultTextRenderer(runtime, new Tex2DArrayGlyphAtlas(1024, 1024), shaders, 256, PreSplashLayout.WIDTH, PreSplashLayout.HEIGHT);
                        renderer.loggerName = "pre_splash";
                        return renderer;
                    },
                    runtime -> {
                        int layoutPixelSize = config.pixelSize();
                        if (!minecraftFont) {
                            int capHeight = runtime.getGlyphMetrics(runtime.getFont().getGlyphIndex('A')).getGlyphHeight();
                            layoutPixelSize = Math.round(capHeight * GlyphBuilder.LINE_HEIGHT_PIXELS / (float) GlyphBuilder.MC_LINE_TOP_PIXELS);
                        }
                        return new DefaultTextProducer(runtime, layoutPixelSize, GlyphBuilder.LINE_HEIGHT_PIXELS);
                    },
                    shaders,
                    underline,
                    strikethrough,
                    config);
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

    private static float easeInOut(float t) {
        t = Math.clamp(t, 0f, 1f);
        return t * t * (3f - 2f * t);
    }

    public void render(int width, int height) {
        if (state == null) {
            return;
        }

        float scale = Math.min((float) width / PreSplashLayout.WIDTH, (float) height / PreSplashLayout.HEIGHT);
        int viewportWidth = Math.round(PreSplashLayout.WIDTH * scale);
        int viewportHeight = Math.round(PreSplashLayout.HEIGHT * scale);
        GL11.glViewport((width - viewportWidth) / 2, (height - viewportHeight) / 2, viewportWidth, viewportHeight);

        GL11.glClearColor(
                ((PreSplashLayout.BACKGROUND >> 16) & 255) / 255f,
                ((PreSplashLayout.BACKGROUND >> 8) & 255) / 255f,
                (PreSplashLayout.BACKGROUND & 255) / 255f,
                1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

        Preconditions.checkNotNull(logoAnimation);
        Preconditions.checkNotNull(text);
        Preconditions.checkNotNull(gui);

        //<editor-fold desc="intro">
        if (!mainStopWatch.isStarted()) {
            mainStopWatch.start();
        }

        if (introStage == IntroStage.LOGO && mainStopWatch.getTime(TimeUnit.MILLISECONDS) >= LOGO_ANIM_DURATION_MILLIS) {
            fontLoadingAtEntry = !fontLoader.isFinished();
            earlyTitleFallback = fontLoadingAtEntry || !resolveFont();
            introText = text;
            introStage = IntroStage.TITLE_WARMUP;
        }

        if (introStage == IntroStage.TITLE_HOLD) {
            if (!titleHoldStopWatch.isStarted()) {
                titleHoldStopWatch.start();
            }
            if (!englishWarmedUp) {
                introStage = IntroStage.ENGLISH_WARMUP;
            } else if (titleHoldStopWatch.getTime(TimeUnit.MILLISECONDS) >= EARLY_TITLE_HOLD_MILLIS) {
                introStage = IntroStage.FADE_OUT;
                introFadeOutStopWatch.start();
            }
        }

        if (introStage == IntroStage.FONT_LOADING && fontLoader.isFinished()) {
            introStage = IntroStage.BAR_FADE_OUT;
            loadingBarFadeOutStopWatch.start();
        }

        if (introStage == IntroStage.ENGLISH_WARMUP) {
            if (fontLoadingAtEntry) {
                resolveFont();
            }

            text.begin();
            text.append(ENGLISH_CHARACTERS, 0, 0, 40, 0x00FFFFFF);
            text.endDraw();
            GL11.glFinish();
            englishWarmedUp = true;

            if (fontLoadingAtEntry || titleHoldStopWatch.getTime(TimeUnit.MILLISECONDS) >= EARLY_TITLE_HOLD_MILLIS) {
                introStage = IntroStage.FADE_OUT;
                introFadeOutStopWatch.start();
            } else {
                introStage = IntroStage.TITLE_HOLD;
            }
        }

        if (introStage != IntroStage.FINISHED) {
            logoAnimation.render(viewportWidth, viewportHeight);
        }

        if (introStage != IntroStage.LOGO && introStage != IntroStage.FINISHED) {
            Preconditions.checkNotNull(introText);

            float progress = switch (introStage) {
                case TITLE_WARMUP -> 0;
                case TITLE_FADE_IN -> easeInOut(earlyTitleStopWatch.getTime(TimeUnit.MILLISECONDS) / (float) EARLY_TITLE_INOUT_ANIM_MILLIS);
                default -> 1;
            };

            String title = fontLoadingAtEntry ? "Warming Up" : "Cleanroom";
            int titleFontSize = earlyTitleFallback ? 30 : 40;
            var lineInfo = introText.simulate(title, 0, 0, titleFontSize);

            introText.begin();
            introText.appendStyled(
                    new StyledText(String.format("§c=rgba(255;255;255;%d)[%s]§", (int) (progress * 255), title)),
                    4 + (PreSplashLayout.WIDTH - lineInfo.getLineWidth()) / 2f,
                    PreSplashLayout.HEIGHT * 0.74f,
                    titleFontSize);
            introText.endDraw();

            boolean showLoadingBar = fontLoadingAtEntry && (introStage == IntroStage.TITLE_WARMUP
                    || introStage == IntroStage.TITLE_FADE_IN
                    || introStage == IntroStage.FONT_LOADING
                    || introStage == IntroStage.BAR_FADE_OUT);

            float barFadeOut = easeInOut(loadingBarFadeOutStopWatch.getTime(TimeUnit.MILLISECONDS) / (float) LOADING_BAR_FADE_OUT_MILLIS);
            float barProgress = progress * (1f - barFadeOut);
            long titleTime = earlyTitleStopWatch.getTime(TimeUnit.MILLISECONDS);
            long fadeOutTime = introFadeOutStopWatch.getTime(TimeUnit.MILLISECONDS);
            introFadeOutElapsedMillis += Math.min(fadeOutTime - introFadeOutFrameMillis, MAX_TRANSITION_FRAME_MILLIS);
            introFadeOutFrameMillis = fadeOutTime;
            float fadeOut = easeInOut(introFadeOutElapsedMillis / (float) INTRO_FADE_OUT_ANIM_MILLIS);

            gui.begin();
            gui.append(stream -> {
                if (showLoadingBar) {
                    float barHeight = 5;
                    float barWidth = 180;
                    float segmentWidth = 50;
                    float barX = (PreSplashLayout.WIDTH - barWidth) / 2f;
                    float barY = PreSplashLayout.HEIGHT * 0.83f;
                    float cycle = (titleTime % LOADING_BAR_CYCLE_MILLIS) / (float) LOADING_BAR_CYCLE_MILLIS;
                    float position = easeInOut(1f - Math.abs(cycle * 2f - 1f));
                    stream.rectEx(barX, barY, barWidth, barHeight, (int) (barProgress * 40) << 24 | 0xFFFFFF)
                            .radius(barHeight / 2f, 1)
                            .emit();
                    stream.rectEx(barX + position * (barWidth - segmentWidth), barY, segmentWidth, barHeight, (int) (barProgress * 255) << 24 | 0xB9C7DB)
                            .radius(barHeight / 2f, 1)
                            .emit();
                }

                stream.rect(
                        0,
                        0,
                        PreSplashLayout.WIDTH,
                        PreSplashLayout.HEIGHT,
                        (int) (fadeOut * 255) << 24 | (PreSplashLayout.BACKGROUND & 0xFFFFFF));
            });
            gui.endDraw();

            if (introStage == IntroStage.TITLE_WARMUP) {
                GL11.glFinish();
                introStage = IntroStage.TITLE_FADE_IN;
                earlyTitleStopWatch.start();
            } else if (introStage == IntroStage.TITLE_FADE_IN && progress >= 1) {
                introStage = fontLoadingAtEntry ? IntroStage.FONT_LOADING : IntroStage.TITLE_HOLD;
            } else if (introStage == IntroStage.BAR_FADE_OUT && barFadeOut >= 1) {
                introStage = IntroStage.ENGLISH_WARMUP;
            }

            if (introStage == IntroStage.FADE_OUT && !startupSignaled) {
                startupSignaled = true;
                startupReady.run();
            }
            if (introStage == IntroStage.FADE_OUT && fadeOut >= 1) {
                introStage = IntroStage.FINISHED;
            }
        }
        //</editor-fold>

        if (introStage == IntroStage.FINISHED) {
            text.begin();
            text.append(state.title(), 48, 106, 40, 0xFFF1F4FA);
            text.append(state.message(), 48, 190, 30, 0xFFB9C7DB);
            text.append(state.details(), 48, 286, 30, 0xFF8998AE);
            text.endDraw();

            long maskTime = postIntroMaskStopWatch.getTime(TimeUnit.MILLISECONDS);
            boolean maskFinished = postIntroMaskElapsedMillis >= POST_INTRO_MASK_FADE_OUT_MILLIS;
            if (!maskFinished) {
                postIntroMaskElapsedMillis += Math.min(maskTime - postIntroMaskFrameMillis, MAX_TRANSITION_FRAME_MILLIS);
                postIntroMaskFrameMillis = maskTime;
                float opacity = 1f - easeInOut(postIntroMaskElapsedMillis / (float) POST_INTRO_MASK_FADE_OUT_MILLIS);

                gui.begin();
                gui.append(stream -> stream.rect(
                        0,
                        0,
                        PreSplashLayout.WIDTH,
                        PreSplashLayout.HEIGHT,
                        (int) (opacity * 255) << 24 | (PreSplashLayout.BACKGROUND & 0xFFFFFF)));
                gui.endDraw();

                if (!postIntroMaskStopWatch.isStarted()) {
                    postIntroMaskStopWatch.start();
                }
            }

            if (maskFinished && introText != null) {
                SimpleTextRuntime previous = introText;
                introText = null;
                if (previous != text) {
                    Preconditions.checkNotNull(freeType);

                    try {
                        previous.close();
                    } catch (Throwable failure) {
                        LOGGER.warn("Could not dispose the pre splash intro text runtime.", failure);
                    } finally {
                        freeType.unload(FALLBACK_FONT);
                    }
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (mainStopWatch.isStarted()) {
            mainStopWatch.stop();
            LOGGER.info("Pre splash renderer finished in {}ms.", mainStopWatch.getTime(TimeUnit.MILLISECONDS));
        }

        fontLoader.close();
        try {
            try {
                if (introText != null && introText != text) {
                    introText.close();
                }
            } finally {
                if (text != null) {
                    text.close();
                }
            }
        } finally {
            try {
                if (logoAnimation != null) {
                    logoAnimation.close();
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
}

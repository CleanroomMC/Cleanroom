/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre;

import com.cleanroommc.anone.reveal.ReflectiveInvocation;
import com.cleanroommc.kirino.engine.render.core.shader.ImmediateShaderAccess;
import com.cleanroommc.kirino.gl.GLResourceManager;
import com.cleanroommc.kirino.gl.buffer.view.VBOView;
import com.cleanroommc.kirino.gl.vao.VAO;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.ui.font.McTtfFontManager;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.data.MetadataSerializer;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.launcher.FMLTweaker;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_Event;
import org.lwjgl.sdl.SDL_EventFilter;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.ThreadLocalUtil;
import org.lwjgl.util.freetype.FreeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * It owns the temporary window, isolated class loading, GL context, and all rendering resources.
 */
public final class PreSplashScreenContainer {

    private static @Nullable PreSplashScreenContainer instance;

    public static final Logger LOGGER = LogManager.getLogger("Pre Splash Screen");

    private static final int WIDTH = 720;
    private static final int HEIGHT = 360;
    private static final float SCREEN_PERCENTAGE = 0.35f;
    private static final float UI_SCALE = 1.15f;

    private static final int INITIAL_WAIT_SECOND = 10;

    private final Thread gameThread;
    private final Thread thread;
    private final Thread shutdownHook;

    private final CountDownLatch nativeReady = new CountDownLatch(1);
    private final CountDownLatch firstFrameReady = new CountDownLatch(1);

    private final File assetsDirectory;
    private final @Nullable String assetIndex;

    private volatile boolean running = true;

    /**
     * Must be called on the game thread.
     *
     * @see #start(File, String)
     */
    private PreSplashScreenContainer(File assetsDirectory, @Nullable String assetIndex) {
        Preconditions.checkNotNull(assetsDirectory);

        gameThread = Thread.currentThread();
        this.assetsDirectory = assetsDirectory;
        this.assetIndex = assetIndex;
        thread = new Thread(this::run, "Pre Splash Renderer");
        thread.setDaemon(true);
        shutdownHook = new Thread(this::stopAndWait, "Pre Splash Shutdown");
    }

    /**
     * Called only from the client tweaker, right after publishing {@link PreSplashPhase#FML_SETUP}.
     *
     * @see FMLTweaker#injectIntoClassLoader(LaunchClassLoader)
     */
    @ApiStatus.Internal
    public static synchronized void start(File assetsDirectory, @Nullable String assetIndex) {
        Preconditions.checkNotNull(assetsDirectory);
        Preconditions.checkState(instance == null, "Pre splash has already started.");

        PreSplashScreenContainer screen = new PreSplashScreenContainer(assetsDirectory, assetIndex);
        instance = screen;

        Runtime.getRuntime().addShutdownHook(screen.shutdownHook);
        screen.thread.start(); // this::run

        // wait for native initialization
        boolean interrupted = false;
        while (true) {
            try {
                screen.nativeReady.await();
                screen.firstFrameReady.await();
                break;
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (screen.running) {
            try {
                TimeUnit.SECONDS.sleep(INITIAL_WAIT_SECOND);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Called only from the client handler, right after publishing {@link PreSplashPhase#RIGHT_BEFORE_SPLASH}.
     *
     * @see FMLClientHandler#beginMinecraftLoading(Minecraft, List, IReloadableResourceManager, MetadataSerializer)
     */
    @ApiStatus.Internal
    public static synchronized void finish() {
        if (instance == null) {
            return;
        }

        instance.stopAndWait();
    }

    private void stopAndWait() {
        running = false;
        LockSupport.unpark(thread);

        boolean interrupted = false;
        while (thread.isAlive()) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private void run() {
        Thread renderThread = Thread.currentThread();
        ClassLoader previousContextLoader = renderThread.getContextClassLoader();

        try {
            ClassLoader loader = new PreSplashClassLoader(PreSplashScreenContainer.class.getClassLoader());
            renderThread.setContextClassLoader(loader);

            Class<?> renderer = Class.forName(PreSplashScreenContainer.class.getName() + "$Renderer", true, loader);
            Runnable render = (Runnable) renderer.getConstructor(
                    File.class,
                    String.class,
                    BooleanSupplier.class,
                    Supplier.class,
                    Runnable.class,
                    Runnable.class,
                    Runnable.class).newInstance(
                            assetsDirectory,
                            assetIndex,
                            (BooleanSupplier) () -> running && gameThread.isAlive(),
                            (Supplier<PreSplashPhase>) PreSplashManager.INSTANCE::readPhase,
                            (Runnable) () -> running = false,
                            (Runnable) nativeReady::countDown,
                            (Runnable) firstFrameReady::countDown);

            // ENTRYPOINT
            render.run();

        } catch (Throwable failure) {
            LOGGER.warn("Pre splash unavailable. Continue normal startup.", failure);
        } finally {
            running = false;
            nativeReady.countDown();
            firstFrameReady.countDown();
            renderThread.setContextClassLoader(previousContextLoader);
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // if shutdown has already begun
            }
        }
    }

    /**
     * Defined only by {@link PreSplashClassLoader}.
     *
     * @see PreSplashScreenContainer#run()
     */
    @ApiStatus.Internal
    public static final class Renderer implements Runnable {

        private final File assetsDirectory;
        private final @Nullable String assetIndex;
        private final BooleanSupplier running;
        private final Supplier<PreSplashPhase> phase;
        private final Runnable stop;
        private final Runnable nativeReady;
        private final Runnable firstFrameReady;

        @ReflectiveInvocation
        public Renderer(
                File assetsDirectory,
                @Nullable String assetIndex,
                BooleanSupplier running,
                Supplier<PreSplashPhase> phase,
                Runnable stop,
                Runnable nativeReady,
                Runnable firstFrameReady) {

            Preconditions.checkNotNull(assetsDirectory);
            Preconditions.checkNotNull(running);
            Preconditions.checkNotNull(phase);
            Preconditions.checkNotNull(stop);
            Preconditions.checkNotNull(nativeReady);
            Preconditions.checkNotNull(firstFrameReady);

            this.assetsDirectory = assetsDirectory;
            this.assetIndex = assetIndex;
            this.running = running;
            this.phase = phase;
            this.stop = stop;
            this.nativeReady = nativeReady;
            this.firstFrameReady = firstFrameReady;
        }

        private static void checkSDL(boolean success, String operation) {
            Preconditions.checkState(success, "%s: %s", operation, SDLError.SDL_GetError());
        }

        /**
         * @see PreSplashScreenContainer#run()
         */
        @Override
        public void run() {
            long window = 0;
            long context = 0;
            boolean videoInitialized = false;
            SDL_EventFilter watch = null;
            String originalLibraryPath = System.getProperty("org.lwjgl.librarypath");
            boolean initialized = false;

            try {
                // configure this loader's (PreSplashClassLoader) LWJGL copy
                Configuration.LIBRARY_PATH.set(null);
                Configuration.SHARED_LIBRARY_EXTRACT_PATH.set(null);
                Configuration.SHARED_LIBRARY_EXTRACT_DIRECTORY.set("cleanroom-pre-splash");
                Configuration.SHARED_LIBRARY_EXTRACT_FORCE.set(true);

                // prefer native Wayland over XWayland
                if (System.getenv("WAYLAND_DISPLAY") != null && System.getenv("SDL_VIDEODRIVER") == null) {
                    if (!SDLHints.SDL_SetHintWithPriority(SDLHints.SDL_HINT_VIDEO_DRIVER, "wayland,x11", SDLHints.SDL_HINT_DEFAULT)) {
                        LOGGER.debug("Could not apply pre splash video driver preference; keeping the configured driver.");
                    }
                }

                checkSDL(SDLInit.SDL_InitSubSystem(SDLInit.SDL_INIT_VIDEO | SDLInit.SDL_INIT_EVENTS), "SDL_InitSubSystem");

                videoInitialized = true;

                SDLVideo.SDL_GL_ResetAttributes();
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, 4), "GL major version");
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, 6), "GL minor version");
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE), "GL profile");
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_DOUBLEBUFFER, 1), "GL double buffering");

                int display = SDLVideo.SDL_GetPrimaryDisplay();
                Preconditions.checkState(display != 0, "SDL_GetPrimaryDisplay: %s", SDLError.SDL_GetError());

                // must not be SDL.window() which belongs to Minecraft
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    SDL_Rect bounds = SDL_Rect.malloc(stack);
                    checkSDL(SDLVideo.SDL_GetDisplayBounds(display, bounds), "SDL_GetDisplayBounds");

                    float scale = Math.min((float) bounds.w() / WIDTH, (float) bounds.h() / HEIGHT) * SCREEN_PERCENTAGE;
                    int windowWidth = Math.max(1, Math.round(WIDTH * scale));
                    int windowHeight = Math.max(1, Math.round(HEIGHT * scale));

                    window = SDLVideo.SDL_CreateWindow("Cleanroom Pre Splash", windowWidth, windowHeight,
                            SDLVideo.SDL_WINDOW_OPENGL
                                    | SDLVideo.SDL_WINDOW_HIGH_PIXEL_DENSITY
                                    | SDLVideo.SDL_WINDOW_HIDDEN
                                    | SDLVideo.SDL_WINDOW_NOT_FOCUSABLE
                                    | SDLVideo.SDL_WINDOW_ALWAYS_ON_TOP);

                    Preconditions.checkState(window != 0, "SDL_CreateWindow: %s", SDLError.SDL_GetError());

                    // fixed size window
                    checkSDL(SDLVideo.SDL_SetWindowMinimumSize(window, windowWidth, windowHeight), "SDL_SetWindowMinimumSize");
                    checkSDL(SDLVideo.SDL_SetWindowMaximumSize(window, windowWidth, windowHeight), "SDL_SetWindowMaximumSize");
                }

                // Wayland does not support explicit positioning
                String videoDriver = SDLVideo.SDL_GetCurrentVideoDriver();
                Preconditions.checkState(videoDriver != null, "SDL video driver is not initialized.");
                if (!"wayland".equals(videoDriver)) {
                    int centered = SDLVideo.SDL_WINDOWPOS_CENTERED_DISPLAY(display);
                    if (!SDLVideo.SDL_SetWindowPosition(window, centered, centered)) {
                        LOGGER.debug("Could not center pre splash window: {}", SDLError.SDL_GetError());
                    }
                }

                context = SDLVideo.SDL_GL_CreateContext(window);
                Preconditions.checkState(context != 0, "SDL_GL_CreateContext: %s", SDLError.SDL_GetError());
                checkSDL(SDLVideo.SDL_GL_MakeCurrent(window, context), "SDL_GL_MakeCurrent");
                // todo: implement multi gl backend
                Preconditions.checkState(GL.createCapabilities().OpenGL46, "Pre splash requires OpenGL 4.6.");

                // release LWJGL's global table so the game's LWJGL can initialize
                ThreadLocalUtil.setFunctionMissingAddresses(0);

                if (!SDLVideo.SDL_GL_SetSwapInterval(0)) {
                    LOGGER.debug("Could not disable pre splash VSync: {}", SDLError.SDL_GetError());
                }

                SDLVideo.SDL_GL_ResetAttributes();

                int windowId = SDLVideo.SDL_GetWindowID(window);
                checkSDL(windowId != 0, "SDL_GetWindowID");

                watch = SDL_EventFilter.create((_, address) -> {
                    // borrowed. must not close
                    SDL_Event event = SDL_Event.create(address);
                    if (event.type() == SDLEvents.SDL_EVENT_QUIT
                            || (event.type() == SDLEvents.SDL_EVENT_WINDOW_CLOSE_REQUESTED && event.window().windowID() == windowId)) {
                        stop.run();
                    }
                    return true;
                });
                checkSDL(SDLEvents.SDL_AddEventWatch(watch, 0), "SDL_AddEventWatch");

                if (originalLibraryPath == null) {
                    System.clearProperty("org.lwjgl.librarypath");
                } else {
                    System.setProperty("org.lwjgl.librarypath", originalLibraryPath);
                }

                initialized = true;
                nativeReady.run();

                // ACTUAL RENDERING ENTRYPOINT
                try (GLResourceManager.ResourceScope ignored = GLResourceManager.openScope()) {
                    if (running.getAsBoolean()) {
                        render(window);
                    }
                }

            } catch (Throwable failure) {
                LOGGER.warn("Pre splash unavailable. Continue normal startup.", failure);
            } finally {
                stop.run();
                try {
                    if (watch != null) {
                        SDLEvents.SDL_RemoveEventWatch(watch, 0);
                        watch.free();
                    }
                    GL.setCapabilities(null);
                    if (context != 0) {
                        if (!SDLVideo.SDL_GL_MakeCurrent(window, 0)) {
                            LOGGER.warn("Could not detach pre splash GL context: {}", SDLError.SDL_GetError());
                        }
                        if (!SDLVideo.SDL_GL_DestroyContext(context)) {
                            LOGGER.warn("Could not destroy pre splash GL context: {}", SDLError.SDL_GetError());
                        }
                    }
                    GL.destroy();
                    if (window != 0) {
                        SDLVideo.SDL_DestroyWindow(window);
                    }
                    if (videoInitialized) {
                        if (!initialized) {
                            SDLVideo.SDL_GL_ResetAttributes();
                        }
                        SDLInit.SDL_QuitSubSystem(SDLInit.SDL_INIT_VIDEO | SDLInit.SDL_INIT_EVENTS);
                    }
                } finally {
                    if (!initialized) {
                        if (originalLibraryPath == null) {
                            System.clearProperty("org.lwjgl.librarypath");
                        } else {
                            System.setProperty("org.lwjgl.librarypath", originalLibraryPath);
                        }
                    }
                    nativeReady.run();
                    firstFrameReady.run();
                }
            }
        }

        private void render(long window) throws Exception {
            var constructor = FreeTypeManager.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            FreeTypeManager freeType = constructor.newInstance();
            freeType.loggerName = "pre_splash";
            freeType.init();
            try {
                McTtfFontManager fonts = PreSplashFontResources.create(assetsDirectory, assetIndex, freeType);
                try {
                    var face = fonts.loadDefaultFace(48);
                    Preconditions.checkState(face != null, "Could not prepare the default Minecraft font.");
                    ImmediateShaderAccess shaders = new ImmediateShaderAccess(rl -> {
                        String path = "/assets/" + rl.getNamespace() + "/" + rl.getPath();
                        try (InputStream input = PreSplashScreenContainer.class.getResourceAsStream(path)) {
                            Preconditions.checkNotNull(input, "Missing pre splash shader: %s", path);
                            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
                        } catch (IOException e) {
                            throw new IllegalStateException("Could not load pre splash shader " + path, e);
                        }
                    });
                    AttributeLayout layout = new AttributeLayout();
                    layout.push(new Stride(0));
                    VAO dummyVao = new VAO(layout, null, (VBOView[]) null);
                    ST_Config config = new ST_Config(
                            ST_FontBackendType.FREE_TYPE, 48, 16, 12,
                            FreeType.FT_LOAD_RENDER | FreeType.FT_LOAD_NO_HINTING);
                    float uiWidth = WIDTH / UI_SCALE;
                    float uiHeight = HEIGHT / UI_SCALE;
                    try (SimpleGuiRuntime gui = new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight);
                         SimpleTextRuntime text = new SimpleTextRuntime(
                            (_, _) -> new FreeTypeFontHandle(face),
                            runtime -> {
                                DefaultTextRenderer renderer = new DefaultTextRenderer(runtime, new Tex2DArrayGlyphAtlas(1024, 1024), shaders, 256, uiWidth, uiHeight);
                                renderer.loggerName = "pre_splash";
                                return renderer;
                            },
                            runtime -> new DefaultTextProducer(runtime, config.pixelSize(), GlyphBuilder.LINE_HEIGHT_PIXELS),
                            shaders, new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight),
                            new SimpleGuiRuntime(shaders, dummyVao, uiWidth, uiHeight), config, null)) {

                        drawLoop(window, text, gui);
                    }
                } finally {
                    fonts.destroy();
                }
            } finally {
                freeType.destroy();
            }
        }

        private void drawLoop(long window, SimpleTextRuntime text, SimpleGuiRuntime gui) {
            checkSDL(SDLVideo.SDL_ShowWindow(window), "SDL_ShowWindow");
            long started = System.nanoTime();
            boolean firstFrame = true;
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer width = stack.mallocInt(1);
                IntBuffer height = stack.mallocInt(1);
                while (running.getAsBoolean()) {
                    long frameStart = System.nanoTime();
                    PreSplashPhase currentPhase = phase.get();
                    if (currentPhase == PreSplashPhase.RIGHT_BEFORE_SPLASH) {
                        break;
                    }
                    SDLEvents.SDL_PumpEvents();
                    checkSDL(SDLVideo.SDL_GetWindowSizeInPixels(window, width, height), "SDL_GetWindowSizeInPixels");
                    if (width.get(0) <= 0 || height.get(0) <= 0) {
                        LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
                        continue;
                    }

                    float scale = Math.min((float) width.get(0) / WIDTH, (float) height.get(0) / HEIGHT);
                    int viewportWidth = Math.round(WIDTH * scale);
                    int viewportHeight = Math.round(HEIGHT * scale);
                    GL11.glViewport((width.get(0) - viewportWidth) / 2, (height.get(0) - viewportHeight) / 2, viewportWidth, viewportHeight);

                    GL11.glClearColor(0.055f, 0.065f, 0.085f, 1);
                    GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
                    String label = switch (currentPhase) {
                        case FML_SETUP -> "Preparing Forge Mod Loader";
                        case INITIAL_MOD_SCAN -> "Discovering mods";
                        case CORE_MOD_LAUNCH -> "Loading core mods";
                        case MINECRAFT_INIT -> "Initializing Minecraft";
                        case RIGHT_BEFORE_SPLASH -> "Starting mod loading";
                    };
                    int stage = currentPhase.ordinal() + 1;
                    int stages = PreSplashPhase.RIGHT_BEFORE_SPLASH.ordinal();
                    gui.begin().append(stream -> {
                        stream.rect(48, 264, 544, 4, 0xFF30384A);
                        stream.rect(48, 264, 544f * stage / stages, 4, 0xFF88B8F8);
                    }).endDraw();
                    text.begin()
                        .append("Cleanroom", 48, 106, 32, 0xFFF1F4FA)
                        .append(label, 48, 190, 16, 0xFFB9C7DB)
                        .append("Stage " + stage + " / " + stages + "  |  " + TimeUnit.NANOSECONDS.toSeconds(frameStart - started) + "s", 48, 286, 16, 0xFF8998AE)
                        .endDraw();
                    checkSDL(SDLVideo.SDL_GL_SwapWindow(window), "SDL_GL_SwapWindow");
                    if (firstFrame) {
                        firstFrame = false;
                        firstFrameReady.run();
                    }
                    long remaining = TimeUnit.SECONDS.toNanos(1) / 30 - (System.nanoTime() - frameStart);
                    if (remaining > 0) {
                        LockSupport.parkNanos(remaining);
                    }
                }
            }
        }
    }
}

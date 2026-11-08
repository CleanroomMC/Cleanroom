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
import com.cleanroommc.cleanroom.splash.pre.renderer.PreSplashLayout;
import com.cleanroommc.cleanroom.splash.pre.renderer.PreSplashLogger;
import com.cleanroommc.cleanroom.splash.pre.renderer.SdlSplashRenderer;
import com.cleanroommc.cleanroom.splash.pre.renderer.SwingSplashRenderer;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

/**
 * It owns the renderer lifecycle, SDL window OR Swing process.
 */
public final class PreSplashScreenContainer {

    private static @Nullable PreSplashScreenContainer instance;

    public static final Logger LOGGER = LogManager.getLogger("Pre Splash Screen");

    private static final int THREAD_START_TIMEOUT_SECOND = 45;
    private static final int THREAD_STOP_TIMEOUT_SECOND = 10;

    private final Thread gameThread;
    private final Thread thread;
    private final Thread shutdownHook;
    private final PreSplashPresentation presentation;

    private final CountDownLatch startupReady = new CountDownLatch(1);

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
        presentation = new PreSplashPresentation(PreSplashManager.INSTANCE);
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
    public static void start(File assetsDirectory, @Nullable String assetIndex) {
        Preconditions.checkNotNull(assetsDirectory);

        PreSplashScreenContainer screen;
        synchronized (PreSplashScreenContainer.class) {
            Preconditions.checkState(instance == null, "Pre splash has already started.");
            screen = new PreSplashScreenContainer(assetsDirectory, assetIndex);
            instance = screen;

            Runtime.getRuntime().addShutdownHook(screen.shutdownHook);
            screen.thread.start(); // this::run
        }

        // wait until the renderer releases game startup
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(THREAD_START_TIMEOUT_SECOND);
        boolean interrupted = false;
        while (true) {
            try {
                if (!screen.startupReady.await(deadline - System.nanoTime(), TimeUnit.NANOSECONDS)) {
                    LOGGER.warn("Pre splash renderer did not become ready within {} seconds.", THREAD_START_TIMEOUT_SECOND);
                    screen.stopAndWait();
                }
                break;
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
    public static void finish() {
        PreSplashScreenContainer screen;
        synchronized (PreSplashScreenContainer.class) {
            if (instance == null) {
                return;
            }

            screen = instance;
        }

        screen.stopAndWait();
    }

    private void stopAndWait() {
        running = false;
        LockSupport.unpark(thread);
        if (Thread.currentThread() == thread) {
            return;
        }

        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(THREAD_STOP_TIMEOUT_SECOND);
        boolean interrupted = false;
        while (thread.isAlive()) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                LOGGER.warn("Pre splash renderer did not stop within {} seconds.", THREAD_STOP_TIMEOUT_SECOND);
                break;
            }
            try {
                TimeUnit.NANOSECONDS.timedJoin(thread, remaining);
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        // only if the renderer has not signaled yet
        startupReady.countDown();

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isRunning() {
        return running && gameThread.isAlive() && !presentation.isFinished();
    }

    private void run() {
        try {
            LOGGER.info("Starting pre splash screen.");
            if (!isRunning()) {
                return;
            }
            boolean mac = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("mac");
            if (mac || !runSdl()) {
                if (isRunning()) {
                    runSwing();
                }
            }
        } catch (Throwable failure) {
            LOGGER.warn("Pre splash unavailable. Continue normal startup.", failure);
        } finally {
            running = false;
            startupReady.countDown();
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException ignored) {
                // if shutdown has already begun
            }
            LOGGER.info("Pre splash screen stopped.");
        }
    }

    //<editor-fold desc="runSdl & runSwing">
    private boolean runSdl() {
        Thread renderThread = Thread.currentThread();
        ClassLoader previousContextLoader = renderThread.getContextClassLoader();
        boolean[] unavailable = { false };

        try {
            ClassLoader loader = new PreSplashClassLoader(PreSplashScreenContainer.class.getClassLoader());
            renderThread.setContextClassLoader(loader);

            Class<?> renderer = Class.forName(PreSplashScreenContainer.class.getName() + "$SdlRenderer", true, loader);
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
                            (BooleanSupplier) this::isRunning,
                            (Supplier<PreSplashState>) presentation::nextFrame,
                            (Runnable) () -> running = false,
                            (Runnable) startupReady::countDown,
                            (Runnable) () -> unavailable[0] = true);

            // SDL ENTRYPOINT
            render.run();

        } catch (Throwable failure) {
            LOGGER.warn("SDL pre splash unavailable. Falling back to Swing.", failure);
            return false;
        } finally {
            renderThread.setContextClassLoader(previousContextLoader);
        }

        return !unavailable[0];
    }

    private void runSwing() throws IOException {
        // SWING ENTRYPOINT
        new SwingRenderer(
                this::isRunning,
                presentation::nextFrame,
                startupReady::countDown).run();
    }
    //</editor-fold>

    //<editor-fold desc="SdlRenderer & SwingRenderer">
    /**
     * @see PreSplashScreenContainer#runSdl()
     */
    @ApiStatus.Internal
    public static final class SdlRenderer implements Runnable {

        private final File assetsDirectory;
        private final @Nullable String assetIndex;
        private final BooleanSupplier running;
        private final Supplier<PreSplashState> state;
        private final Runnable stop;
        private final Runnable startupReady;
        private final Runnable unavailable;

        @ReflectiveInvocation
        public SdlRenderer(
                File assetsDirectory,
                @Nullable String assetIndex,
                BooleanSupplier running,
                Supplier<PreSplashState> state,
                Runnable stop,
                Runnable startupReady,
                Runnable unavailable) {

            Preconditions.checkNotNull(assetsDirectory);
            Preconditions.checkNotNull(running);
            Preconditions.checkNotNull(state);
            Preconditions.checkNotNull(stop);
            Preconditions.checkNotNull(startupReady);
            Preconditions.checkNotNull(unavailable);

            this.assetsDirectory = assetsDirectory;
            this.assetIndex = assetIndex;
            this.running = running;
            this.state = state;
            this.stop = stop;
            this.startupReady = startupReady;
            this.unavailable = unavailable;
        }

        @Override
        public void run() {
            try (Window window = Window.open(stop)) {
                if (running.getAsBoolean()) {
                    try (SdlSplashRenderer renderer = new SdlSplashRenderer(assetsDirectory, assetIndex, startupReady)) {
                        window.runLoop(running, (width, height) -> {
                            renderer.update(state.get());
                            renderer.render(width, height);
                        });
                    }
                }
            } catch (Throwable failure) {
                LOGGER.warn("SDL pre splash unavailable. Falling back to Swing.", failure);
                unavailable.run();
            }
        }

        static final class Window implements AutoCloseable {

            private static final int TARGET_FRAME_RATE = 60;

            private final @Nullable String originalLibraryPath = System.getProperty("org.lwjgl.librarypath");
            private long window;
            private long context;
            private boolean videoInitialized;
            private boolean initialized;
            private boolean glInitialized;
            private @Nullable SDL_EventFilter watch;

            static Window open(Runnable stop) {
                Window window = new Window();
                try {
                    window.initialize(stop);
                    return window;
                } catch (RuntimeException | Error failure) {
                    try {
                        window.close();
                    } catch (Throwable cleanup) {
                        failure.addSuppressed(cleanup);
                    }
                    throw failure;
                }
            }

            private static void checkSDL(boolean success, String operation) {
                Preconditions.checkState(success, "%s: %s", operation, SDLError.SDL_GetError());
            }

            private void initialize(Runnable stop) {
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
                // on non-Apple platforms, the video initializing thread is SDL's main thread
                Preconditions.checkState(SDLInit.SDL_IsMainThread(), "Pre splash must run on its SDL video thread.");

                SDLVideo.SDL_GL_ResetAttributes();
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MAJOR_VERSION, 4), "GL major version");
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_MINOR_VERSION, 6), "GL minor version");
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_CONTEXT_PROFILE_MASK, SDLVideo.SDL_GL_CONTEXT_PROFILE_CORE), "GL profile");
                checkSDL(SDLVideo.SDL_GL_SetAttribute(SDLVideo.SDL_GL_DOUBLEBUFFER, 1), "GL double buffering");

                int display = SDLVideo.SDL_GetPrimaryDisplay();
                Preconditions.checkState(display != 0, "SDL_GetPrimaryDisplay: %s", SDLError.SDL_GetError());

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    SDL_Rect bounds = SDL_Rect.malloc(stack);
                    checkSDL(SDLVideo.SDL_GetDisplayBounds(display, bounds), "SDL_GetDisplayBounds");

                    float scale = Math.min((float) bounds.w() / PreSplashLayout.WIDTH, (float) bounds.h() / PreSplashLayout.HEIGHT) * PreSplashLayout.SCREEN_PERCENTAGE;
                    int windowWidth = Math.max(1, Math.round(PreSplashLayout.WIDTH * scale));
                    int windowHeight = Math.max(1, Math.round(PreSplashLayout.HEIGHT * scale));

                    window = SDLVideo.SDL_CreateWindow("Cleanroom Pre Splash", windowWidth, windowHeight,
                            SDLVideo.SDL_WINDOW_OPENGL
                                    | SDLVideo.SDL_WINDOW_HIGH_PIXEL_DENSITY
                                    | SDLVideo.SDL_WINDOW_HIDDEN
                                    | SDLVideo.SDL_WINDOW_UTILITY
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

                // triggers class initializing
                GL.getFunctionProvider();

                glInitialized = true;
                boolean gl46 = GL.createCapabilities().OpenGL46;

                // release LWJGL's global table so the game's LWJGL can initialize
                ThreadLocalUtil.setFunctionMissingAddresses(0);
                Preconditions.checkState(gl46, "Pre splash requires OpenGL 4.6.");

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
            }

            void runLoop(BooleanSupplier running, BiConsumer<Integer, Integer> draw) {
                checkSDL(SDLVideo.SDL_ShowWindow(window), "SDL_ShowWindow");
                try (MemoryStack stack = MemoryStack.stackPush()) {
                    IntBuffer width = stack.mallocInt(1);
                    IntBuffer height = stack.mallocInt(1);
                    while (running.getAsBoolean()) {
                        long frameStart = System.nanoTime();
                        SDLEvents.SDL_PumpEvents();
                        checkSDL(SDLVideo.SDL_GetWindowSizeInPixels(window, width, height), "SDL_GetWindowSizeInPixels");
                        if (width.get(0) <= 0 || height.get(0) <= 0) {
                            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(100));
                            continue;
                        }

                        draw.accept(width.get(0), height.get(0));
                        checkSDL(SDLVideo.SDL_GL_SwapWindow(window), "SDL_GL_SwapWindow");
                        long remaining = TimeUnit.SECONDS.toNanos(1) / TARGET_FRAME_RATE - (System.nanoTime() - frameStart);
                        if (remaining > 0) {
                            LockSupport.parkNanos(remaining);
                        }
                    }
                }
            }

            @Override
            public void close() {
                try {
                    if (watch != null) {
                        SDLEvents.SDL_RemoveEventWatch(watch, 0);
                        watch.free();
                    }
                    if (glInitialized) {
                        GL.setCapabilities(null);
                        if (!initialized) {
                            ThreadLocalUtil.setFunctionMissingAddresses(0);
                        }
                    }
                    if (context != 0) {
                        if (!SDLVideo.SDL_GL_MakeCurrent(window, 0)) {
                            LOGGER.warn("Could not detach pre splash GL context: {}", SDLError.SDL_GetError());
                        }
                        if (!SDLVideo.SDL_GL_DestroyContext(context)) {
                            LOGGER.warn("Could not destroy pre splash GL context: {}", SDLError.SDL_GetError());
                        }
                    }
                } finally {
                    try {
                        if (glInitialized) {
                            GL.destroy();
                        }
                    } finally {
                        try {
                            if (window != 0) {
                                SDLVideo.SDL_DestroyWindow(window);
                            }
                        } finally {
                            try {
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
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @see PreSplashScreenContainer#runSwing()
     */
    @ApiStatus.Internal
    public static final class SwingRenderer {

        private static final int HELPER_STOP_TIMEOUT_SECOND = 2;

        private final BooleanSupplier running;
        private final Supplier<PreSplashState> state;
        private final Runnable startupReady;

        private SwingRenderer(BooleanSupplier running, Supplier<PreSplashState> state, Runnable startupReady) {
            this.running = running;
            this.state = state;
            this.startupReady = startupReady;
        }

        private void run() throws IOException {
            new HelperProcess(running, state, startupReady).run();
        }

        static final class Protocol {

            private static final int STATE = 1;
            private static final int CLOSE = 2;
            private static final int READY = 3;
            private static final int LOG = 4;
            private static final int MAX_LOG_BYTES = 1024 * 1024;

            private Protocol() {
            }

            static void writeState(DataOutputStream output, PreSplashState state) throws IOException {
                output.writeByte(STATE);
                output.writeUTF(state.title());
                output.writeUTF(state.message());
                output.writeUTF(state.details());
                output.flush();
            }

            @Nullable
            static PreSplashState readState(DataInputStream input) throws IOException {
                int command;
                try {
                    command = input.readUnsignedByte();
                } catch (IOException ignored) {
                    return null;
                }
                if (command == CLOSE) {
                    return null;
                }
                if (command != STATE) {
                    throw new IOException("Invalid pre splash command: " + command);
                }
                return new PreSplashState(input.readUTF(), input.readUTF(), input.readUTF());
            }

            static void close(DataOutputStream output) throws IOException {
                output.writeByte(CLOSE);
                output.flush();
            }

            static void ready(DataOutputStream output) throws IOException {
                synchronized (output) {
                    output.writeByte(READY);
                    output.flush();
                }
            }

            static void log(DataOutputStream output, PreSplashLogger.Level level, String message, @Nullable Throwable failure) throws IOException {
                if (failure != null) {
                    StringWriter stack = new StringWriter();
                    failure.printStackTrace(new PrintWriter(stack));
                    message += "\n" + stack;
                }

                byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
                if (bytes.length > MAX_LOG_BYTES) {
                    throw new IOException("Pre splash log message is too large.");
                }

                synchronized (output) {
                    output.writeByte(LOG);
                    output.writeByte(level.ordinal());
                    output.writeInt(bytes.length);
                    output.write(bytes);
                    output.flush();
                }
            }

            private static final class ReplyReader {

                private final DataInputStream input;
                private byte @Nullable [] bytes;
                private int level;
                private int offset;

                private ReplyReader(DataInputStream input) {
                    this.input = input;
                }

                private int poll() throws IOException {
                    if (bytes == null) {
                        if (input.available() == 0) {
                            return 0;
                        }
                        input.mark(6);
                        int reply = input.readUnsignedByte();
                        if (reply == READY) {
                            return READY;
                        }
                        if (reply != LOG) {
                            throw new IOException("Invalid pre splash reply: " + reply);
                        }
                        if (input.available() < 5) {
                            input.reset();
                            return 0;
                        }
                        level = input.readUnsignedByte();
                        int length = input.readInt();
                        if (level >= PreSplashLogger.Level.values().length || length < 0 || length > MAX_LOG_BYTES) {
                            throw new IOException("Invalid pre splash log message.");
                        }
                        bytes = new byte[length];
                        offset = 0;
                    }

                    int available = Math.min(input.available(), bytes.length - offset);
                    input.readFully(bytes, offset, available);
                    offset += available;
                    if (offset < bytes.length) {
                        return 0;
                    }

                    PreSplashLogger.LOGGER.log(PreSplashLogger.Level.values()[level], new String(bytes, StandardCharsets.UTF_8));
                    bytes = null;
                    return LOG;
                }
            }
        }

        private record HelperProcess(BooleanSupplier running, Supplier<PreSplashState> state, Runnable startupReady) {

            private static void writeHelperClass(JarOutputStream output, Class<?> type, Set<String> entries) throws IOException {
                String resource = type.getName().replace('.', '/') + ".class";
                if (!entries.add(resource)) {
                    return;
                }

                try (InputStream input = PreSplashScreenContainer.class.getResourceAsStream("/" + resource)) {
                    Preconditions.checkNotNull(input, "Missing Swing pre splash class: %s", type.getName());
                    output.putNextEntry(new JarEntry(resource));
                    input.transferTo(output);
                    output.closeEntry();
                }

                for (Class<?> nested : type.getDeclaredClasses()) {
                    writeHelperClass(output, nested, entries);
                }
            }

            private void run() throws IOException {
                String renderer = SwingRenderer.class.getName() + "$Helper";
                String windowTitle = "Cleanroom Pre Splash [" + UUID.randomUUID() + "]";
                Path helperJar = Files.createTempFile("cleanroom-pre-splash-", ".jar");
                Process helper = null;

                try {
                    try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(helperJar))) {
                        Set<String> entries = new HashSet<>();
                        for (Class<?> type : List.of(Protocol.class, Helper.class, PreSplashState.class, SwingSplashRenderer.class, PreSplashLayout.class, PreSplashLogger.class)) {
                            writeHelperClass(output, type, entries);
                        }
                    }

                    boolean windows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows");
                    Path java = Path.of(System.getProperty("java.home"), "bin", windows ? "java.exe" : "java");

                    ProcessBuilder builder = new ProcessBuilder(
                            java.toString(),
                            "-Dapple.awt.UIElement=true",
                            "-cp", helperJar.toString(),
                            renderer,
                            Long.toString(ProcessHandle.current().pid()),
                            windowTitle);

                    builder.environment().remove("JDK_JAVA_OPTIONS");
                    builder.environment().remove("JAVA_TOOL_OPTIONS");
                    builder.environment().remove("_JAVA_OPTIONS");
                    builder.redirectError(ProcessBuilder.Redirect.INHERIT);

                    helper = builder.start();

                    try (DataOutputStream commands = new DataOutputStream(new BufferedOutputStream(helper.getOutputStream()));
                         DataInputStream replies = new DataInputStream(new BufferedInputStream(helper.getInputStream()))) {

                        PreSplashState previous = null;
                        boolean ready = false;
                        Protocol.ReplyReader reader = new Protocol.ReplyReader(replies);

                        while (running.getAsBoolean()) {
                            PreSplashState current = state.get();
                            if (!helper.isAlive()) {
                                LOGGER.warn("Swing pre splash helper exited with code {}.", helper.exitValue());
                                break;
                            }

                            if (current != previous) {
                                Protocol.writeState(commands, current);
                                previous = current;
                            }

                            int reply;
                            while ((reply = reader.poll()) != 0) {
                                if (reply == Protocol.READY && !ready) {
                                    centerWindowOnNiri(windowTitle);
                                    ready = true;
                                    startupReady.run();
                                }
                            }

                            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(50));
                        }

                        if (helper.isAlive()) {
                            Protocol.close(commands);
                        }
                    }
                } finally {
                    try {
                        if (helper != null) {
                            stopHelper(helper);
                        }
                    } finally {
                        Files.deleteIfExists(helperJar);
                    }
                }
            }

            private static void stopHelper(Process helper) {
                boolean interrupted = false;
                try {
                    if (!helper.waitFor(HELPER_STOP_TIMEOUT_SECOND, TimeUnit.SECONDS)) {
                        helper.destroy();
                        helper.waitFor(HELPER_STOP_TIMEOUT_SECOND, TimeUnit.SECONDS);
                    }
                } catch (InterruptedException ignored) {
                    interrupted = true;
                } finally {
                    if (helper.isAlive()) {
                        helper.destroyForcibly();
                        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(HELPER_STOP_TIMEOUT_SECOND);
                        while (helper.isAlive()) {
                            long remaining = deadline - System.nanoTime();
                            if (remaining <= 0) {
                                break;
                            }
                            try {
                                helper.waitFor(remaining, TimeUnit.NANOSECONDS);
                            } catch (InterruptedException ignored) {
                                interrupted = true;
                            }
                        }
                    }
                    if (interrupted) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            //<editor-fold desc="niri integration">
            private static @Nullable JsonElement niriMessage(String... arguments) {
                List<String> command = new ArrayList<>(List.of("niri", "msg", "--json"));
                command.addAll(List.of(arguments));

                Process message = null;
                try {
                    message = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
                    if (message.waitFor(1, TimeUnit.SECONDS) && message.exitValue() == 0) {
                        try (InputStream input = message.getInputStream()) {
                            return JsonParser.parseString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                        }
                    }
                } catch (IOException | RuntimeException failure) {
                    LOGGER.debug("Could not communicate with niri for pre splash positioning.", failure);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                } finally {
                    if (message != null && message.isAlive()) {
                        message.destroyForcibly();
                    }
                }
                return null;
            }

            private static void centerWindowOnNiri(String windowTitle) {
                if (System.getenv("NIRI_SOCKET") == null) {
                    return;
                }

                try {
                    JsonElement windows = niriMessage("windows");
                    if (windows == null) {
                        return;
                    }

                    JsonObject splash = null;
                    for (JsonElement element : windows.getAsJsonArray()) {
                        JsonObject window = element.getAsJsonObject();
                        JsonElement title = window.get("title");
                        if (title != null
                                && title.isJsonPrimitive()
                                && windowTitle.equals(title.getAsString())
                                && window.get("is_floating").getAsBoolean()) {
                            if (splash != null) {
                                return;
                            }

                            splash = window;
                        }
                    }

                    if (splash == null) {
                        return;
                    }

                    JsonElement workspaces = niriMessage("workspaces");
                    JsonElement outputs = niriMessage("outputs");
                    if (workspaces == null || outputs == null) {
                        return;
                    }

                    for (JsonElement element : workspaces.getAsJsonArray()) {
                        JsonObject workspace = element.getAsJsonObject();
                        if (!workspace.get("id").equals(splash.get("workspace_id")) || workspace.get("output").isJsonNull()) {
                            continue;
                        }

                        JsonObject output = outputs.getAsJsonObject().getAsJsonObject(workspace.get("output").getAsString());
                        JsonObject logical = output.getAsJsonObject("logical");
                        JsonObject layout = splash.getAsJsonObject("layout");
                        var size = layout.getAsJsonArray("tile_size");
                        var position = layout.getAsJsonArray("tile_pos_in_workspace_view");
                        double x = (logical.get("width").getAsDouble() - size.get(0).getAsDouble()) / 2;
                        double y = (logical.get("height").getAsDouble() - size.get(1).getAsDouble()) / 2;

                        niriMessage(
                                "action",
                                "move-floating-window",
                                "--id", splash.get("id").getAsString(),
                                "--x=" + String.format(Locale.ROOT, "%+.3f", x - position.get(0).getAsDouble()),
                                "--y=" + String.format(Locale.ROOT, "%+.3f", y - position.get(1).getAsDouble()));

                        return;
                    }
                } catch (RuntimeException failure) {
                    LOGGER.debug("Could not center Swing pre splash through niri.", failure);
                }
            }
            //</editor-fold>
        }

        /**
         * This is the entry point in the helper JVM.
         */
        public static final class Helper {

            private static final int TARGET_FRAME_RATE = 60;

            private static final DataOutputStream REPLIES = new DataOutputStream(System.out);

            private final SwingSplashRenderer renderer = new SwingSplashRenderer(Helper::signalStartupReady);

            private @Nullable JDialog window;
            private @Nullable Timer timer;

            public static void main(String[] args) {
                try {
                    PreSplashLogger.setSink(Helper::log);
                    ProcessHandle parent = ProcessHandle.of(Long.parseLong(args[0])).orElse(null);
                    if (parent == null) {
                        return;
                    }

                    parent.onExit().thenRun(() -> System.exit(0));

                    try (DataInputStream commands = new DataInputStream(new BufferedInputStream(System.in))) {
                        PreSplashState initial = Protocol.readState(commands);
                        if (initial == null) {
                            return;
                        }

                        Helper[] screen = new Helper[1];
                        SwingUtilities.invokeAndWait(() -> {
                            Helper helper = new Helper();
                            screen[0] = helper;
                            helper.renderer.update(initial);
                            helper.open(args[1]);
                        });

                        Helper helper = screen[0];

                        PreSplashState state;
                        while ((state = Protocol.readState(commands)) != null) {
                            PreSplashState update = state;
                            SwingUtilities.invokeLater(() -> helper.renderer.update(update));
                        }

                        SwingUtilities.invokeLater(helper::close);
                    }
                } catch (Throwable failure) {
                    failure.printStackTrace(System.err);
                    System.exit(1);
                }
            }

            private void open(String windowTitle) {
                var configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                Rectangle bounds = configuration.getBounds();
                float scale = Math.min((float) bounds.width / PreSplashLayout.WIDTH, (float) bounds.height / PreSplashLayout.HEIGHT) * PreSplashLayout.SCREEN_PERCENTAGE;
                int width = Math.max(1, Math.round(PreSplashLayout.WIDTH * scale));
                int height = Math.max(1, Math.round(PreSplashLayout.HEIGHT * scale));

                window = new JDialog((Frame) null, windowTitle, false, configuration);
                window.setType(Window.Type.UTILITY);
                window.setUndecorated(true);
                window.setResizable(false);
                window.setFocusableWindowState(false);
                window.setAutoRequestFocus(false);
                window.setAlwaysOnTop(true);
                window.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                window.setContentPane(renderer);
                window.setSize(width, height);
                window.setLocation(bounds.x + (bounds.width - width) / 2, bounds.y + (bounds.height - height) / 2);
                window.setVisible(true);

                timer = new Timer(1000 / TARGET_FRAME_RATE, _ -> {
                    if (!window.isDisplayable()) {
                        close();
                        return;
                    }
                    renderer.repaint();
                });

                timer.start();
            }

            private static void signalStartupReady() {
                try {
                    Protocol.ready(REPLIES);
                } catch (IOException failure) {
                    failure.printStackTrace(System.err);
                    System.exit(1);
                }
            }

            private static void log(PreSplashLogger.Level level, String message, @Nullable Throwable failure) {
                try {
                    Protocol.log(REPLIES, level, message, failure);
                } catch (IOException error) {
                    System.err.println("[" + level + "] [Pre Splash Renderer]: " + message);
                    if (failure != null) {
                        failure.printStackTrace(System.err);
                    }
                    error.printStackTrace(System.err);
                }
            }

            private void close() {
                if (timer != null) {
                    timer.stop();
                }
                if (window != null) {
                    window.dispose();
                }
                System.exit(0);
            }
        }
    }
    //</editor-fold>
}

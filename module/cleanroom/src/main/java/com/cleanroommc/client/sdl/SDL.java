/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.audio.AudioDevices;
import com.cleanroommc.client.sdl.audio.AudioInternal;
import com.cleanroommc.client.sdl.camera.CameraInternal;
import com.cleanroommc.client.sdl.camera.Cameras;
import com.cleanroommc.client.sdl.hid.Hid;
import com.cleanroommc.client.sdl.hid.HidInternal;
import com.cleanroommc.client.sdl.input.Gamepads;
import com.cleanroommc.client.sdl.input.Haptics;
import com.cleanroommc.client.sdl.input.InputInternal;
import com.cleanroommc.client.sdl.input.Joysticks;
import com.cleanroommc.client.sdl.input.Keyboard;
import com.cleanroommc.client.sdl.input.Mouse;
import com.cleanroommc.client.sdl.input.Pens;
import com.cleanroommc.client.sdl.input.Sensors;
import com.cleanroommc.client.sdl.input.Touches;
import com.cleanroommc.client.sdl.input.virtual.Text;
import com.cleanroommc.client.sdl.video.Displays;
import com.cleanroommc.client.sdl.video.VideoInternal;
import com.cleanroommc.common.CleanroomVersion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLLog;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_LogOutputFunction;
import org.lwjgl.system.JNI;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Platform;
import org.lwjgl.system.linux.DynamicLinkLoader;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The way into SDL. Every device, subsystem and lifecycle call is exposed through here.
 */
public final class SDL {

    private static final Logger LOGGER = LoggerFactory.getLogger("SDL3");
    private static boolean nvidiaWaylandWorkaroundChecked;
    /**
     * Every {@code com.cleanroommc.client.sdl.events} event is posted here, not on Forge's bus.
     * */
    private static final EventBus EVENT_BUS = new EventBus();

    private static int initializedBits;
    private static SDL_LogOutputFunction logOutput;

    private SDL() { }

    public static EventBus events() {
        return EVENT_BUS;
    }

    public static Window window() {
        return Window.main();
    }

    public static Window.Builder createWindow() {
        return Window.builder();
    }

    public static Displays displays() {
        return VideoInternal.displays();
    }

    public static Keyboard keyboard() {
        return InputInternal.keyboard();
    }

    public static Mouse mouse() {
        return InputInternal.mouse();
    }

    public static Text text() {
        Window window = Window.main();
        return window == null ? null : window.text();
    }

    public static Gamepads gamepads() {
        return InputInternal.gamepads();
    }

    public static Joysticks joysticks() {
        return InputInternal.joysticks();
    }

    public static Haptics haptics() {
        return InputInternal.haptics();
    }

    public static Sensors sensors() {
        return InputInternal.sensors();
    }

    public static Touches touches() {
        return InputInternal.touches();
    }

    public static Pens pens() {
        return InputInternal.pens();
    }

    public static Cameras cameras() {
        return CameraInternal.cameras();
    }

    public static AudioDevices audio() {
        return AudioInternal.audio();
    }

    public static Hid hid() {
        return HidInternal.hid();
    }

    public static Clipboard clipboard() {
        return Clipboard.INSTANCE;
    }

    public static FileDialogs dialogs() {
        return FileDialogs.INSTANCE;
    }

    public static Desktop desktop() {
        return Desktop.INSTANCE;
    }

    public static FileSystem files() {
        return FileSystem.INSTANCE;
    }

    public static RuntimeInfo runtime() {
        return RuntimeInfo.INSTANCE;
    }

    public static Power power() {
        return Power.query();
    }

    public static SystemTheme theme() {
        return SystemTheme.current();
    }

    public static Tray createTray(BufferedImage icon, String tooltip) {
        return Tray.create(icon, tooltip);
    }

    /**
     * Brings up the video and event subsystems, if they are not already up.
     */
    public static void ensureVideo() {
        ensureSubsystem(SDLInit.SDL_INIT_VIDEO | SDLInit.SDL_INIT_EVENTS);
        applyNvidiaWaylandWorkaround();
    }

    /*
     * On Wayland with PRIME render offload, NVIDIA's driver rejects the first eglSwapBuffers with
     * EGL_BAD_DISPLAY once Threaded Optimizations are on, even though the EGLDisplay is valid. The
     * driver reads the knob while it initializes the EGL display inside SDL_CreateWindow, so it has
     * to be in the environment before any window exists. The optimization is off by default, but the
     * driver's application profile turns it on for anything that loads liblwjgl*.so before EGL is
     * initialized, which is why this is needed here at all.
     *
     * <p>Only runs when PRIME offload was requested, so other Wayland and NVIDIA setups are left
     * alone. Opt out with {@code -Dcleanroom.sdl.nvidiaWaylandWorkaround=false}.
     */
    private static synchronized void applyNvidiaWaylandWorkaround() {
        if (nvidiaWaylandWorkaroundChecked) {
            return;
        }
        nvidiaWaylandWorkaroundChecked = true;
        if (!Boolean.parseBoolean(System.getProperty("cleanroom.sdl.nvidiaWaylandWorkaround", "true"))) {
            return;
        }
        if (Platform.get() != Platform.LINUX) {
            return;
        }
        if (!"wayland".equals(SDLVideo.SDL_GetCurrentVideoDriver())) {
            return;
        }
        if (!Files.isRegularFile(Path.of("/proc/driver/nvidia/version"))) {
            return;
        }
        if (!nvidiaOffloadRequested()) {
            return;
        }
        String threaded = System.getenv("__GL_THREADED_OPTIMIZATIONS");
        if (threaded != null && ("0".equals(threaded.trim()) || threaded.isBlank())) {
            return;
        }
        if (setEnvironmentVariable("__GL_THREADED_OPTIMIZATIONS", "0") != 0) {
            LOGGER.warn("Could not set __GL_THREADED_OPTIMIZATIONS, so the Wayland swap may keep failing");
            return;
        }
        if (threaded != null) {
            LOGGER.warn(
                "Overriding __GL_THREADED_OPTIMIZATIONS={} because it breaks EGL on Wayland with PRIME offload; "
                    + "set -Dcleanroom.sdl.nvidiaWaylandWorkaround=false to keep it",
                threaded
            );
        } else {
            LOGGER.info("Disabled NVIDIA Threaded Optimizations for the SDL Wayland window");
        }
    }

    private static boolean nvidiaOffloadRequested() {
        String offload = System.getenv("__NV_PRIME_RENDER_OFFLOAD");
        if (offload != null && !offload.isBlank() && !"0".equals(offload.trim())) {
            return true;
        }
        String vendor = System.getenv("__GLX_VENDOR_LIBRARY_NAME");
        return vendor != null && "nvidia".equalsIgnoreCase(vendor.trim());
    }

    /**
     * Calls SDL's {@code setenv} wrapper with overwrite enabled, so the driver sees the value when it
     * reads the environment.
     *
     * <p>LWJGL maps SDL's {@code overwrite} parameter to an {@code int}: {@code 0} keeps an existing
     * value and {@code 1} replaces it.
     *
     * @param name the variable to set
     * @param value the value to store
     * @return 0 on success, non-zero otherwise
     */
    private static int setEnvironmentVariable(String name, String value) {
        return SDLStdinc.SDL_setenv_unsafe(name, value, 1);
    }

    /**
     * Initializes whichever SDL {@code flags} is passed through the argument.
     *
     * <p>SDL delivers nothing for a subsystem that was never initialized.
     *
     * @param flags one or more {@code SDL_INIT_*} bits
     */
    public static synchronized void ensureSubsystem(int flags) {
        int missing = flags & ~initializedBits;
        if (missing == 0) {
            return;
        }
        installLog();
        prepare(missing);
        check(SDLInit.SDL_InitSubSystem(missing), "SDL_InitSubSystem");
        initializedBits |= missing;
    }

    /**
     * Sets an SDL hint. Some hints are ignored once the matching subsystem is up.
     */
    public static void hint(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException("Hint name and value cannot be null");
        }
        check(SDLHints.SDL_SetHint(name, value), "SDL_SetHint(" + name + ")");
    }

    /**
     * @param flags one or more {@code SDL_INIT_*} bits
     * @return whether every one of them is up
     */
    public static synchronized boolean isInitialized(int flags) {
        return (initializedBits & flags) == flags;
    }

    /** Closes the host window and tears down every subsystem this class brought up. */
    public static synchronized void shutdown() {
        Lifecycle.reset();
        CleanroomWindowBridge.uninstall();
        Window.closeMain();
        if (initializedBits == 0) {
            return;
        }
        boolean wayland = "wayland".equals(SDLVideo.SDL_GetCurrentVideoDriver());
        SDLInit.SDL_Quit();
        initializedBits = 0;
        if (wayland && Platform.get() == Platform.LINUX) {
            skipNativeExitHandlers();
        }
    }

    /**
     * @param result the {@code boolean} an SDL call returned
     * @param call the call's name, for the message
     * @throws SDLException if the call failed
     */
    public static void check(boolean result, String call) {
        if (!result) {
            throw new SDLException(call + " failed: " + SDLError.SDL_GetError());
        }
    }

    /**
     * Logs a failed optional SDL call at {@code debug} rather than throwing {@link SDLException}.
     *
     * @param result the {@code boolean} an SDL call returned
     * @param call the call's name, for the message
     * @return {@code result}
     */
    public static boolean tryCheck(boolean result, String call) {
        if (!result) {
            LOGGER.debug("{} failed: {}", call, SDLError.SDL_GetError());
        }
        return result;
    }

    /**
     * @param handle a pointer an SDL call returned
     * @param call the call's name, for the message
     * @return {@code handle}
     * @throws SDLException if the call returned null
     */
    public static long checkHandle(long handle, String call) {
        if (handle == 0L) {
            throw new SDLException(call + " failed: " + SDLError.SDL_GetError());
        }
        return handle;
    }

    /**
     * Hints that must be set before a subsystem comes up, plus app metadata for the video path.
     */
    private static void prepare(int missing) {
        if ((missing & SDLInit.SDL_INIT_VIDEO) != 0) {
            Text.implementIME(true, true);
            check(
                SDLInit.SDL_SetAppMetadata("Cleanroom", CleanroomVersion.VERSION, "com.cleanroommc.cleanroom"),
                "SDL_SetAppMetadata"
            );
            check(
                SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_TYPE_STRING, "game"),
                "SDL_SetAppMetadataProperty"
            );
        }
    }

    /*
     * JVM calls exit() off the main thread. With two or more EGL contexts created, NVIDIA's EGL atexit
     * teardown races its worker threads and segfaults. Registered last, _exit runs first and ends the
     * process with the JVM's status once shutdown hooks are done.
     * on_exit is glibc only.
     */
    private static void skipNativeExitHandlers() {
        long self = DynamicLinkLoader.dlopen((CharSequence) null, DynamicLinkLoader.RTLD_LAZY);
        long onExit = DynamicLinkLoader.dlsym(self, "on_exit");
        long exit = DynamicLinkLoader.dlsym(self, "_exit");
        if (onExit != 0L && exit != 0L) {
            JNI.invokePPI(exit, 0L, onExit);
        }
    }

    private static void installLog() {
        if (logOutput != null) {
            return;
        }
        logOutput = SDL_LogOutputFunction.create((_, category, priority, message) -> {
            String text = MemoryUtil.memUTF8(message);
            if (priority >= SDLLog.SDL_LOG_PRIORITY_ERROR) {
                LOGGER.error("[{}] {}", category, text);
            } else if (priority == SDLLog.SDL_LOG_PRIORITY_WARN) {
                LOGGER.warn("[{}] {}", category, text);
            } else if (priority == SDLLog.SDL_LOG_PRIORITY_INFO) {
                LOGGER.info("[{}] {}", category, text);
            } else {
                LOGGER.debug("[{}] {}", category, text);
            }
        });
        SDLLog.SDL_SetLogOutputFunction(logOutput, 0L);
    }

}

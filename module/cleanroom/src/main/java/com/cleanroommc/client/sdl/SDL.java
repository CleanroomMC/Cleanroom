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
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLHints;
import org.lwjgl.sdl.SDLInit;
import org.lwjgl.sdl.SDLLog;
import org.lwjgl.sdl.SDL_LogOutputFunction;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * The way into SDL. Every device, subsystem and lifecycle call is exposed through here.
 */
public final class SDL {

    /**
     * Every {@code com.cleanroommc.client.sdl.events} event is posted here, not on Forge's bus.
     * */
    private static final EventBus EVENT_BUS = new EventBus();

    private static int initializedBits;
    private static SDL_LogOutputFunction logOutput;

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
     * Hints that must be set before a subsystem comes up, plus app metadata for the video path.
     */
    private static void prepare(int missing) {
        if ((missing & SDLInit.SDL_INIT_VIDEO) != 0) {
            Text.implementIME(true, true);
            check(SDLInit.SDL_SetAppMetadata("Cleanroom", CleanroomVersion.VERSION, "com.cleanroommc.cleanroom"),
                    "SDL_SetAppMetadata");
            check(SDLInit.SDL_SetAppMetadataProperty(SDLInit.SDL_PROP_APP_METADATA_TYPE_STRING, "game"),
                    "SDL_SetAppMetadataProperty");
        }
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
        SDLInit.SDL_Quit();
        initializedBits = 0;
    }

    private static void installLog() {
        if (logOutput != null) {
            return;
        }
        Logger logger = LoggerFactory.getLogger("SDL3");
        logOutput = SDL_LogOutputFunction.create((_, category, priority, message) -> {
            String text = MemoryUtil.memUTF8(message);
            if (priority >= SDLLog.SDL_LOG_PRIORITY_ERROR) {
                logger.error("[{}] {}", category, text);
            } else if (priority == SDLLog.SDL_LOG_PRIORITY_WARN) {
                logger.warn("[{}] {}", category, text);
            } else if (priority == SDLLog.SDL_LOG_PRIORITY_INFO) {
                logger.info("[{}] {}", category, text);
            } else {
                logger.debug("[{}] {}", category, text);
            }
        });
        SDLLog.SDL_SetLogOutputFunction(logOutput, 0L);
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

    private SDL() { }

}

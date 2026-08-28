package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.input.virtual.Text;
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

/**
 * SDL's lifecycle helpers.
 */
public final class SDL {

    /**
     * Every {@code com.cleanroommc.client.sdl.events} event is posted here, not on Forge's bus.
     * */
    public static final EventBus EVENT_BUS = new EventBus();

    private static int initializedBits;
    private static SDL_LogOutputFunction logOutput;

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
        Devices.reset();
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

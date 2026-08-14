package com.cleanroommc.client.input;

import org.lwjgl.sdl.SDLError;
import org.lwjgl.sdl.SDLInit;

/**
 * SDL's lifecycle helpers.
 */
public final class SDL {

    private static int initializedBits;

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
        check(SDLInit.SDL_InitSubSystem(missing), "SDL_InitSubSystem");
        initializedBits |= missing;
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
        if (initializedBits == 0) {
            return;
        }
        // TODO: new SDL input stack
        // Inputs.reset();
        Window.closeMain();
        SDLInit.SDL_Quit();
        initializedBits = 0;
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

    public static final class SDLException extends RuntimeException {

        SDLException(String message) {
            super(message);
        }

    }

}

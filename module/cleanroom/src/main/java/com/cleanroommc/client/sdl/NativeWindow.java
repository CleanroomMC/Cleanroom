package com.cleanroommc.client.sdl;

/**
 * The platform window behind Cleanroom's SDL window.
 *
 * @param compositor which window system {@code handle} belongs to
 * @param handle HWND, wl_surface, X11 Window, NSWindow, and so on; {@code 0} when unknown
 */
public record NativeWindow(Compositor compositor, long handle) {

    public static final NativeWindow UNKNOWN = new NativeWindow(Compositor.UNKNOWN, 0L);

    public boolean known() {
        return compositor != Compositor.UNKNOWN && handle != 0L;
    }

}

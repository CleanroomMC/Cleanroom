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

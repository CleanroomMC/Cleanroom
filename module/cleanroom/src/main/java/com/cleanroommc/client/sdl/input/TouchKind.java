/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLTouch;

/** How a touch device reports coordinates. */
public enum TouchKind {

    INVALID(SDLTouch.SDL_TOUCH_DEVICE_INVALID),
    DIRECT(SDLTouch.SDL_TOUCH_DEVICE_DIRECT),
    INDIRECT_ABSOLUTE(SDLTouch.SDL_TOUCH_DEVICE_INDIRECT_ABSOLUTE),
    INDIRECT_RELATIVE(SDLTouch.SDL_TOUCH_DEVICE_INDIRECT_RELATIVE);

    private final int value;

    TouchKind(int value) {
        this.value = value;
    }

    public static TouchKind of(int value) {
        return switch (value) {
            case SDLTouch.SDL_TOUCH_DEVICE_DIRECT -> DIRECT;
            case SDLTouch.SDL_TOUCH_DEVICE_INDIRECT_ABSOLUTE -> INDIRECT_ABSOLUTE;
            case SDLTouch.SDL_TOUCH_DEVICE_INDIRECT_RELATIVE -> INDIRECT_RELATIVE;
            default -> INVALID;
        };
    }

    public int value() {
        return value;
    }

}

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

import org.lwjgl.sdl.SDLPen;

/** Direct (display) vs indirect (tablet) pens. */
public enum PenType {

    INVALID(SDLPen.SDL_PEN_DEVICE_TYPE_INVALID),
    UNKNOWN(SDLPen.SDL_PEN_DEVICE_TYPE_UNKNOWN),
    DIRECT(SDLPen.SDL_PEN_DEVICE_TYPE_DIRECT),
    INDIRECT(SDLPen.SDL_PEN_DEVICE_TYPE_INDIRECT);

    private final int value;

    PenType(int value) {
        this.value = value;
    }

    public static PenType of(int value) {
        return switch (value) {
            case SDLPen.SDL_PEN_DEVICE_TYPE_INVALID -> INVALID;
            case SDLPen.SDL_PEN_DEVICE_TYPE_DIRECT -> DIRECT;
            case SDLPen.SDL_PEN_DEVICE_TYPE_INDIRECT -> INDIRECT;
            default -> UNKNOWN;
        };
    }

    public int value() {
        return value;
    }

}

package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLPen;

/** Direct (display) vs indirect (tablet) pens. */
public enum PenType {

    INVALID(SDLPen.SDL_PEN_DEVICE_TYPE_INVALID),
    UNKNOWN(SDLPen.SDL_PEN_DEVICE_TYPE_UNKNOWN),
    DIRECT(SDLPen.SDL_PEN_DEVICE_TYPE_DIRECT),
    INDIRECT(SDLPen.SDL_PEN_DEVICE_TYPE_INDIRECT);

    public static PenType of(int value) {
        return switch (value) {
            case SDLPen.SDL_PEN_DEVICE_TYPE_INVALID -> INVALID;
            case SDLPen.SDL_PEN_DEVICE_TYPE_DIRECT -> DIRECT;
            case SDLPen.SDL_PEN_DEVICE_TYPE_INDIRECT -> INDIRECT;
            default -> UNKNOWN;
        };
    }

    private final int value;

    PenType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

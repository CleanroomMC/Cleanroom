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

    public int value() {
        return value;
    }

    public static TouchKind of(int value) {
        return switch (value) {
            case SDLTouch.SDL_TOUCH_DEVICE_DIRECT -> DIRECT;
            case SDLTouch.SDL_TOUCH_DEVICE_INDIRECT_ABSOLUTE -> INDIRECT_ABSOLUTE;
            case SDLTouch.SDL_TOUCH_DEVICE_INDIRECT_RELATIVE -> INDIRECT_RELATIVE;
            default -> INVALID;
        };
    }

}

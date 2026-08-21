package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLMouse;

/** SDL mouse buttons. */
public enum MouseButton {

    LEFT(SDLMouse.SDL_BUTTON_LEFT),
    MIDDLE(SDLMouse.SDL_BUTTON_MIDDLE),
    RIGHT(SDLMouse.SDL_BUTTON_RIGHT),
    X1(SDLMouse.SDL_BUTTON_X1),
    X2(SDLMouse.SDL_BUTTON_X2);

    public static MouseButton of(int value) {
        return switch (value) {
            case SDLMouse.SDL_BUTTON_LEFT -> LEFT;
            case SDLMouse.SDL_BUTTON_MIDDLE -> MIDDLE;
            case SDLMouse.SDL_BUTTON_RIGHT -> RIGHT;
            case SDLMouse.SDL_BUTTON_X1 -> X1;
            case SDLMouse.SDL_BUTTON_X2 -> X2;
            default -> null;
        };
    }

    private final int value;

    MouseButton(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

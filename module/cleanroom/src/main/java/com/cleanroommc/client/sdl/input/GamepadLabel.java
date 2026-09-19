package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLGamepad;

/** Face-button glyph for the pad's own layout (A vs Cross, etc.). */
public enum GamepadLabel {

    UNKNOWN(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_UNKNOWN),
    A(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_A),
    B(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_B),
    X(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_X),
    Y(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_Y),
    CROSS(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_CROSS),
    CIRCLE(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_CIRCLE),
    SQUARE(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_SQUARE),
    TRIANGLE(SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_TRIANGLE);

    public static GamepadLabel of(int value) {
        return switch (value) {
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_A -> A;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_B -> B;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_X -> X;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_Y -> Y;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_CROSS -> CROSS;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_CIRCLE -> CIRCLE;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_SQUARE -> SQUARE;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LABEL_TRIANGLE -> TRIANGLE;
            default -> UNKNOWN;
        };
    }

    private final int value;

    GamepadLabel(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

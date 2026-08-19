package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLGamepad;

/** SDL gamepad axes. */
public enum GamepadAxis {

    LEFT_X(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX),
    LEFT_Y(SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY),
    RIGHT_X(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX),
    RIGHT_Y(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY),
    LEFT_TRIGGER(SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER),
    RIGHT_TRIGGER(SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER);

    public static GamepadAxis of(int value) {
        return switch (value) {
            case SDLGamepad.SDL_GAMEPAD_AXIS_LEFTX -> LEFT_X;
            case SDLGamepad.SDL_GAMEPAD_AXIS_LEFTY -> LEFT_Y;
            case SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTX -> RIGHT_X;
            case SDLGamepad.SDL_GAMEPAD_AXIS_RIGHTY -> RIGHT_Y;
            case SDLGamepad.SDL_GAMEPAD_AXIS_LEFT_TRIGGER -> LEFT_TRIGGER;
            case SDLGamepad.SDL_GAMEPAD_AXIS_RIGHT_TRIGGER -> RIGHT_TRIGGER;
            default -> null;
        };
    }

    private final int value;

    GamepadAxis(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

    public boolean trigger() {
        return this == LEFT_TRIGGER || this == RIGHT_TRIGGER;
    }

}

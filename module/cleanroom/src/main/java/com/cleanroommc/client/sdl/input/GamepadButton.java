package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLGamepad;

/** SDL gamepad buttons, face-neutral ({@code SOUTH} is A on Xbox and Cross on PlayStation). */
public enum GamepadButton {

    SOUTH(SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH),
    EAST(SDLGamepad.SDL_GAMEPAD_BUTTON_EAST),
    WEST(SDLGamepad.SDL_GAMEPAD_BUTTON_WEST),
    NORTH(SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH),
    BACK(SDLGamepad.SDL_GAMEPAD_BUTTON_BACK),
    GUIDE(SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE),
    START(SDLGamepad.SDL_GAMEPAD_BUTTON_START),
    LEFT_STICK(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK),
    RIGHT_STICK(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK),
    LEFT_SHOULDER(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER),
    RIGHT_SHOULDER(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER),
    DPAD_UP(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP),
    DPAD_DOWN(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN),
    DPAD_LEFT(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT),
    DPAD_RIGHT(SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT),
    MISC1(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC1),
    RIGHT_PADDLE1(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1),
    LEFT_PADDLE1(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE1),
    RIGHT_PADDLE2(SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2),
    LEFT_PADDLE2(SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE2),
    TOUCHPAD(SDLGamepad.SDL_GAMEPAD_BUTTON_TOUCHPAD),
    MISC2(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC2),
    MISC3(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC3),
    MISC4(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC4),
    MISC5(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC5),
    MISC6(SDLGamepad.SDL_GAMEPAD_BUTTON_MISC6);

    public static GamepadButton of(int value) {
        return switch (value) {
            case SDLGamepad.SDL_GAMEPAD_BUTTON_SOUTH -> SOUTH;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_EAST -> EAST;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_WEST -> WEST;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_NORTH -> NORTH;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_BACK -> BACK;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_GUIDE -> GUIDE;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_START -> START;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_STICK -> LEFT_STICK;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_STICK -> RIGHT_STICK;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_SHOULDER -> LEFT_SHOULDER;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_SHOULDER -> RIGHT_SHOULDER;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_UP -> DPAD_UP;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_DOWN -> DPAD_DOWN;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_LEFT -> DPAD_LEFT;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_DPAD_RIGHT -> DPAD_RIGHT;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_MISC1 -> MISC1;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE1 -> RIGHT_PADDLE1;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE1 -> LEFT_PADDLE1;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_RIGHT_PADDLE2 -> RIGHT_PADDLE2;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_LEFT_PADDLE2 -> LEFT_PADDLE2;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_TOUCHPAD -> TOUCHPAD;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_MISC2 -> MISC2;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_MISC3 -> MISC3;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_MISC4 -> MISC4;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_MISC5 -> MISC5;
            case SDLGamepad.SDL_GAMEPAD_BUTTON_MISC6 -> MISC6;
            default -> null;
        };
    }

    private final int value;

    GamepadButton(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

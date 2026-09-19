package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLJoystick;

/** How a joystick is attached. */
public enum JoystickConnection {

    INVALID(SDLJoystick.SDL_JOYSTICK_CONNECTION_INVALID),
    UNKNOWN(SDLJoystick.SDL_JOYSTICK_CONNECTION_UNKNOWN),
    WIRED(SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRED),
    WIRELESS(SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRELESS);

    public static JoystickConnection of(int value) {
        return switch (value) {
            case SDLJoystick.SDL_JOYSTICK_CONNECTION_INVALID -> INVALID;
            case SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRED -> WIRED;
            case SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRELESS -> WIRELESS;
            default -> UNKNOWN;
        };
    }

    private final int value;

    JoystickConnection(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

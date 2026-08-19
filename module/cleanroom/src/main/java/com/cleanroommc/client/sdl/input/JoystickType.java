package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLJoystick;

/** SDL's idea of what a raw joystick is. */
public enum JoystickType {

    UNKNOWN(SDLJoystick.SDL_JOYSTICK_TYPE_UNKNOWN),
    GAMEPAD(SDLJoystick.SDL_JOYSTICK_TYPE_GAMEPAD),
    WHEEL(SDLJoystick.SDL_JOYSTICK_TYPE_WHEEL),
    ARCADE_STICK(SDLJoystick.SDL_JOYSTICK_TYPE_ARCADE_STICK),
    FLIGHT_STICK(SDLJoystick.SDL_JOYSTICK_TYPE_FLIGHT_STICK),
    DANCE_PAD(SDLJoystick.SDL_JOYSTICK_TYPE_DANCE_PAD),
    GUITAR(SDLJoystick.SDL_JOYSTICK_TYPE_GUITAR),
    DRUM_KIT(SDLJoystick.SDL_JOYSTICK_TYPE_DRUM_KIT),
    ARCADE_PAD(SDLJoystick.SDL_JOYSTICK_TYPE_ARCADE_PAD),
    THROTTLE(SDLJoystick.SDL_JOYSTICK_TYPE_THROTTLE);

    public static JoystickType of(int value) {
        return switch (value) {
            case SDLJoystick.SDL_JOYSTICK_TYPE_GAMEPAD -> GAMEPAD;
            case SDLJoystick.SDL_JOYSTICK_TYPE_WHEEL -> WHEEL;
            case SDLJoystick.SDL_JOYSTICK_TYPE_ARCADE_STICK -> ARCADE_STICK;
            case SDLJoystick.SDL_JOYSTICK_TYPE_FLIGHT_STICK -> FLIGHT_STICK;
            case SDLJoystick.SDL_JOYSTICK_TYPE_DANCE_PAD -> DANCE_PAD;
            case SDLJoystick.SDL_JOYSTICK_TYPE_GUITAR -> GUITAR;
            case SDLJoystick.SDL_JOYSTICK_TYPE_DRUM_KIT -> DRUM_KIT;
            case SDLJoystick.SDL_JOYSTICK_TYPE_ARCADE_PAD -> ARCADE_PAD;
            case SDLJoystick.SDL_JOYSTICK_TYPE_THROTTLE -> THROTTLE;
            default -> UNKNOWN;
        };
    }

    private final int value;

    JoystickType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

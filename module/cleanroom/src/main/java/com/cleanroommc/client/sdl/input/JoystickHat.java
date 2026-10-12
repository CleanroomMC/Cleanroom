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

import org.lwjgl.sdl.SDLJoystick;

/** One hat position. Diagonals are their own values, not a combination of the four-way names. */
public enum JoystickHat {

    CENTERED(SDLJoystick.SDL_HAT_CENTERED),
    UP(SDLJoystick.SDL_HAT_UP),
    RIGHT(SDLJoystick.SDL_HAT_RIGHT),
    DOWN(SDLJoystick.SDL_HAT_DOWN),
    LEFT(SDLJoystick.SDL_HAT_LEFT),
    RIGHT_UP(SDLJoystick.SDL_HAT_RIGHTUP),
    RIGHT_DOWN(SDLJoystick.SDL_HAT_RIGHTDOWN),
    LEFT_UP(SDLJoystick.SDL_HAT_LEFTUP),
    LEFT_DOWN(SDLJoystick.SDL_HAT_LEFTDOWN);

    private final int value;

    JoystickHat(int value) {
        this.value = value;
    }

    public static JoystickHat of(int value) {
        return switch (value) {
            case SDLJoystick.SDL_HAT_UP -> UP;
            case SDLJoystick.SDL_HAT_RIGHT -> RIGHT;
            case SDLJoystick.SDL_HAT_DOWN -> DOWN;
            case SDLJoystick.SDL_HAT_LEFT -> LEFT;
            case SDLJoystick.SDL_HAT_RIGHTUP -> RIGHT_UP;
            case SDLJoystick.SDL_HAT_RIGHTDOWN -> RIGHT_DOWN;
            case SDLJoystick.SDL_HAT_LEFTUP -> LEFT_UP;
            case SDLJoystick.SDL_HAT_LEFTDOWN -> LEFT_DOWN;
            default -> CENTERED;
        };
    }

    public int value() {
        return value;
    }

    public boolean up() {
        return (value & SDLJoystick.SDL_HAT_UP) != 0;
    }

    public boolean down() {
        return (value & SDLJoystick.SDL_HAT_DOWN) != 0;
    }

    public boolean left() {
        return (value & SDLJoystick.SDL_HAT_LEFT) != 0;
    }

    public boolean right() {
        return (value & SDLJoystick.SDL_HAT_RIGHT) != 0;
    }

}

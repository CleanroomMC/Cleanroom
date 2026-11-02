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

/** How a joystick is attached. */
public enum JoystickConnection {

    INVALID(SDLJoystick.SDL_JOYSTICK_CONNECTION_INVALID),
    UNKNOWN(SDLJoystick.SDL_JOYSTICK_CONNECTION_UNKNOWN),
    WIRED(SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRED),
    WIRELESS(SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRELESS);

    private final int value;

    JoystickConnection(int value) {
        this.value = value;
    }

    public static JoystickConnection of(int value) {
        return switch (value) {
            case SDLJoystick.SDL_JOYSTICK_CONNECTION_INVALID -> INVALID;
            case SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRED -> WIRED;
            case SDLJoystick.SDL_JOYSTICK_CONNECTION_WIRELESS -> WIRELESS;
            default -> UNKNOWN;
        };
    }

    public int value() {
        return value;
    }

}

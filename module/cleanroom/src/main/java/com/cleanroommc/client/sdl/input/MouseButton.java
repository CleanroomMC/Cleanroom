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

import org.lwjgl.sdl.SDLMouse;

/** SDL mouse buttons. */
public enum MouseButton {

    LEFT(SDLMouse.SDL_BUTTON_LEFT),
    MIDDLE(SDLMouse.SDL_BUTTON_MIDDLE),
    RIGHT(SDLMouse.SDL_BUTTON_RIGHT),
    X1(SDLMouse.SDL_BUTTON_X1),
    X2(SDLMouse.SDL_BUTTON_X2);

    private final int value;
    private final int mask;

    MouseButton(int value) {
        this.value = value;
        this.mask = 1 << value - 1;
    }

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

    public int value() {
        return value;
    }

    public int mask() {
        return mask;
    }

}

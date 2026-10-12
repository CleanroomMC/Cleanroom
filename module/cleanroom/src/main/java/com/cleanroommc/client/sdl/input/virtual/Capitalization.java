/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.input.virtual;

import org.lwjgl.sdl.SDLKeyboard;

/** How the on-screen keyboard capitalizes incoming text. */
public enum Capitalization {

    NONE(SDLKeyboard.SDL_CAPITALIZE_NONE),
    SENTENCES(SDLKeyboard.SDL_CAPITALIZE_SENTENCES),
    WORDS(SDLKeyboard.SDL_CAPITALIZE_WORDS),
    LETTERS(SDLKeyboard.SDL_CAPITALIZE_LETTERS);

    private final int value;

    Capitalization(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

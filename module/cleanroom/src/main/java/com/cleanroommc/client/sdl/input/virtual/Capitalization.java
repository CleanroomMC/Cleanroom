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

package com.cleanroommc.client.sdl.input.virtual;

import org.lwjgl.sdl.SDLKeyboard;

public enum TextInputKind {

    TEXT(SDLKeyboard.SDL_TEXTINPUT_TYPE_TEXT),
    NAME(SDLKeyboard.SDL_TEXTINPUT_TYPE_TEXT_NAME),
    EMAIL(SDLKeyboard.SDL_TEXTINPUT_TYPE_TEXT_EMAIL),
    USERNAME(SDLKeyboard.SDL_TEXTINPUT_TYPE_TEXT_USERNAME),
    PASSWORD_HIDDEN(SDLKeyboard.SDL_TEXTINPUT_TYPE_TEXT_PASSWORD_HIDDEN),
    PASSWORD_VISIBLE(SDLKeyboard.SDL_TEXTINPUT_TYPE_TEXT_PASSWORD_VISIBLE),
    NUMBER(SDLKeyboard.SDL_TEXTINPUT_TYPE_NUMBER),
    NUMBER_PASSWORD_HIDDEN(SDLKeyboard.SDL_TEXTINPUT_TYPE_NUMBER_PASSWORD_HIDDEN),
    NUMBER_PASSWORD_VISIBLE(SDLKeyboard.SDL_TEXTINPUT_TYPE_NUMBER_PASSWORD_VISIBLE);

    private final int value;

    TextInputKind(int value) {
        this.value = value;
    }

    int value() {
        return this.value;
    }

}

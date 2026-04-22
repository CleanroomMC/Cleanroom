package com.cleanroommc.client;

import org.lwjgl.opengl.Display;
import org.lwjgl.sdl.SDLKeyboard;

public class IMEHandler {
    public static void setIME(boolean active) {
        if (active) {
            SDLKeyboard.SDL_StartTextInput(Display.getWindow());
        } else {
            SDLKeyboard.SDL_StopTextInput(Display.getWindow());
        }
    }
}

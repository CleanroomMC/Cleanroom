package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLGamepad;

/** SDL's idea of which family a pad belongs to. */
public enum GamepadType {

    UNKNOWN(SDLGamepad.SDL_GAMEPAD_TYPE_UNKNOWN),
    STANDARD(SDLGamepad.SDL_GAMEPAD_TYPE_STANDARD),
    XBOX360(SDLGamepad.SDL_GAMEPAD_TYPE_XBOX360),
    XBOXONE(SDLGamepad.SDL_GAMEPAD_TYPE_XBOXONE),
    PS3(SDLGamepad.SDL_GAMEPAD_TYPE_PS3),
    PS4(SDLGamepad.SDL_GAMEPAD_TYPE_PS4),
    PS5(SDLGamepad.SDL_GAMEPAD_TYPE_PS5),
    NINTENDO_SWITCH_PRO(SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_PRO),
    NINTENDO_SWITCH_JOYCON_LEFT(SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_LEFT),
    NINTENDO_SWITCH_JOYCON_RIGHT(SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_RIGHT),
    NINTENDO_SWITCH_JOYCON_PAIR(SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_PAIR),
    GAMECUBE(SDLGamepad.SDL_GAMEPAD_TYPE_GAMECUBE);

    public static GamepadType of(int value) {
        return switch (value) {
            case SDLGamepad.SDL_GAMEPAD_TYPE_STANDARD -> STANDARD;
            case SDLGamepad.SDL_GAMEPAD_TYPE_XBOX360 -> XBOX360;
            case SDLGamepad.SDL_GAMEPAD_TYPE_XBOXONE -> XBOXONE;
            case SDLGamepad.SDL_GAMEPAD_TYPE_PS3 -> PS3;
            case SDLGamepad.SDL_GAMEPAD_TYPE_PS4 -> PS4;
            case SDLGamepad.SDL_GAMEPAD_TYPE_PS5 -> PS5;
            case SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_PRO -> NINTENDO_SWITCH_PRO;
            case SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_LEFT -> NINTENDO_SWITCH_JOYCON_LEFT;
            case SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_RIGHT -> NINTENDO_SWITCH_JOYCON_RIGHT;
            case SDLGamepad.SDL_GAMEPAD_TYPE_NINTENDO_SWITCH_JOYCON_PAIR -> NINTENDO_SWITCH_JOYCON_PAIR;
            case SDLGamepad.SDL_GAMEPAD_TYPE_GAMECUBE -> GAMECUBE;
            default -> UNKNOWN;
        };
    }

    private final int value;

    GamepadType(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

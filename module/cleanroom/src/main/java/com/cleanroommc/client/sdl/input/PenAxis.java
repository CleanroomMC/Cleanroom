package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLPen;

/** Axes a drawing tablet reports. */
public enum PenAxis {

    PRESSURE(SDLPen.SDL_PEN_AXIS_PRESSURE),
    X_TILT(SDLPen.SDL_PEN_AXIS_XTILT),
    Y_TILT(SDLPen.SDL_PEN_AXIS_YTILT),
    DISTANCE(SDLPen.SDL_PEN_AXIS_DISTANCE),
    ROTATION(SDLPen.SDL_PEN_AXIS_ROTATION),
    SLIDER(SDLPen.SDL_PEN_AXIS_SLIDER),
    TANGENTIAL_PRESSURE(SDLPen.SDL_PEN_AXIS_TANGENTIAL_PRESSURE);

    public static PenAxis of(int value) {
        return switch (value) {
            case SDLPen.SDL_PEN_AXIS_PRESSURE -> PRESSURE;
            case SDLPen.SDL_PEN_AXIS_XTILT -> X_TILT;
            case SDLPen.SDL_PEN_AXIS_YTILT -> Y_TILT;
            case SDLPen.SDL_PEN_AXIS_DISTANCE -> DISTANCE;
            case SDLPen.SDL_PEN_AXIS_ROTATION -> ROTATION;
            case SDLPen.SDL_PEN_AXIS_SLIDER -> SLIDER;
            case SDLPen.SDL_PEN_AXIS_TANGENTIAL_PRESSURE -> TANGENTIAL_PRESSURE;
            default -> null;
        };
    }

    private final int value;

    PenAxis(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

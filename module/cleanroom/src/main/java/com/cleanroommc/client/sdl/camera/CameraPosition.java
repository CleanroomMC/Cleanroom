package com.cleanroommc.client.sdl.camera;

import org.lwjgl.sdl.SDLCamera;

import java.util.HashMap;
import java.util.Map;

/** Front/back/unknown, as the OS reports it. */
public enum CameraPosition {

    UNKNOWN(SDLCamera.SDL_CAMERA_POSITION_UNKNOWN),
    FRONT(SDLCamera.SDL_CAMERA_POSITION_FRONT_FACING),
    BACK(SDLCamera.SDL_CAMERA_POSITION_BACK_FACING);

    public static CameraPosition of(int value) {
        return switch (value) {
            case SDLCamera.SDL_CAMERA_POSITION_FRONT_FACING -> FRONT;
            case SDLCamera.SDL_CAMERA_POSITION_BACK_FACING -> BACK;
            default -> UNKNOWN;
        };
    }

    private final int value;

    CameraPosition(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

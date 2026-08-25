package com.cleanroommc.client.sdl.camera;

import org.lwjgl.sdl.SDLCamera;

import java.util.HashMap;
import java.util.Map;

/** OS permission for an opened camera. */
public enum CameraPermission {

    DENIED(SDLCamera.SDL_CAMERA_PERMISSION_STATE_DENIED),
    PENDING(SDLCamera.SDL_CAMERA_PERMISSION_STATE_PENDING),
    APPROVED(SDLCamera.SDL_CAMERA_PERMISSION_STATE_APPROVED);

    public static CameraPermission of(int value) {
        return switch (value) {
            case SDLCamera.SDL_CAMERA_PERMISSION_STATE_DENIED -> DENIED;
            case SDLCamera.SDL_CAMERA_PERMISSION_STATE_APPROVED -> APPROVED;
            default -> PENDING;
        };
    }

    private final int value;

    CameraPermission(int value) {
        this.value = value;
    }

    public int value() {
        return value;
    }

}

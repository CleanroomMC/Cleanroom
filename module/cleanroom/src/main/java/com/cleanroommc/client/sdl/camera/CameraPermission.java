/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.camera;

import org.lwjgl.sdl.SDLCamera;

/** OS permission for an opened camera. */
public enum CameraPermission {

    DENIED(SDLCamera.SDL_CAMERA_PERMISSION_STATE_DENIED),
    PENDING(SDLCamera.SDL_CAMERA_PERMISSION_STATE_PENDING),
    APPROVED(SDLCamera.SDL_CAMERA_PERMISSION_STATE_APPROVED);

    private final int value;

    CameraPermission(int value) {
        this.value = value;
    }

    public static CameraPermission of(int value) {
        return switch (value) {
            case SDLCamera.SDL_CAMERA_PERMISSION_STATE_DENIED -> DENIED;
            case SDLCamera.SDL_CAMERA_PERMISSION_STATE_APPROVED -> APPROVED;
            default -> PENDING;
        };
    }

    public int value() {
        return value;
    }

}

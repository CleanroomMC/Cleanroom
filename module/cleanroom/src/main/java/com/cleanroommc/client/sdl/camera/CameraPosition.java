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

/** Front/back/unknown, as the OS reports it. */
public enum CameraPosition {

    UNKNOWN(SDLCamera.SDL_CAMERA_POSITION_UNKNOWN),
    FRONT(SDLCamera.SDL_CAMERA_POSITION_FRONT_FACING),
    BACK(SDLCamera.SDL_CAMERA_POSITION_BACK_FACING);

    private final int value;

    CameraPosition(int value) {
        this.value = value;
    }

    public static CameraPosition of(int value) {
        return switch (value) {
            case SDLCamera.SDL_CAMERA_POSITION_FRONT_FACING -> FRONT;
            case SDLCamera.SDL_CAMERA_POSITION_BACK_FACING -> BACK;
            default -> UNKNOWN;
        };
    }

    public int value() {
        return value;
    }

}

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

import com.cleanroommc.client.sdl.SDL;

import org.lwjgl.sdl.SDL_Event;

/**
 * Camera driven by the event pump.
 *
 * <p>Internal. Reach cameras through {@link SDL#cameras()} instead.
 */
public final class CameraInternal {

    private CameraInternal() { }

    public static Cameras cameras() {
        return Cameras.INSTANCE;
    }

    public static void dispatch(SDL_Event event) {
        Cameras.INSTANCE.dispatch(event);
    }

    public static void reset() {
        Cameras.INSTANCE.clear();
    }

}

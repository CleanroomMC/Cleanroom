/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.input;

import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLTouch;
import org.lwjgl.sdl.SDL_Finger;

import java.util.ArrayList;
import java.util.List;

/** One touch device. */
public final class Touch {

    private final long id;

    Touch(long id) {
        this.id = id;
    }

    public long id() {
        return id;
    }

    public String name() {
        String name = SDLTouch.SDL_GetTouchDeviceName(id);
        return name == null ? "" : name;
    }

    public TouchKind kind() {
        return TouchKind.of(SDLTouch.SDL_GetTouchDeviceType(id));
    }

    public List<Finger> fingers() {
        PointerBuffer pointers = SDLTouch.SDL_GetTouchFingers(id);
        if (pointers == null) {
            return List.of();
        }
        try {
            List<Finger> fingers = new ArrayList<>(pointers.remaining());
            while (pointers.hasRemaining()) {
                SDL_Finger finger = SDL_Finger.createSafe(pointers.get());
                if (finger != null) {
                    fingers.add(new Finger(finger.id(), finger.x(), finger.y(), finger.pressure()));
                }
            }
            return List.copyOf(fingers);
        } finally {
            SDLStdinc.SDL_free(pointers.rewind());
        }
    }

}

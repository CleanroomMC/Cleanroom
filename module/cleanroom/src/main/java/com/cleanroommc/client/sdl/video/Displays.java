/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.video;

import com.cleanroommc.client.sdl.SDL;

import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/** Connected displays. Requires the video subsystem. */
public final class Displays {

    static final Displays INSTANCE = new Displays();

    private Displays() { }

    public List<Display> all() {
        SDL.ensureVideo();
        IntBuffer ids = SDLVideo.SDL_GetDisplays();
        if (ids == null) {
            return List.of();
        }
        try {
            List<Display> displays = new ArrayList<>(ids.remaining());
            while (ids.hasRemaining()) {
                displays.add(new Display(ids.get()));
            }
            return List.copyOf(displays);
        } finally {
            SDLStdinc.SDL_free(ids.rewind());
        }
    }

    public Display primary() {
        SDL.ensureVideo();
        int id = SDLVideo.SDL_GetPrimaryDisplay();
        return id == 0 ? null : new Display(id);
    }

    public Display of(int id) {
        return id == 0 ? null : new Display(id);
    }

    public String driver() {
        SDL.ensureVideo();
        String name = SDLVideo.SDL_GetCurrentVideoDriver();
        return name == null ? "" : name;
    }

}

/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.events.SystemThemeEvent;

import org.lwjgl.sdl.SDLVideo;

/** Light/dark preference reported by the desktop environment. Requires the video subsystem. */
public enum SystemTheme {

    UNKNOWN(SDLVideo.SDL_SYSTEM_THEME_UNKNOWN),
    LIGHT(SDLVideo.SDL_SYSTEM_THEME_LIGHT),
    DARK(SDLVideo.SDL_SYSTEM_THEME_DARK);

    private final int value;

    SystemTheme(int value) {
        this.value = value;
    }

    /** Called from the window pump when {@code SDL_EVENT_SYSTEM_THEME_CHANGED} arrives. */
    static void changed(long timestampNs) {
        SDL.events().post(new SystemThemeEvent(current(), timestampNs));
    }

    /** @return the current desktop theme, or {@link #UNKNOWN} when the platform does not report one */
    static SystemTheme current() {
        SDL.ensureVideo();
        int theme = SDLVideo.SDL_GetSystemTheme();
        for (SystemTheme candidate : values()) {
            if (candidate.value == theme) {
                return candidate;
            }
        }
        return UNKNOWN;
    }

    public int value() {
        return value;
    }

    public boolean dark() {
        return this == DARK;
    }

}

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

/**
 * A fullscreen mode on a display.
 *
 * @param displayId SDL display id
 * @param format SDL pixel format
 * @param width width in screen coordinates
 * @param height height in screen coordinates
 * @param pixelDensity pixel density
 * @param refreshRate refresh rate in Hz, or {@code 0} when unknown
 */
public record DisplayMode(int displayId, int format, int width, int height, float pixelDensity, float refreshRate) {

    public boolean matches(int width, int height, float refreshRate) {
        return this.width == width &&
            this.height == height &&
            (refreshRate <= 0 || Math.abs(this.refreshRate - refreshRate) < 0.5F);
    }

}

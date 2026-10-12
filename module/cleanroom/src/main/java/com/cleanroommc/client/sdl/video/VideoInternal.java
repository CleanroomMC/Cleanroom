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

/**
 * Internal. Reach displays through {@link SDL#displays()} instead.
 */
public final class VideoInternal {

    private VideoInternal() { }

    public static Displays displays() {
        return Displays.INSTANCE;
    }

}

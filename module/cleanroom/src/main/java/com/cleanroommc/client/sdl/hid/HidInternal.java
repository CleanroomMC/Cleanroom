/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.hid;

import com.cleanroommc.client.sdl.SDL;

/**
 * HID driven by SDL shutdown.
 *
 * <p>Internal. Reach HID devices through {@link SDL#hid()} instead.
 */
public final class HidInternal {

    private HidInternal() { }

    public static Hid hid() {
        return Hid.INSTANCE;
    }

    public static void reset() {
        Hid.closeAll();
    }

}

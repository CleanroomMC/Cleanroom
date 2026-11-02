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

import com.cleanroommc.client.sdl.audio.AudioInternal;
import com.cleanroommc.client.sdl.camera.CameraInternal;
import com.cleanroommc.client.sdl.hid.HidInternal;
import com.cleanroommc.client.sdl.input.InputInternal;

/**
 * Tears every optional device registry down, in the order {@link SDL#shutdown()} needs.
 */
final class Lifecycle {

    private Lifecycle() { }

    static void reset() {
        FileDialogs.pump();
        InputInternal.reset();
        CameraInternal.reset();
        AudioInternal.reset();
        HidInternal.reset();
        Tray.reset();
        Clipboard.reset();
    }

}

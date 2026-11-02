/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.audio;

import com.cleanroommc.client.sdl.SDL;

import org.lwjgl.sdl.SDL_Event;

/**
 * Audio (Devices) driven by the event pump.
 *
 * <p>Internal. Reach audio devices through {@link SDL#audio()} instead.
 */
public final class AudioInternal {

    private AudioInternal() { }

    public static AudioDevices audio() {
        return AudioDevices.INSTANCE;
    }

    public static void dispatch(SDL_Event event) {
        AudioDevices.INSTANCE.dispatch(event);
    }

    public static void reset() {
        AudioDevices.INSTANCE.reset();
    }

}

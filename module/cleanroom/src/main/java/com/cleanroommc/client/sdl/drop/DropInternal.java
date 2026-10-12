/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.drop;

import com.cleanroommc.client.sdl.SDL;

import org.lwjgl.sdl.SDL_Event;

/**
 * Drop driven by the event pump.
 *
 * <p>Internal. Drops are consumed as {@code DropEvent} on {@link SDL#events()}.
 */
public final class DropInternal {

    private DropInternal() { }

    public static void dispatch(SDL_Event event) {
        Drops.handle(event);
    }

}

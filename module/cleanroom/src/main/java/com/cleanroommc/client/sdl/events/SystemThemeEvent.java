/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.SystemTheme;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Posted on {@link com.cleanroommc.client.sdl.SDL#events()} when the desktop switches between light and dark.
 */
public class SystemThemeEvent extends Event {

    private final SystemTheme theme;
    private final long timestampNs;

    public SystemThemeEvent(SystemTheme theme, long timestampNs) {
        this.theme = theme;
        this.timestampNs = timestampNs;
    }

    public SystemTheme theme() {
        return this.theme;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

}

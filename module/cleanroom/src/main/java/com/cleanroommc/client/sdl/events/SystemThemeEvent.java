package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.SystemTheme;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Posted on {@link com.cleanroommc.client.sdl.SDL#EVENT_BUS} when the desktop switches between light and
 * dark, from the window pump's {@code SDL_EVENT_SYSTEM_THEME_CHANGED} handling.
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

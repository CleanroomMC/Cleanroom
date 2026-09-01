package com.cleanroommc.client.sdl.video;

import com.cleanroommc.client.sdl.SDL;

/**
 * <p>Internal. Reach displays through {@link SDL#displays()} instead.
 */
public final class VideoInternal {

    public static Displays displays() {
        return Displays.INSTANCE;
    }

    private VideoInternal() { }

}

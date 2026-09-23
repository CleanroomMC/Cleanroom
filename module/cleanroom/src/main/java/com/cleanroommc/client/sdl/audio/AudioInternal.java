package com.cleanroommc.client.sdl.audio;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDL_Event;

/**
 * Audio (Devices) driven by the event pump.
 *
 * <p>Internal. Reach audio devices through {@link SDL#audio()} instead.
 */
public final class AudioInternal {

    public static AudioDevices audio() {
        return AudioDevices.INSTANCE;
    }

    public static void dispatch(SDL_Event event) {
        AudioDevices.INSTANCE.dispatch(event);
    }

    public static void reset() {
        AudioDevices.INSTANCE.reset();
    }

    private AudioInternal() { }

}

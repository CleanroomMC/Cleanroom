package com.cleanroommc.client.sdl.camera;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDL_Event;

/**
 * Camera driven by the event pump.
 *
 * <p>Internal. Reach cameras through {@link SDL#cameras()} instead.
 */
public final class CameraInternal {

    public static Cameras cameras() {
        return Cameras.INSTANCE;
    }

    public static void dispatch(SDL_Event event) {
        Cameras.INSTANCE.dispatch(event);
    }

    public static void reset() {
        Cameras.INSTANCE.clear();
    }

    private CameraInternal() { }

}

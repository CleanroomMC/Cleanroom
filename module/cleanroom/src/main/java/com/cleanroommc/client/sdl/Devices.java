package com.cleanroommc.client.sdl;

import com.cleanroommc.client.sdl.camera.Cameras;
import com.cleanroommc.client.sdl.drop.Drops;
import com.cleanroommc.client.sdl.hid.Hid;
import com.cleanroommc.client.sdl.input.Inputs;
import org.lwjgl.sdl.SDL_Event;

/** {@link Window#pump()} delegates to optional device registries. */
final class Devices {

    static void dispatch(SDL_Event event) {
        Inputs.dispatch(event);
        Cameras.handle(event);
    }

    static void afterPump() {
        Tray.pump();
    }

    static void reset() {
        Inputs.reset();
        Cameras.reset();
        Hid.reset();
        Tray.reset();
        Clipboard.reset();
        Drops.reset();
    }

    private Devices() { }

}

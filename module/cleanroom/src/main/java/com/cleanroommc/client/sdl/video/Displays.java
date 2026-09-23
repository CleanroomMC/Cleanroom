package com.cleanroommc.client.sdl.video;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/** Connected displays. Requires the video subsystem. */
public final class Displays {

    static final Displays INSTANCE = new Displays();

    private Displays() { }

    public List<Display> all() {
        SDL.ensureVideo();
        IntBuffer ids = SDLVideo.SDL_GetDisplays();
        if (ids == null) {
            return List.of();
        }
        try {
            List<Display> displays = new ArrayList<>(ids.remaining());
            while (ids.hasRemaining()) {
                displays.add(new Display(ids.get()));
            }
            return List.copyOf(displays);
        } finally {
            SDLStdinc.SDL_free(ids);
        }
    }

    public Display primary() {
        SDL.ensureVideo();
        int id = SDLVideo.SDL_GetPrimaryDisplay();
        return id == 0 ? null : new Display(id);
    }

    public Display of(int id) {
        return id == 0 ? null : new Display(id);
    }

    public String driver() {
        SDL.ensureVideo();
        String name = SDLVideo.SDL_GetCurrentVideoDriver();
        return name == null ? "" : name;
    }


}

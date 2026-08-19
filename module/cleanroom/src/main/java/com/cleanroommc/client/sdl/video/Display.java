package com.cleanroommc.client.sdl.video;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.PointerBuffer;
import org.lwjgl.sdl.SDLStdinc;
import org.lwjgl.sdl.SDLVideo;
import org.lwjgl.sdl.SDL_DisplayMode;
import org.lwjgl.sdl.SDL_Rect;
import org.lwjgl.system.MemoryStack;

import java.util.ArrayList;
import java.util.List;

/** One connected display. */
public final class Display {

    public record Bounds(int x, int y, int width, int height) { }

    static DisplayMode snapshot(SDL_DisplayMode mode) {
        return new DisplayMode(mode.displayID(), mode.format(), mode.w(), mode.h(), mode.pixel_density(), mode.refresh_rate());
    }

    private final int id;

    Display(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public String name() {
        String name = SDLVideo.SDL_GetDisplayName(id);
        return name == null ? "" : name;
    }

    public Bounds bounds() {
        return bounds(false);
    }

    public Bounds usableBounds() {
        return bounds(true);
    }

    public float contentScale() {
        return SDLVideo.SDL_GetDisplayContentScale(id);
    }

    public DisplayMode desktopMode() {
        SDL_DisplayMode mode = SDLVideo.SDL_GetDesktopDisplayMode(id);
        return mode == null ? null : snapshot(mode);
    }

    public DisplayMode currentMode() {
        SDL_DisplayMode mode = SDLVideo.SDL_GetCurrentDisplayMode(id);
        return mode == null ? null : snapshot(mode);
    }

    public List<DisplayMode> modes() {
        PointerBuffer pointers = SDLVideo.SDL_GetFullscreenDisplayModes(id);
        if (pointers == null) {
            return List.of();
        }
        try {
            List<DisplayMode> modes = new ArrayList<>(pointers.remaining());
            while (pointers.hasRemaining()) {
                SDL_DisplayMode mode = SDL_DisplayMode.createSafe(pointers.get());
                if (mode != null) {
                    modes.add(snapshot(mode));
                }
            }
            return List.copyOf(modes);
        } finally {
            SDLStdinc.SDL_free(pointers);
        }
    }

    DisplayMode match(int width, int height, float refreshRate) {
        DisplayMode best = null;
        for (DisplayMode mode : modes()) {
            if (!mode.matches(width, height, 0)) {
                continue;
            }
            if (refreshRate <= 0 || mode.matches(width, height, refreshRate)) {
                return mode;
            }
            if (best == null) {
                best = mode;
            }
        }
        return best;
    }

    private Bounds bounds(boolean usable) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SDL_Rect rect = SDL_Rect.calloc(stack);
            if (usable) {
                SDL.check(SDLVideo.SDL_GetDisplayUsableBounds(id, rect), "SDL_GetDisplayUsableBounds");
            } else {
                SDL.check(SDLVideo.SDL_GetDisplayBounds(id, rect), "SDL_GetDisplayBounds");
            }
            return new Bounds(rect.x(), rect.y(), rect.w(), rect.h());
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Display display && display.id == id;
    }

    @Override
    public int hashCode() {
        return id;
    }

}

package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.Surfaces;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDL_Surface;

import java.awt.image.BufferedImage;

/** A color cursor owned by the caller. Destroy it, or let {@link Inputs#reset()} do so. */
public final class Cursor implements AutoCloseable {

    public static Cursor of(BufferedImage image) {
        return of(image, 0, 0);
    }

    public static Cursor of(BufferedImage image, int hotX, int hotY) {
        SDL_Surface surface = Surfaces.from(image);
        try {
            long handle = SDL.checkHandle(SDLMouse.SDL_CreateColorCursor(surface, hotX, hotY), "SDL_CreateColorCursor");
            Cursor cursor = new Cursor(handle, false);
            Inputs.track(cursor);
            return cursor;
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
    }

    static Cursor system(SystemCursor kind) {
        long handle = SDL.checkHandle(SDLMouse.SDL_CreateSystemCursor(kind.value()), "SDL_CreateSystemCursor");
        return new Cursor(handle, true);
    }

    private final long handle;
    private final boolean system;

    private boolean closed;

    private Cursor(long handle, boolean system) {
        this.handle = handle;
        this.system = system;
    }

    void apply() {
        ensureOpen();
        SDL.check(SDLMouse.SDL_SetCursor(handle), "SDL_SetCursor");
    }

    boolean system() {
        return system;
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("The cursor is closed");
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        SDLMouse.SDL_DestroyCursor(handle);
        Inputs.forget(this);
    }

}

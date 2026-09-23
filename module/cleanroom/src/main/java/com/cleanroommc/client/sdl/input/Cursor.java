package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.internal.Surfaces;
import org.lwjgl.sdl.SDLMouse;
import org.lwjgl.sdl.SDLSurface;
import org.lwjgl.sdl.SDL_Surface;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** A color cursor owned by the caller. Destroy it, or let {@link SDL#shutdown()} do so. */
public final class Cursor implements AutoCloseable {

    private static final Map<SystemCursor, Cursor> SYSTEM = new EnumMap<>(SystemCursor.class);
    private static final Set<Cursor> CUSTOM = new HashSet<>();

    public static Cursor of(BufferedImage image) {
        return of(image, 0, 0);
    }

    public static Cursor of(BufferedImage image, int hotX, int hotY) {
        SDL_Surface surface = Surfaces.from(image);
        try {
            long handle = SDL.checkHandle(SDLMouse.SDL_CreateColorCursor(surface, hotX, hotY), "SDL_CreateColorCursor");
            Cursor cursor = new Cursor(handle, false);
            track(cursor);
            return cursor;
        } finally {
            SDLSurface.SDL_DestroySurface(surface);
        }
    }

    static synchronized Cursor system(SystemCursor kind) {
        return SYSTEM.computeIfAbsent(kind, owned -> new Cursor(
                SDL.checkHandle(SDLMouse.SDL_CreateSystemCursor(owned.value()), "SDL_CreateSystemCursor"), true));
    }

    static synchronized void closeAll() {
        for (Cursor cursor : List.copyOf(SYSTEM.values())) {
            cursor.close();
        }
        SYSTEM.clear();
        for (Cursor cursor : Set.copyOf(CUSTOM)) {
            cursor.close();
        }
        CUSTOM.clear();
    }

    private static synchronized void track(Cursor cursor) {
        CUSTOM.add(cursor);
    }

    private static synchronized void forget(Cursor cursor) {
        CUSTOM.remove(cursor);
        if (cursor.system) {
            SYSTEM.values().remove(cursor);
        }
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
        forget(this);
    }

}

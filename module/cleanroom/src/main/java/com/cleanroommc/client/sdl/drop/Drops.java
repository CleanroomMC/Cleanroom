package com.cleanroommc.client.sdl.drop;

import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_DropEvent;
import org.lwjgl.sdl.SDL_Event;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Process-wide drop target. {@link com.cleanroommc.client.sdl.Window} forwards the SDL drop events here.
 */
public final class Drops {

    private static final List<DropListener> LISTENERS = new CopyOnWriteArrayList<>();

    private Drops() { }

    public static void listen(DropListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        }
        LISTENERS.add(listener);
    }

    public static void mute(DropListener listener) {
        LISTENERS.remove(listener);
    }

    public static void dispatchFile(Path path) {
        if (path != null) {
            file(path.toString());
        }
    }

    public static void dispatchText(String text) {
        text(text);
    }

    public static void handle(SDL_Event event) {
        SDL_DropEvent drop = event.drop();
        switch (event.type()) {
            case SDLEvents.SDL_EVENT_DROP_BEGIN -> begin();
            case SDLEvents.SDL_EVENT_DROP_FILE -> file(drop.dataString());
            case SDLEvents.SDL_EVENT_DROP_TEXT -> text(drop.dataString());
            case SDLEvents.SDL_EVENT_DROP_POSITION -> position(drop.x(), drop.y());
            case SDLEvents.SDL_EVENT_DROP_COMPLETE -> complete();
            default -> { }
        }
    }

    static void begin() {
        for (DropListener listener : LISTENERS) {
            listener.begin();
        }
    }

    static void file(String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        Path path = Path.of(data);
        for (DropListener listener : LISTENERS) {
            listener.file(path);
        }
    }

    static void text(String data) {
        String value = data == null ? "" : data;
        for (DropListener listener : LISTENERS) {
            listener.text(value);
        }
    }

    static void position(float x, float y) {
        for (DropListener listener : LISTENERS) {
            listener.position(x, y);
        }
    }

    static void complete() {
        for (DropListener listener : LISTENERS) {
            listener.complete();
        }
    }

    public static void reset() {
        LISTENERS.clear();
    }

    public static int listenerCount() {
        return LISTENERS.size();
    }

}

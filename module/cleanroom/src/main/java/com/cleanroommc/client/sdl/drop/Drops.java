package com.cleanroommc.client.sdl.drop;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.DropEvent;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDL_DropEvent;
import org.lwjgl.sdl.SDL_Event;

import java.nio.file.Path;

/** Process-wide drop target. The event pump forwards the SDL drop events here. */
final class Drops {

    private Drops() { }

    static void handle(SDL_Event event) {
        SDL_DropEvent drop = event.drop();
        int windowId = drop.windowID();
        long timestampNs = drop.timestamp();
        float x = drop.x();
        float y = drop.y();
        String source = drop.sourceString();
        switch (event.type()) {
            case SDLEvents.SDL_EVENT_DROP_BEGIN -> begin(windowId, timestampNs, x, y, source);
            case SDLEvents.SDL_EVENT_DROP_FILE -> file(windowId, timestampNs, x, y, source, drop.dataString());
            case SDLEvents.SDL_EVENT_DROP_TEXT -> text(windowId, timestampNs, x, y, source, drop.dataString());
            case SDLEvents.SDL_EVENT_DROP_POSITION ->
                    SDL.events().post(new DropEvent.Position(windowId, timestampNs, x, y, source));
            case SDLEvents.SDL_EVENT_DROP_COMPLETE ->
                    SDL.events().post(new DropEvent.Complete(windowId, timestampNs, x, y, source));
        }
    }

    static void begin(int windowId, long timestampNs, float x, float y, String source) {
        SDL.events().post(new DropEvent.Begin(windowId, timestampNs, x, y, source));
    }

    static void file(int windowId, long timestampNs, float x, float y, String source, String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        SDL.events().post(new DropEvent.File(windowId, timestampNs, x, y, source, Path.of(data)));
    }

    static void text(int windowId, long timestampNs, float x, float y, String source, String data) {
        SDL.events().post(new DropEvent.Text(windowId, timestampNs, x, y, source, data == null ? "" : data));
    }

}

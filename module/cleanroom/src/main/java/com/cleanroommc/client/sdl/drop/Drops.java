package com.cleanroommc.client.sdl.drop;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.DropEvent;
import org.lwjgl.sdl.SDLEvents;
import org.lwjgl.sdl.SDLTimer;
import org.lwjgl.sdl.SDL_DropEvent;
import org.lwjgl.sdl.SDL_Event;

import java.nio.file.Path;

/**
 * Process-wide drop target. {@link com.cleanroommc.client.sdl.Window} forwards the SDL drop events here.
 */
public final class Drops {

    private Drops() { }

    /** Injects a drop that did not come from SDL, so it carries no window, position or source app. */
    public static void dispatchFile(Path path) {
        if (path != null) {
            file(0, SDLTimer.SDL_GetTicksNS(), 0.0F, 0.0F, null, path.toString());
        }
    }

    public static void dispatchText(String text) {
        text(0, SDLTimer.SDL_GetTicksNS(), 0.0F, 0.0F, null, text);
    }

    public static void handle(SDL_Event event) {
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
                    SDL.EVENT_BUS.post(new DropEvent.Position(windowId, timestampNs, x, y, source));
            case SDLEvents.SDL_EVENT_DROP_COMPLETE ->
                    SDL.EVENT_BUS.post(new DropEvent.Complete(windowId, timestampNs, x, y, source));
        }
    }

    static void begin(int windowId, long timestampNs, float x, float y, String source) {
        SDL.EVENT_BUS.post(new DropEvent.Begin(windowId, timestampNs, x, y, source));
    }

    static void file(int windowId, long timestampNs, float x, float y, String source, String data) {
        if (data == null || data.isEmpty()) {
            return;
        }
        SDL.EVENT_BUS.post(new DropEvent.File(windowId, timestampNs, x, y, source, Path.of(data)));
    }

    static void text(int windowId, long timestampNs, float x, float y, String source, String data) {
        SDL.EVENT_BUS.post(new DropEvent.Text(windowId, timestampNs, x, y, source, data == null ? "" : data));
    }

}

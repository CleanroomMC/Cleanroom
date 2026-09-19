package com.cleanroommc.client.sdl.drop;

import com.cleanroommc.client.sdl.SDL;
import org.lwjgl.sdl.SDL_Event;

/**
 * Drop driven by the event pump.
 *
 * <p>Internal. Drops are consumed as {@code DropEvent} on {@link SDL#events()}.
 */
public final class DropInternal {

    public static void dispatch(SDL_Event event) {
        Drops.handle(event);
    }

    private DropInternal() { }

}

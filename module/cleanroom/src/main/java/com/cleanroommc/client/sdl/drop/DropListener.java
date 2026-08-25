package com.cleanroommc.client.sdl.drop;

import java.nio.file.Path;

/** File and text drops onto the host window. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface DropListener {

    default void begin() { }

    default void file(Path path) { }

    default void text(String text) { }

    default void position(float x, float y) { }

    default void complete() { }

}

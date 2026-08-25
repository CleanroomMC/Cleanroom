package com.cleanroommc.client.sdl.input;

/** Drawing-tablet events. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface PenListener {

    default void proximity(int pen, boolean in) { }

    default void down(int pen, float x, float y, boolean eraser) { }

    default void up(int pen, float x, float y, boolean eraser) { }

    default void motion(int pen, float x, float y) { }

    default void button(int pen, float x, float y, int button, boolean down) { }

    default void axis(int pen, float x, float y, PenAxis axis, float value) { }

}

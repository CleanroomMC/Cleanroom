package com.cleanroommc.client.sdl.input;

/** Multi-touch and pinch. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface TouchListener {

    default void down(long device, long finger, float x, float y, float pressure) { }

    default void up(long device, long finger, float x, float y, float pressure) { }

    default void motion(long device, long finger, float x, float y, float dx, float dy, float pressure) { }

    default void canceled(long device, long finger, float x, float y, float pressure) { }

    default void pinchBegin(float scale) { }

    default void pinch(float scale) { }

    default void pinchEnd(float scale) { }

}

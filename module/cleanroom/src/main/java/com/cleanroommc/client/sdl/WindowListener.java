package com.cleanroommc.client.sdl;

/**
 * Host-window notifications. Called from {@link Window#pump()} on the thread that pumps.
 */
public interface WindowListener {

    default void resized(int width, int height) { }

    default void focus(boolean focused) { }

    default void closeRequested() { }

    default void displayChanged() { }

    default void scaleChanged(float scale) { }

}

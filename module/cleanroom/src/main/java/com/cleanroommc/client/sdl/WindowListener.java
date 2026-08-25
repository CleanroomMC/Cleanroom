package com.cleanroommc.client.sdl;

/**
 * Host-window notifications. Called from {@link Window#pump()} on the thread that pumps, except
 * {@link #borderlessChanged(boolean)}, which fires from {@link Window#borderless(boolean)} because SDL
 * reports no event for it.
 */
public interface WindowListener {

    default void resized(int width, int height) { }

    default void focus(boolean focused) { }

    default void closeRequested() { }

    default void displayChanged() { }

    default void scaleChanged(float scale) { }

    default void fullscreenChanged(boolean fullscreen) { }

    default void borderlessChanged(boolean borderless) { }

}

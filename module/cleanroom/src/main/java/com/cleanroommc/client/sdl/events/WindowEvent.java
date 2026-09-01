package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.Window;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Host-window notifications, posted on {@link com.cleanroommc.client.sdl.SDL#events()}.
 */
public abstract class WindowEvent extends Event {

    private final Window window;
    private final long timestampNs;

    protected WindowEvent(Window window, long timestampNs) {
        this.window = window;
        this.timestampNs = timestampNs;
    }

    public Window window() {
        return this.window;
    }

    /** @return SDL's nanosecond clock, taken when the event was raised */
    public long timestampNs() {
        return this.timestampNs;
    }

    public static class Resized extends WindowEvent {

        private final int width;
        private final int height;

        public Resized(Window window, long timestampNs, int width, int height) {
            super(window, timestampNs);
            this.width = width;
            this.height = height;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

    }

    public static class Focus extends WindowEvent {

        private final boolean focused;

        public Focus(Window window, long timestampNs, boolean focused) {
            super(window, timestampNs);
            this.focused = focused;
        }

        public boolean focused() {
            return this.focused;
        }

    }

    public static class CloseRequested extends WindowEvent {

        public CloseRequested(Window window, long timestampNs) {
            super(window, timestampNs);
        }

    }

    public static class DisplayChanged extends WindowEvent {

        private final int displayId;

        public DisplayChanged(Window window, long timestampNs, int displayId) {
            super(window, timestampNs);
            this.displayId = displayId;
        }

        /** @return the SDL id of the display the window moved to */
        public int displayId() {
            return this.displayId;
        }

    }

    public static class ScaleChanged extends WindowEvent {

        private final float scale;

        public ScaleChanged(Window window, long timestampNs, float scale) {
            super(window, timestampNs);
            this.scale = scale;
        }

        public float scale() {
            return this.scale;
        }

    }

    public static class Fullscreen extends WindowEvent {

        private final boolean fullscreen;

        public Fullscreen(Window window, long timestampNs, boolean fullscreen) {
            super(window, timestampNs);
            this.fullscreen = fullscreen;
        }

        public boolean fullscreen() {
            return this.fullscreen;
        }

    }

    public static class Borderless extends WindowEvent {

        private final boolean borderless;

        public Borderless(Window window, long timestampNs, boolean borderless) {
            super(window, timestampNs);
            this.borderless = borderless;
        }

        public boolean borderless() {
            return this.borderless;
        }

    }

}

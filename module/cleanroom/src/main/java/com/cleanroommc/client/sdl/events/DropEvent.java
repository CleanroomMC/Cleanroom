package com.cleanroommc.client.sdl.events;

import net.minecraftforge.fml.common.eventhandler.Event;

import java.nio.file.Path;

/**
 * File and text drops onto the host window, posted on {@link com.cleanroommc.client.sdl.SDL#events()}.
 */
public abstract class DropEvent extends Event {

    private final int windowId;
    private final long timestampNs;
    private final float x;
    private final float y;
    private final String source;

    protected DropEvent(int windowId, long timestampNs, float x, float y, String source) {
        this.windowId = windowId;
        this.timestampNs = timestampNs;
        this.x = x;
        this.y = y;
        this.source = source;
    }

    /** @return the SDL id of the window dropped onto */
    public int windowId() {
        return this.windowId;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    public float x() {
        return this.x;
    }

    public float y() {
        return this.y;
    }

    /** @return the app that started the drag, or {@code null} when the platform does not name one */
    public String source() {
        return this.source;
    }

    public static class Begin extends DropEvent {

        public Begin(int windowId, long timestampNs, float x, float y, String source) {
            super(windowId, timestampNs, x, y, source);
        }

    }

    public static class File extends DropEvent {

        private final Path path;

        public File(int windowId, long timestampNs, float x, float y, String source, Path path) {
            super(windowId, timestampNs, x, y, source);
            this.path = path;
        }

        public Path path() {
            return this.path;
        }

    }

    public static class Text extends DropEvent {

        private final String text;

        public Text(int windowId, long timestampNs, float x, float y, String source, String text) {
            super(windowId, timestampNs, x, y, source);
            this.text = text;
        }

        public String text() {
            return this.text;
        }

    }

    public static class Position extends DropEvent {

        public Position(int windowId, long timestampNs, float x, float y, String source) {
            super(windowId, timestampNs, x, y, source);
        }

    }

    public static class Complete extends DropEvent {

        public Complete(int windowId, long timestampNs, float x, float y, String source) {
            super(windowId, timestampNs, x, y, source);
        }

    }

}

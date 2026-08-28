package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.input.PenAxis;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Drawing-tablet events, posted on {@link com.cleanroommc.client.sdl.SDL#EVENT_BUS} from
 * {@link com.cleanroommc.client.sdl.Window#pump()}.
 */
public abstract class PenEvent extends Event {

    private final int pen;
    private final int windowId;
    private final long timestampNs;

    protected PenEvent(int pen, int windowId, long timestampNs) {
        this.pen = pen;
        this.windowId = windowId;
        this.timestampNs = timestampNs;
    }

    public int pen() {
        return this.pen;
    }

    /** @return the SDL id of the window the pen reported against */
    public int windowId() {
        return this.windowId;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    /** Everything but {@link Proximity} carries the pen's position and the state of its buttons and tip. */
    public abstract static class Positioned extends PenEvent {

        private final float x;
        private final float y;
        private final int state;

        protected Positioned(int pen, int windowId, long timestampNs, float x, float y, int state) {
            super(pen, windowId, timestampNs);
            this.x = x;
            this.y = y;
            this.state = state;
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        /** @return SDL's {@code SDL_PenInputFlags} bitmask of held buttons, tip and eraser */
        public int state() {
            return this.state;
        }

    }

    public static class Proximity extends PenEvent {

        private final boolean in;

        public Proximity(int pen, int windowId, long timestampNs, boolean in) {
            super(pen, windowId, timestampNs);
            this.in = in;
        }

        public boolean in() {
            return this.in;
        }

    }

    public static class Down extends Positioned {

        private final boolean eraser;

        public Down(int pen, int windowId, long timestampNs, float x, float y, int state, boolean eraser) {
            super(pen, windowId, timestampNs, x, y, state);
            this.eraser = eraser;
        }

        public boolean eraser() {
            return this.eraser;
        }

    }

    public static class Up extends Positioned {

        private final boolean eraser;

        public Up(int pen, int windowId, long timestampNs, float x, float y, int state, boolean eraser) {
            super(pen, windowId, timestampNs, x, y, state);
            this.eraser = eraser;
        }

        public boolean eraser() {
            return this.eraser;
        }

    }

    public static class Motion extends Positioned {

        public Motion(int pen, int windowId, long timestampNs, float x, float y, int state) {
            super(pen, windowId, timestampNs, x, y, state);
        }

    }

    public static class Button extends Positioned {

        private final int button;
        private final boolean down;

        public Button(int pen, int windowId, long timestampNs, float x, float y, int state, int button, boolean down) {
            super(pen, windowId, timestampNs, x, y, state);
            this.button = button;
            this.down = down;
        }

        public int button() {
            return this.button;
        }

        public boolean down() {
            return this.down;
        }

    }

    public static class Axis extends Positioned {

        private final PenAxis axis;
        private final float value;

        public Axis(int pen, int windowId, long timestampNs, float x, float y, int state, PenAxis axis, float value) {
            super(pen, windowId, timestampNs, x, y, state);
            this.axis = axis;
            this.value = value;
        }

        public PenAxis axis() {
            return this.axis;
        }

        public float value() {
            return this.value;
        }

    }

}

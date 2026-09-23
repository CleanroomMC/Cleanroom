package com.cleanroommc.client.sdl.events;

import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Multi-touch and pinch, posted on {@link com.cleanroommc.client.sdl.SDL#events()}.
 */
public abstract class TouchEvent extends Event {

    private final int windowId;
    private final long timestampNs;

    protected TouchEvent(int windowId, long timestampNs) {
        this.windowId = windowId;
        this.timestampNs = timestampNs;
    }

    /** @return the SDL id of the window the touch reported against */
    public int windowId() {
        return this.windowId;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    /** A single finger on a touch device. Coordinates are normalized to {@code 0..1} of the window. */
    public abstract static class Finger extends TouchEvent {

        private final long device;
        private final long finger;
        private final float x;
        private final float y;
        private final float pressure;

        protected Finger(int windowId, long timestampNs, long device, long finger, float x, float y, float pressure) {
            super(windowId, timestampNs);
            this.device = device;
            this.finger = finger;
            this.x = x;
            this.y = y;
            this.pressure = pressure;
        }

        public long device() {
            return this.device;
        }

        public long finger() {
            return this.finger;
        }

        public float x() {
            return this.x;
        }

        public float y() {
            return this.y;
        }

        public float pressure() {
            return this.pressure;
        }

    }

    public static class Down extends Finger {

        public Down(int windowId, long timestampNs, long device, long finger, float x, float y, float pressure) {
            super(windowId, timestampNs, device, finger, x, y, pressure);
        }

    }

    public static class Up extends Finger {

        public Up(int windowId, long timestampNs, long device, long finger, float x, float y, float pressure) {
            super(windowId, timestampNs, device, finger, x, y, pressure);
        }

    }

    public static class Canceled extends Finger {

        public Canceled(int windowId, long timestampNs, long device, long finger, float x, float y, float pressure) {
            super(windowId, timestampNs, device, finger, x, y, pressure);
        }

    }

    public static class Motion extends Finger {

        private final float dx;
        private final float dy;

        public Motion(int windowId, long timestampNs, long device, long finger, float x, float y, float dx, float dy,
                      float pressure) {
            super(windowId, timestampNs, device, finger, x, y, pressure);
            this.dx = dx;
            this.dy = dy;
        }

        public float dx() {
            return this.dx;
        }

        public float dy() {
            return this.dy;
        }

    }

    /** A pinch gesture, whose scale is relative to the gesture's start. */
    public abstract static class Pinch extends TouchEvent {

        private final float scale;

        protected Pinch(int windowId, long timestampNs, float scale) {
            super(windowId, timestampNs);
            this.scale = scale;
        }

        public float scale() {
            return this.scale;
        }

    }

    public static class PinchBegin extends Pinch {

        public PinchBegin(int windowId, long timestampNs, float scale) {
            super(windowId, timestampNs, scale);
        }

    }

    public static class PinchUpdate extends Pinch {

        public PinchUpdate(int windowId, long timestampNs, float scale) {
            super(windowId, timestampNs, scale);
        }

    }

    public static class PinchEnd extends Pinch {

        public PinchEnd(int windowId, long timestampNs, float scale) {
            super(windowId, timestampNs, scale);
        }

    }

}

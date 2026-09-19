package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.input.Gamepad;
import com.cleanroommc.client.sdl.input.SensorType;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Hotplug and pad-local input for mapped gamepads, posted on
 * {@link com.cleanroommc.client.sdl.SDL#events()}.
 */
public abstract class GamepadEvent extends Event {

    private final int instanceId;
    private final long timestampNs;

    protected GamepadEvent(int instanceId, long timestampNs) {
        this.instanceId = instanceId;
        this.timestampNs = timestampNs;
    }

    public int instanceId() {
        return this.instanceId;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    public static class Added extends GamepadEvent {

        private final Gamepad gamepad;

        public Added(long timestampNs, Gamepad gamepad) {
            super(gamepad.id(), timestampNs);
            this.gamepad = gamepad;
        }

        public Gamepad gamepad() {
            return this.gamepad;
        }

    }

    /** The pad is already closed by the time this fires, so only its id is left. */
    public static class Removed extends GamepadEvent {

        public Removed(int instanceId, long timestampNs) {
            super(instanceId, timestampNs);
        }

    }

    public static class Remapped extends GamepadEvent {

        private final Gamepad gamepad;

        public Remapped(long timestampNs, Gamepad gamepad) {
            super(gamepad.id(), timestampNs);
            this.gamepad = gamepad;
        }

        public Gamepad gamepad() {
            return this.gamepad;
        }

    }

    public static class Touchpad extends GamepadEvent {

        private final Gamepad gamepad;
        private final int touchpad;
        private final int finger;
        private final float x;
        private final float y;
        private final float pressure;
        private final boolean down;

        public Touchpad(long timestampNs, Gamepad gamepad, int touchpad, int finger, float x, float y, float pressure,
                        boolean down) {
            super(gamepad.id(), timestampNs);
            this.gamepad = gamepad;
            this.touchpad = touchpad;
            this.finger = finger;
            this.x = x;
            this.y = y;
            this.pressure = pressure;
            this.down = down;
        }

        public Gamepad gamepad() {
            return this.gamepad;
        }

        public int touchpad() {
            return this.touchpad;
        }

        public int finger() {
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

        public boolean down() {
            return this.down;
        }

    }

    public static class Sensor extends GamepadEvent {

        private final Gamepad gamepad;
        private final SensorType type;
        private final float[] data;
        private final long sensorTimestampNs;

        public Sensor(long timestampNs, Gamepad gamepad, SensorType type, float[] data, long sensorTimestampNs) {
            super(gamepad.id(), timestampNs);
            this.gamepad = gamepad;
            this.type = type;
            this.data = data;
            this.sensorTimestampNs = sensorTimestampNs;
        }

        public Gamepad gamepad() {
            return this.gamepad;
        }

        public SensorType type() {
            return this.type;
        }

        public float[] data() {
            return this.data;
        }

        /** @return the hardware's own timestamp for the reading, which lags {@link #timestampNs()} */
        public long sensorTimestampNs() {
            return this.sensorTimestampNs;
        }

    }

}

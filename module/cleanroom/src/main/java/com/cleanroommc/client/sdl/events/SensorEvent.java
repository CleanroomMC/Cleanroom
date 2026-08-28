package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.input.Sensor;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Hotplug and readings for standalone sensors, posted on
 * {@link com.cleanroommc.client.sdl.SDL#EVENT_BUS} from {@link com.cleanroommc.client.sdl.Window#pump()}.
 */
public abstract class SensorEvent extends Event {

    private final int id;
    private final long timestampNs;

    protected SensorEvent(int id, long timestampNs) {
        this.id = id;
        this.timestampNs = timestampNs;
    }

    public int id() {
        return this.id;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    public static class Added extends SensorEvent {

        private final Sensor sensor;

        public Added(long timestampNs, Sensor sensor) {
            super(sensor.id(), timestampNs);
            this.sensor = sensor;
        }

        public Sensor sensor() {
            return this.sensor;
        }

    }

    /** The sensor is already closed by the time this fires, so only its id is left. */
    public static class Removed extends SensorEvent {

        public Removed(int id, long timestampNs) {
            super(id, timestampNs);
        }

    }

    public static class Updated extends SensorEvent {

        private final Sensor sensor;
        private final float[] data;
        private final long sensorTimestampNs;

        public Updated(long timestampNs, Sensor sensor, float[] data, long sensorTimestampNs) {
            super(sensor.id(), timestampNs);
            this.sensor = sensor;
            this.data = data;
            this.sensorTimestampNs = sensorTimestampNs;
        }

        public Sensor sensor() {
            return this.sensor;
        }

        /** @return three floats: accel is in m/s², gyro is in rad/s */
        public float[] data() {
            return this.data;
        }

        /** @return the hardware's own timestamp for the reading, which lags {@link #timestampNs()} */
        public long sensorTimestampNs() {
            return this.sensorTimestampNs;
        }

    }

}

package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.audio.AudioDevice;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Audio endpoint hotplug, posted on {@link com.cleanroommc.client.sdl.SDL#events()}.
 *
 * <p>Playback still goes through OpenAL, a listener that cares about the sound engine
 * wants {@link DefaultChanged} rather than {@link Removed}.
 * OpenAL opened the default endpoint and does not follow it when the system moves.
 */
public abstract class AudioDeviceEvent extends Event {

    private final int id;
    private final long timestampNs;

    protected AudioDeviceEvent(int id, long timestampNs) {
        this.id = id;
        this.timestampNs = timestampNs;
    }

    public int id() {
        return this.id;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    public static class Added extends AudioDeviceEvent {

        private final AudioDevice device;

        public Added(int id, long timestampNs, AudioDevice device) {
            super(id, timestampNs);
            this.device = device;
        }

        public AudioDevice device() {
            return this.device;
        }

    }

    public static class Removed extends AudioDeviceEvent {

        private final boolean recording;

        public Removed(int id, long timestampNs, boolean recording) {
            super(id, timestampNs);
            this.recording = recording;
        }

        public boolean recording() {
            return this.recording;
        }

    }

    public static class FormatChanged extends AudioDeviceEvent {

        private final AudioDevice device;

        public FormatChanged(int id, long timestampNs, AudioDevice device) {
            super(id, timestampNs);
            this.device = device;
        }

        public AudioDevice device() {
            return this.device;
        }

    }

    public static class DefaultChanged extends AudioDeviceEvent {

        private final String previous;
        private final String current;

        public DefaultChanged(long timestampNs, String previous, String current) {
            super(0, timestampNs);
            this.previous = previous;
            this.current = current;
        }

        public String previous() {
            return this.previous;
        }

        public String current() {
            return this.current;
        }

    }

}

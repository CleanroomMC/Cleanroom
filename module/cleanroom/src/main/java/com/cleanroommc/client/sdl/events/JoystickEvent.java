package com.cleanroommc.client.sdl.events;

import com.cleanroommc.client.sdl.Power;
import com.cleanroommc.client.sdl.input.Joystick;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * Hotplug and battery for raw joysticks, posted on {@link com.cleanroommc.client.sdl.SDL#events()}.
 */
public abstract class JoystickEvent extends Event {

    private final int instanceId;
    private final long timestampNs;

    protected JoystickEvent(int instanceId, long timestampNs) {
        this.instanceId = instanceId;
        this.timestampNs = timestampNs;
    }

    public int instanceId() {
        return this.instanceId;
    }

    public long timestampNs() {
        return this.timestampNs;
    }

    public static class Added extends JoystickEvent {

        private final Joystick joystick;

        public Added(long timestampNs, Joystick joystick) {
            super(joystick.id(), timestampNs);
            this.joystick = joystick;
        }

        public Joystick joystick() {
            return this.joystick;
        }

    }

    /** The joystick is already closed by the time this fires, so only its id is left. */
    public static class Removed extends JoystickEvent {

        public Removed(int instanceId, long timestampNs) {
            super(instanceId, timestampNs);
        }

    }

    public static class Battery extends JoystickEvent {

        private final Joystick joystick;
        private final Power.State state;
        private final int percent;

        public Battery(long timestampNs, Joystick joystick, Power.State state, int percent) {
            super(joystick.id(), timestampNs);
            this.joystick = joystick;
            this.state = state;
            this.percent = percent;
        }

        public Joystick joystick() {
            return this.joystick;
        }

        public Power.State state() {
            return this.state;
        }

        /** @return charge left, or {@code -1} when the platform does not report one */
        public int percent() {
            return this.percent;
        }

    }

}

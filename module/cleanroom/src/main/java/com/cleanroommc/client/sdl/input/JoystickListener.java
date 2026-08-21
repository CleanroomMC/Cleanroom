package com.cleanroommc.client.sdl.input;

import com.cleanroommc.client.sdl.Power;

/** Hotplug and battery for raw joysticks. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface JoystickListener {

    default void added(Joystick joystick) { }

    default void removed(int instanceId) { }

    default void battery(Joystick joystick, Power.State state, int percent) { }

}

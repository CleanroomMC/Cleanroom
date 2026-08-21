package com.cleanroommc.client.sdl.input;

/** Hotplug for mapped gamepads. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface GamepadListener {

    default void added(Gamepad gamepad) { }

    default void removed(int instanceId) { }

    default void remapped(Gamepad gamepad) { }

    default void touchpad(Gamepad gamepad, int touchpad, int finger, float x, float y, float pressure, boolean down) { }

    default void sensor(Gamepad gamepad, SensorType type, float[] data) { }

}

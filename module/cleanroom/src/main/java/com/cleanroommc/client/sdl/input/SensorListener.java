package com.cleanroommc.client.sdl.input;

/** Hotplug for standalone sensors. Called from {@link com.cleanroommc.client.sdl.Window#pump()}. */
public interface SensorListener {

    default void added(Sensor sensor) { }

    default void removed(int id) { }

    default void updated(Sensor sensor, float[] data) { }

}

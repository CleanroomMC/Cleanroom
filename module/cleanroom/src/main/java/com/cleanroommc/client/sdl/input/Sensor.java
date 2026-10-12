/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.input;

import org.lwjgl.sdl.SDLSensor;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

/** One standalone sensor (accel/gyro that is not hanging off a {@link Gamepad}). */
public final class Sensor {

    private static final int DATA_COUNT = 3;

    private final int id;
    private final long handle;

    Sensor(int id, long handle) {
        this.id = id;
        this.handle = handle;
    }

    public int id() {
        return id;
    }

    public String name() {
        String name = SDLSensor.SDL_GetSensorName(handle);
        return name == null ? "" : name;
    }

    public SensorType type() {
        return SensorType.of(SDLSensor.SDL_GetSensorType(handle));
    }

    /**
     * @return three floats: accel is in m/s², gyro is in rad/s
     */
    public float[] data() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer buffer = stack.mallocFloat(DATA_COUNT);
            if (!SDLSensor.SDL_GetSensorData(handle, buffer)) {
                return new float[DATA_COUNT];
            }
            float[] values = new float[DATA_COUNT];
            buffer.get(values);
            return values;
        }
    }

    long handle() {
        return handle;
    }

}

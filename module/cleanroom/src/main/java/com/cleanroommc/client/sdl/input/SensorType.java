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

/** SDL sensor kinds, including left/right Joy-Con variants. */
public enum SensorType {

    INVALID(SDLSensor.SDL_SENSOR_INVALID),
    UNKNOWN(SDLSensor.SDL_SENSOR_UNKNOWN),
    ACCEL(SDLSensor.SDL_SENSOR_ACCEL),
    GYRO(SDLSensor.SDL_SENSOR_GYRO),
    ACCEL_L(SDLSensor.SDL_SENSOR_ACCEL_L),
    GYRO_L(SDLSensor.SDL_SENSOR_GYRO_L),
    ACCEL_R(SDLSensor.SDL_SENSOR_ACCEL_R),
    GYRO_R(SDLSensor.SDL_SENSOR_GYRO_R);

    private final int value;

    SensorType(int value) {
        this.value = value;
    }

    public static SensorType of(int value) {
        return switch (value) {
            case SDLSensor.SDL_SENSOR_INVALID -> INVALID;
            case SDLSensor.SDL_SENSOR_ACCEL -> ACCEL;
            case SDLSensor.SDL_SENSOR_GYRO -> GYRO;
            case SDLSensor.SDL_SENSOR_ACCEL_L -> ACCEL_L;
            case SDLSensor.SDL_SENSOR_GYRO_L -> GYRO_L;
            case SDLSensor.SDL_SENSOR_ACCEL_R -> ACCEL_R;
            case SDLSensor.SDL_SENSOR_GYRO_R -> GYRO_R;
            default -> UNKNOWN;
        };
    }

    public int value() {
        return value;
    }

}

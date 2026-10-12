/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.sdl.hid;

import org.lwjgl.sdl.SDLHIDAPI;

/** Bus a HID device is attached to. */
public enum HidBus {

    UNKNOWN(SDLHIDAPI.SDL_HID_API_BUS_UNKNOWN),
    USB(SDLHIDAPI.SDL_HID_API_BUS_USB),
    BLUETOOTH(SDLHIDAPI.SDL_HID_API_BUS_BLUETOOTH),
    I2C(SDLHIDAPI.SDL_HID_API_BUS_I2C),
    SPI(SDLHIDAPI.SDL_HID_API_BUS_SPI);

    private final int value;

    HidBus(int value) {
        this.value = value;
    }

    public static HidBus of(int value) {
        return switch (value) {
            case SDLHIDAPI.SDL_HID_API_BUS_USB -> USB;
            case SDLHIDAPI.SDL_HID_API_BUS_BLUETOOTH -> BLUETOOTH;
            case SDLHIDAPI.SDL_HID_API_BUS_I2C -> I2C;
            case SDLHIDAPI.SDL_HID_API_BUS_SPI -> SPI;
            default -> UNKNOWN;
        };
    }

    public int value() {
        return value;
    }

}

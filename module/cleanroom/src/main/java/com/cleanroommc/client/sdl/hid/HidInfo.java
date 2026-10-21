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

/**
 * One enumerated HID device. Vendor/product are unsigned 16-bits.
 */
public record HidInfo(
    String path,
    int vendor,
    int product,
    String serial,
    String manufacturer,
    String productName,
    int usagePage,
    int usage,
    HidBus bus) { }

/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.common.component;

/**
 * Thrown by {@link NbtCodec#read} when saved component data cannot be decoded for the value type
 * the codec produces. A failed component is dropped, not fatal; see the {@code ItemStack} compound
 * read path.
 */
public class ComponentFormatException extends Exception {

    public ComponentFormatException(String message) {
        super(message);
    }

    public ComponentFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}

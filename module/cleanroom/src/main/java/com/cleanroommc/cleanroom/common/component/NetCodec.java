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

import net.minecraft.network.PacketBuffer;

/**
 * Network form of a {@link DataComponentType}: how a value is written to and read back from a
 * {@link PacketBuffer} item-stack slot. A type without a {@code NetCodec} does not travel over the network.
 */
public interface NetCodec<T> {

    void write(PacketBuffer buf, T value);

    T read(PacketBuffer buf);

}

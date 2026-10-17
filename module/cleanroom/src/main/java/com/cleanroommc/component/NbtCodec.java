/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.component;

import net.minecraft.nbt.NBTBase;

/**
 * Persistent form of a {@link DataComponentType}: how a value is written to and read back from
 * an {@code ItemStack}'s {@code Components} compound. A type without an {@code NbtCodec} is not saved.
 */
public interface NbtCodec<T> {

    NBTBase write(T value);

    T read(NBTBase nbt) throws ComponentFormatException;

}

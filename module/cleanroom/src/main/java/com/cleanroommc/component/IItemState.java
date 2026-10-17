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

import com.google.common.collect.ImmutableMap;

import net.minecraft.block.properties.IProperty;
import net.minecraft.item.Item;

import java.util.Collection;

/**
 * First-class item state standing by itself, mirroring {@link net.minecraft.block.state.IBlockState}.
 * An {@link Item} acts as the template for one or more {@code IItemState} instances; each valid
 * state has a per-item ordinal ({@link #getOrdinal()}) and a dense runtime ID in
 * {@code Item.ITEM_STATE_IDS} ({@link #getStateId()}).
 */
public interface IItemState {

    Collection<IProperty<?>> getPropertyKeys();

    <T extends Comparable<T>> T getValue(IProperty<T> property);

    <T extends Comparable<T>,
    V extends T> IItemState withProperty(IProperty<T> property, V value);

    <T extends Comparable<T>> IItemState cycleProperty(IProperty<T> property);

    ImmutableMap<IProperty<?>, Comparable<?>> getProperties();

    default int getOrdinal() {
        return getItem().getItemState().getValidStates().indexOf(this);
    }

    default int getStateId() {
        return Item.getStateId(this);
    }

    Item getItem();

}

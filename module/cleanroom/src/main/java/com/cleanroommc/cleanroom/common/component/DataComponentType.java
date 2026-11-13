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

import org.jspecify.annotations.Nullable;

import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * A typed slot an {@code ItemStack} can carry, identified by a registry name. Component values are
 * immutable and implement {@code equals}/{@code hashCode}; stacks compare them by value and share
 * them by reference.
 * <p>
 * The codecs are independent: a type with only an {@link NbtCodec} is saved but never sent, a type
 * with only a {@link NetCodec} is sent but never saved, and a type with neither would be inert, so at
 * least one is required.
 */
public final class DataComponentType<T> extends IForgeRegistryEntry.Impl<DataComponentType<?>> {

    @Nullable private final NbtCodec<T> nbtCodec;
    @Nullable private final NetCodec<T> netCodec;

    private DataComponentType(@Nullable NbtCodec<T> nbtCodec, @Nullable NetCodec<T> netCodec) {
        this.nbtCodec = nbtCodec;
        this.netCodec = netCodec;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Nullable
    public NbtCodec<T> nbtCodec() {
        return nbtCodec;
    }

    @Nullable
    public NetCodec<T> netCodec() {
        return netCodec;
    }

    public boolean isPersistent() {
        return nbtCodec != null;
    }

    public boolean isNetworkSynchronized() {
        return netCodec != null;
    }

    @Override
    public String toString() {
        return String.valueOf(getRegistryName());
    }

    public static final class Builder<T> {

        @Nullable private NbtCodec<T> nbtCodec;
        @Nullable private NetCodec<T> netCodec;

        private Builder() { }

        public Builder<T> persistent(NbtCodec<T> codec) {
            this.nbtCodec = codec;
            return this;
        }

        public Builder<T> networkSynchronized(NetCodec<T> codec) {
            this.netCodec = codec;
            return this;
        }

        public DataComponentType<T> build() {
            if (nbtCodec == null && netCodec == null) {
                throw new IllegalStateException("A data component type needs at least one codec: persistent(NbtCodec) and/or networkSynchronized(NetCodec)"
                );
            }
            return new DataComponentType<>(nbtCodec, netCodec);
        }

    }

}

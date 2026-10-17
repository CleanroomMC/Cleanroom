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

import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/**
 * An immutable, read-only set of component values keyed by {@link DataComponentType}. Items use one
 * as their prototype of default components; a stack stores only a patch on top of it
 * ({@link PatchedDataComponentMap}). Values are immutable and shared, never copied.
 */
public interface DataComponentMap {

    DataComponentMap EMPTY = new SimpleDataComponentMap(new Reference2ObjectArrayMap<>());

    static Builder builder() {
        return new Builder();
    }

    @Nullable
    <T> T get(DataComponentType<? extends T> type);

    default <T> T getOrDefault(DataComponentType<? extends T> type, T fallback) {
        T value = get(type);
        return value != null ? value : fallback;
    }

    boolean has(DataComponentType<?> type);

    Set<DataComponentType<?>> keySet();

    int size();

    default boolean isEmpty() {
        return size() == 0;
    }

    final class Builder {

        private final Reference2ObjectMap<DataComponentType<?>, Object> values = new Reference2ObjectArrayMap<>();

        private Builder() { }

        public <T> Builder set(DataComponentType<T> type, T value) {
            values.put(type, value);
            return this;
        }

        public Builder addAll(DataComponentMap map) {
            for (DataComponentType<?> type : map.keySet()) {
                values.put(type, map.get(type));
            }
            return this;
        }

        public DataComponentMap build() {
            if (values.isEmpty()) {
                return EMPTY;
            }
            return new SimpleDataComponentMap(new Reference2ObjectArrayMap<>(values));
        }

    }

}

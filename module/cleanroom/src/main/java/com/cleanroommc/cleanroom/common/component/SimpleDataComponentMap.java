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

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import org.jspecify.annotations.Nullable;

import java.util.Set;

/** The immutable {@link DataComponentMap} built by {@link DataComponentMap.Builder}. */
final class SimpleDataComponentMap implements DataComponentMap {

    private final Reference2ObjectMap<DataComponentType<?>, Object> values;

    SimpleDataComponentMap(Reference2ObjectMap<DataComponentType<?>, Object> values) {
        this.values = values;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(DataComponentType<? extends T> type) {
        return (T) values.get(type);
    }

    @Override
    public boolean has(DataComponentType<?> type) {
        return values.containsKey(type);
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        return values.keySet();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this ||
            (obj instanceof SimpleDataComponentMap && values.equals(((SimpleDataComponentMap) obj).values));
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

}

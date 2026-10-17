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
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.UnaryOperator;

/**
 * A stack's components: an item's prototype {@link DataComponentMap} plus a patch of this stack's
 * differences. A patch entry holds {@code Optional.of(value)} for an override/addition and
 * {@code Optional.empty()} for the removal of a prototype default — the two are distinct so a
 * removed default stays removed (and round-trips) instead of falling back to the prototype.
 * <p>
 * {@code equals}/{@code hashCode} compare the patch by value and the prototype by identity: two
 * stacks of the same item share the same prototype instance.
 */
public class PatchedDataComponentMap implements DataComponentMap {

    private final DataComponentMap prototype;
    private final Reference2ObjectArrayMap<DataComponentType<?>, Optional<?>> patch;

    public PatchedDataComponentMap(DataComponentMap prototype) {
        this.prototype = prototype;
        this.patch = new Reference2ObjectArrayMap<>();
    }

    private PatchedDataComponentMap(DataComponentMap prototype,
        Reference2ObjectArrayMap<DataComponentType<?>, Optional<?>> patch
    ) {
        this.prototype = prototype;
        this.patch = patch;
    }

    /* The item's default components; identical (==) for two stacks of the same item. */
    public DataComponentMap getPrototype() {
        return prototype;
    }

    @Nullable
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(DataComponentType<? extends T> type) {
        Optional<?> entry = patch.get(type);
        if (entry != null) {
            return (T) entry.orElse(null);
        }
        return prototype.get(type);
    }

    @Override
    public boolean has(DataComponentType<?> type) {
        Optional<?> entry = patch.get(type);
        if (entry != null) {
            return entry.isPresent();
        }
        return prototype.has(type);
    }

    @Override
    public Set<DataComponentType<?>> keySet() {
        Set<DataComponentType<?>> keys = new ReferenceOpenHashSet<>(prototype.keySet());
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : patch.entrySet()) {
            if (entry.getValue().isPresent()) {
                keys.add(entry.getKey());
            } else {
                keys.remove(entry.getKey());
            }
        }
        return keys;
    }

    @Override
    public int size() {
        return keySet().size();
    }

    /**
     * Sets or overrides a component. {@code null} is a removal of a prototype default. A value equal
     * to the prototype's collapses to no patch entry at all.
     *
     * @param <T> component value type
     * @param type component type
     * @param value component value, or null to remove
     * @return the value in effect before this call
     */
    @Nullable
    public <T> T set(DataComponentType<? extends T> type, @Nullable T value) {
        T previous = get(type);
        if (value == null) {
            if (prototype.has(type)) {
                patch.put(type, Optional.empty());
            } else {
                patch.remove(type);
            }
        } else if (value.equals(prototype.get(type))) {
            patch.remove(type);
        } else {
            patch.put(type, Optional.of(value));
        }
        return previous;
    }

    /* Removes a component: a prototype default becomes a removal entry, a patch value disappears. */
    public <T> void remove(DataComponentType<? extends T> type) {
        set(type, null);
    }

    /* {@code getOrDefault(type, fallback)}, transformed, then {@link #set}. Returns the new value. */
    public <T> T update(DataComponentType<T> type, T fallback, UnaryOperator<T> updater) {
        T value = updater.apply(getOrDefault(type, fallback));
        set(type, value);
        return value;
    }

    /* The differences from the prototype, {@code Optional.empty()} marking a removed default. */
    public Reference2ObjectArrayMap<DataComponentType<?>, Optional<?>> asPatch() {
        return patch.clone();
    }

    /* Restores patch entries read from a saved {@code Components} compound; merges onto the current patch. */
    public void restorePatch(Map<DataComponentType<?>, Optional<?>> entries) {
        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : entries.entrySet()) {
            Optional<?> value = entry.getValue();
            if (value != null && value.isPresent() && value.get().equals(prototype.get(entry.getKey()))) {
                patch.remove(entry.getKey());
            } else {
                patch.put(entry.getKey(), value);
            }
        }
    }

    /* An independent map: same prototype, same patch contents, no shared patch state. */
    public PatchedDataComponentMap copy() {
        return new PatchedDataComponentMap(prototype, patch.clone());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PatchedDataComponentMap)) {
            return false;
        }
        PatchedDataComponentMap other = (PatchedDataComponentMap) obj;
        return prototype == other.prototype && patch.equals(other.patch);
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(prototype) * 31 + patch.hashCode();
    }

}

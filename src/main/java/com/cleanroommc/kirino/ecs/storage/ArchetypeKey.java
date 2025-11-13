package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * It's a runtime-only key.
 * Must not be used for persistent or network related stuff.
 * Thread safety is guaranteed.
 */
public final class ArchetypeKey {
    private static final Map<String, Integer> map = new HashMap<>();
    private static int nextId = 0;

    public static synchronized int getIdForKey(String key) {
        return map.computeIfAbsent(key, k -> nextId++);
    }

    public final int id;
    public final String key;

    public ArchetypeKey(List<Class<? extends ICleanComponent>> components) {
        List<String> names = components.stream().map(Class::getName).sorted().toList();
        String combined = String.join(",", names);
        id = getIdForKey(combined);
        key = combined;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArchetypeKey that)) {
            return false;
        }

        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "ArchetypeKey{ id=" + id + " }";
    }

    public boolean contains(Class<? extends ICleanComponent> component) {
        return key.contains(component.getName());
    }
}

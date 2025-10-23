package com.cleanroommc.kirino.schemata.semantic.space;

import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItemType;
import org.jspecify.annotations.NonNull;

import java.util.*;

public class SpaceSet implements Set<SpaceItem> {
    public final SpaceItemType type;
    private final Set<SpaceItem> items = new HashSet<>();

    public SpaceSet(SpaceItemType type) {
        this.type = type;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return items.contains(o);
    }

    @NonNull
    @Override
    public Iterator<SpaceItem> iterator() {
        return items.iterator();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return items.toArray(new SpaceItem[0]);
    }

    @NonNull
    @Override
    public <T> T[] toArray(@NonNull T[] a) {
        return items.toArray(a);
    }

    @Override
    public boolean add(SpaceItem item) {
        if (item.type != type) {
            throw new IllegalStateException("Illegal SpaceItemType " + item.type + " from the item. This is a " + type + " set.");
        }
        return items.add(item);
    }

    @Override
    public boolean remove(Object o) {
        return items.remove(o);
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        return items.containsAll(c);
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends SpaceItem> c) {
        for (SpaceItem item : c) {
            if (item.type != type) {
                throw new IllegalStateException("Illegal SpaceItemType " + item.type + " from an item of the collection. This is a " + type + " set.");
            }
        }

        return items.addAll(c);
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        return items.retainAll(c);
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        return items.removeAll(c);
    }

    @Override
    public void clear() {
        items.clear();
    }
}

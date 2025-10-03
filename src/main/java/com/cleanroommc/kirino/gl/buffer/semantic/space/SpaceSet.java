package com.cleanroommc.kirino.gl.buffer.semantic.space;

import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItemType;

import java.util.ArrayList;
import java.util.List;

public class SpaceSet {
    public final SpaceItemType type;
    private final List<SpaceItem> items = new ArrayList<>();

    public SpaceSet(SpaceItemType type) {
        this.type = type;
    }

    public void addItem(SpaceItem item) {
        if (item.type != type) {
            throw new IllegalStateException("Illegal SpaceItemType " + item.type + " from the item. This is a " + type + " set.");
        }
        items.add(item);
    }

    public void removeItem(SpaceItem item) {
        items.remove(item);
    }

    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
    }

    public SpaceItem[] getItems() {
        return items.toArray(new SpaceItem[0]);
    }

    public boolean contains(SpaceItem item) {
        return items.contains(item);
    }
}

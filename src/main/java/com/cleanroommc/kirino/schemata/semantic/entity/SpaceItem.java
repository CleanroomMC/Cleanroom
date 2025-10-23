package com.cleanroommc.kirino.schemata.semantic.entity;

import java.util.Objects;

public class SpaceItem {
    public final SpaceItemType type;

    // OBJECT
    public final int size;
    public final Object payload;

    // DATA_SLICE
    public final int offset;
    public final int length;

    // payload is part of the uniqueness of this SpaceItem
    public SpaceItem(int size, Object payload) {
        type = SpaceItemType.OBJECT;
        this.size = size;
        this.payload = payload;
        offset = -1;
        length = -1;
    }

    public SpaceItem(int offset, int length) {
        type = SpaceItemType.DATA_SLICE;
        size = -1;
        payload = null;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SpaceItem spaceItem)) {
            return false;
        }
        if (type != spaceItem.type) {
            return false;
        }
        if (spaceItem.type == SpaceItemType.DATA_SLICE) {
            return offset == spaceItem.offset && length == spaceItem.length;
        } else if (spaceItem.type == SpaceItemType.OBJECT) {
            return size == spaceItem.size && Objects.equals(payload, spaceItem.payload);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, size, payload, offset, length);
    }
}

package com.cleanroommc.kirino.gl.buffer.semantic.entity;

public class SpaceItem {
    public final SpaceItemType type;

    // OBJECT
    public final int size;
    public final Object payload;

    // DATA_SLICE
    public final int offset;
    public final int length;

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
}

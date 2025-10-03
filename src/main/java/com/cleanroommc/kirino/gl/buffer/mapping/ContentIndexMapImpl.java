package com.cleanroommc.kirino.gl.buffer.mapping;

import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.gl.buffer.semantic.morph.BijectiveMapFunction;

public class ContentIndexMapImpl extends BijectiveMapFunction {
    public final int bufferSize;

    public ContentIndexMapImpl(int bufferSize) {
        super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE, ContentIndexMapImpl::forward, ContentIndexMapImpl::backward);
        this.bufferSize = bufferSize;
    }

    private static SpaceItem forward(SpaceItem item) {
        // todo
        return null;
    }

    private static SpaceItem backward(SpaceItem item) {
        // todo
        return null;
    }
}

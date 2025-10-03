package com.cleanroommc.kirino.gl.buffer.mapping;

import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.gl.buffer.semantic.morph.BijectiveMapFunction;
import com.cleanroommc.kirino.gl.buffer.semantic.morph.Morphism;
import com.cleanroommc.kirino.gl.buffer.semantic.space.SpaceSet;

import java.util.function.Consumer;

public class BufferMorphism extends Morphism {
    public BufferMorphism(int bufferSize) {
        super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE);
        turnOffValidation();
        setMapFunc(new ContentIndexMapImpl(bufferSize));
    }

    public BufferMorphism(BijectiveMapFunction mapFunc) {
        super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE);
        turnOffValidation();
        setMapFunc(mapFunc);
    }

    public void update(Consumer<SpaceSet> action) {
        modifyDomain(action);
    }

    public void build() {
        mapAndCache();
    }
}

package com.cleanroommc.kirino.gl.buffer.mapping;

import com.cleanroommc.kirino.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.semantic.morph.Morphism;

public class Buf2DrawMorphism extends Morphism {
    public Buf2DrawMorphism() {
        super(SpaceItemType.DATA_SLICE, SpaceItemType.OBJECT);
    }
}

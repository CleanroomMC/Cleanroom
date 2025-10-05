package com.cleanroommc.kirino.gl.buffer.mapping;

import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.schemata.semantic.morph.BijectiveMapFunction;
import com.cleanroommc.kirino.schemata.semantic.morph.Morphism;
import com.cleanroommc.kirino.schemata.semantic.space.SpaceSet;

import java.util.function.Consumer;

public class RenderObj2BufMorphism extends Morphism {
    public RenderObj2BufMorphism(int bufferSize) {
        super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE);
        setMapFunc(new ContentIndexMapImpl(bufferSize));
        turnOffValidation();
    }

    public RenderObj2BufMorphism(BijectiveMapFunction mapFunc) {
        super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE);
        setMapFunc(mapFunc);
        turnOffValidation();
    }

    public RenderObj2BufMorphism update(Consumer<SpaceSet> action) {
        modifyDomain(action);
        return this;
    }

    public RenderObj2BufMorphism build() {
        mapAndCache();
        return this;
    }

    public static class ContentIndexMapImpl extends BijectiveMapFunction {
        public final int bufferSize;

        public ContentIndexMapImpl(int bufferSize) {
            super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE, ContentIndexMapImpl::func);
            this.bufferSize = bufferSize;
        }

        private static SpaceItem func(SpaceItem item) {
            // todo
            return null;
        }

        @Override
        public SpaceSet construct(SpaceSet domain) {
            SpaceSet codomain = new SpaceSet(SpaceItemType.DATA_SLICE);
            return codomain;
        }
    }
}

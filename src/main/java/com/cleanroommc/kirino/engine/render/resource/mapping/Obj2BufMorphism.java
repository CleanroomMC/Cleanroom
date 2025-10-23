package com.cleanroommc.kirino.engine.render.resource.mapping;

import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.schemata.semantic.morph.BijectiveMapFunction;
import com.cleanroommc.kirino.schemata.semantic.morph.Morphism;
import com.cleanroommc.kirino.schemata.semantic.space.SpaceSet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Obj2BufMorphism extends Morphism {
    public final int bufferSize;
    private final ContentIndexMapImpl mapFunc;

    public int getMinimizedBufferSize() {
        return mapFunc.minimizedBufferSize;
    }

    public Obj2BufMorphism(int bufferSize) {
        super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE);
        this.bufferSize = bufferSize;
        mapFunc = new ContentIndexMapImpl(bufferSize);
        setMapFunc(mapFunc);
        turnOffValidation();
    }

    public Obj2BufMorphism update(Consumer<SpaceSet> action) {
        modifyDomain(action);
        return this;
    }

    public Obj2BufMorphism build() {
        mapAndCache();
        return this;
    }

    public static class ContentIndexMapImpl extends BijectiveMapFunction {
        public final int bufferSize;
        private int minimizedBufferSize = -1;
        private final Map<SpaceItem, SpaceItem> map = new HashMap<>();

        public ContentIndexMapImpl(int bufferSize) {
            super(SpaceItemType.OBJECT, SpaceItemType.DATA_SLICE);
            setFunc(map::get);
            this.bufferSize = bufferSize;
        }

        @Override
        public SpaceSet construct(SpaceSet domain) {
            SpaceSet codomain = new SpaceSet(SpaceItemType.DATA_SLICE);

            map.clear();
            int pointer = 0;
            for (SpaceItem obj : domain) {
                if (pointer + obj.size > bufferSize) {
                    throw new IllegalStateException("Failed to construct the mapping function. Cumulative domain size is exceeding the allocated size " + bufferSize + ".");
                }

                SpaceItem slice = new SpaceItem(pointer, obj.size);
                pointer += obj.size;
                map.put(obj, slice);
                codomain.add(slice);
            }
            minimizedBufferSize = pointer;

            return codomain;
        }
    }
}

package com.cleanroommc.kirino.gl.buffer.semantic.morph;

import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItemType;

import java.util.function.Function;

public class BijectiveMapFunction {
    public final SpaceItemType from;
    public final SpaceItemType to;
    public final Function<SpaceItem, SpaceItem> func;
    public final Function<SpaceItem, SpaceItem> funcInv;

    public BijectiveMapFunction(SpaceItemType from, SpaceItemType to, Function<SpaceItem, SpaceItem> func, Function<SpaceItem, SpaceItem> funcInv) {
        this.from = from;
        this.to = to;
        this.func = func;
        this.funcInv = funcInv;
    }

    public final BijectiveMapFunction inverse() {
        return new BijectiveMapFunction(to, from, funcInv, func);
    }

    public final BijectiveMapFunction compose(BijectiveMapFunction other) {
        if (to != other.from) {
            throw new IllegalStateException("Can't compose " + from + " -> " + to + " and " + other.from + " -> " + other.to + ".");
        }
        return new BijectiveMapFunction(from, other.to,
                item -> other.func.apply(func.apply(item)),
                item -> funcInv.apply(other.funcInv.apply(item)));
    }
}

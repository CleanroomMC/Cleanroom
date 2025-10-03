package com.cleanroommc.kirino.gl.buffer.semantic.morph;

import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.gl.buffer.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.gl.buffer.semantic.space.SpaceSet;
import com.cleanroommc.kirino.utils.ReflectionUtils;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Morphism {
    private final static Function<SpaceSet, List<SpaceItem>> SPACE_SET_ITEMS_GETTER;

    static {
        Object getter = Objects.requireNonNull(ReflectionUtils.getDeclaredFieldGetter(SpaceSet.class, "items"));
        SPACE_SET_ITEMS_GETTER = (Function<SpaceSet, List<SpaceItem>>) getter;
    }

    private boolean validation = true;

    public final void turnOnValidation() {
        validation = true;
    }

    public final void turnOffValidation() {
        validation = false;
    }

    public final boolean isValidationOn() {
        return validation;
    }

    public final SpaceItemType domainType;
    public final SpaceItemType codomainType;
    private final SpaceSet domain;
    public final SpaceSet codomain;

    protected Morphism(SpaceItemType domainType, SpaceItemType codomainType) {
        this.domainType = domainType;
        this.codomainType = codomainType;
        domain = new SpaceSet(domainType);
        codomain = new SpaceSet(codomainType);
    }

    private boolean resultCached = false;
    private final BiMap<SpaceItem, SpaceItem> resultBiMap = HashBiMap.create();

    private BijectiveMapFunction mapFunc = null;

    protected final Morphism modifyDomain(Consumer<SpaceSet> action) {
        action.accept(domain);
        codomain.clear();
        resultBiMap.clear();
        resultCached = false;
        return this;
    }

    protected final Morphism setMapFunc(BijectiveMapFunction mapFunc) {
        if (validation) {
            if (mapFunc.from != domainType || mapFunc.to != codomainType) {
                throw new IllegalStateException("Illegal mapping function " + mapFunc.from + " -> " + mapFunc.to + ". This morphism requires " + domainType + " -> " + codomainType + ".");
            }
        }

        this.mapFunc = mapFunc;
        codomain.clear();
        resultBiMap.clear();
        resultCached = false;
        return this;
    }

    protected final Morphism mapAndCache() {
        if (validation) {
            if (mapFunc == null) {
                throw new IllegalStateException("BijectiveMapFunction is null. Call setMapFunc() first.");
            }
        }

        for (SpaceItem item : SPACE_SET_ITEMS_GETTER.apply(domain)) {
            resultBiMap.put(item, mapFunc.func.apply(item));
        }
        resultCached = true;
        return this;
    }

    public final SpaceItem apply(SpaceItem input) {
        if (validation) {
            if (!resultCached) {
                throw new IllegalStateException("The mapping result isn't cached yet. Call mapAndCache() first.");
            }
            if (!domain.contains(input)) {
                throw new IllegalStateException("Input isn't in the domain.");
            }
        }

        return resultBiMap.get(input);
    }

    public final SpaceItem inverse(SpaceItem input) {
        if (validation) {
            if (!resultCached) {
                throw new IllegalStateException("The mapping result isn't cached yet. Call mapAndCache() first.");
            }
            if (!codomain.contains(input)) {
                throw new IllegalStateException("Input isn't in the codomain.");
            }
        }

        return resultBiMap.inverse().get(input);
    }
}

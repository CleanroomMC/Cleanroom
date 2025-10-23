package com.cleanroommc.kirino.schemata.semantic.morph;

import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.schemata.semantic.space.SpaceSet;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.function.Consumer;

/**
 * It represents a bijective morphism between two {@link SpaceItemType}s by maintaining the domain and codomain sets as well as a cached bidirectional mapping.
 * That is, this is more like a container that runs your {@link BijectiveMapFunction}.
 */
public abstract class Morphism {
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
    private final SpaceSet codomain;

    public SpaceItem[] getDomain() {
        return domain.toArray(new SpaceItem[0]);
    }

    public SpaceItem[] getCodomain() {
        return codomain.toArray(new SpaceItem[0]);
    }

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
            if (!mapFunc.isValid()) {
                throw new IllegalStateException("Illegal mapping function.");
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

        mapFunc.construct(domain);
        for (SpaceItem item : domain) {
            SpaceItem result = mapFunc.apply(item);
            codomain.add(result);
            resultBiMap.put(item, result);
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
                throw new IllegalStateException("Argument input isn't in the domain.");
            }
        }

        return resultBiMap.get(input);
    }

    public final SpaceItem applyInverse(SpaceItem output) {
        if (validation) {
            if (!resultCached) {
                throw new IllegalStateException("The mapping result isn't cached yet. Call mapAndCache() first.");
            }
            if (!codomain.contains(output)) {
                throw new IllegalStateException("Argument output isn't in the codomain.");
            }
        }

        return resultBiMap.inverse().get(output);
    }
}

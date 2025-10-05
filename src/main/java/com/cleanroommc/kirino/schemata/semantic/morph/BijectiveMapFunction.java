package com.cleanroommc.kirino.schemata.semantic.morph;

import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.schemata.semantic.space.SpaceSet;

import java.util.function.Function;

/**
 * It represents a bijective (one-to-one and onto) mapping between two {@link SpaceItemType}s.
 * You don't need to worry about the "onto" property since we construct our codomain afterward (see {@link #construct(SpaceSet)}).
 * However, you must guarantee that the function is one-to-one.
 */
public abstract class BijectiveMapFunction {
    /**
     * The source type (domain) of this mapping.
     */
    public final SpaceItemType from;

    /**
     * The target type (codomain) of this mapping.
     */
    public final SpaceItemType to;

    private final Function<SpaceItem, SpaceItem> func;

    /**
     * @param from The input {@link SpaceItemType} (domain)
     * @param to The output {@link SpaceItemType} (codomain)
     * @param func A function that maps one {@link SpaceItem} to another
     */
    public BijectiveMapFunction(SpaceItemType from, SpaceItemType to, Function<SpaceItem, SpaceItem> func) {
        this.from = from;
        this.to = to;
        this.func = func;
    }

    public final BijectiveMapFunction compose(BijectiveMapFunction other) {
        if (to != other.from) {
            throw new IllegalStateException("Can't compose " + from + " -> " + to + " and " + other.from + " -> " + other.to + ".");
        }
        BijectiveMapFunction this0 = this;
        return new BijectiveMapFunction(from, other.to, item -> other.func.apply(func.apply(item))) {
            @Override
            public SpaceSet construct(SpaceSet domain) {
                return other.construct(this0.construct(domain));
            }
        };
    }

    /**
     * Performs any domain-dependent initialization and returns the corresponding codomain {@link SpaceSet}.
     * That is, you are not forced to create stateless/closed-form functions.
     * This method is guaranteed to be called before {@link #apply(SpaceItem)}.
     *
     * @param domain The domain
     * @return The codomain
     */
    public abstract SpaceSet construct(SpaceSet domain);

    public final SpaceItem apply(SpaceItem item) {
        return func.apply(item);
    }
}

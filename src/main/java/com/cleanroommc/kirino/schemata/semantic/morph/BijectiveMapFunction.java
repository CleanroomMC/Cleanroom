package com.cleanroommc.kirino.schemata.semantic.morph;

import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItem;
import com.cleanroommc.kirino.schemata.semantic.entity.SpaceItemType;
import com.cleanroommc.kirino.schemata.semantic.space.SpaceSet;
import com.google.common.base.Preconditions;

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

    private Function<SpaceItem, SpaceItem> func;
    private boolean valid = false;

    public boolean isValid() {
        return valid;
    }

    /**
     * @param from The input {@link SpaceItemType} (domain)
     * @param to The output {@link SpaceItemType} (codomain)
     */
    public BijectiveMapFunction(SpaceItemType from, SpaceItemType to) {
        this.from = from;
        this.to = to;
    }

    /**
     * This method must be called after the constructor.
     *
     * @param func A function that maps one {@link SpaceItem} to another
     * @return Itself
     */
    protected final BijectiveMapFunction setFunc(Function<SpaceItem, SpaceItem> func) {
        this.func = func;
        valid = true;
        return this;
    }

    public final BijectiveMapFunction compose(BijectiveMapFunction other) {
        Preconditions.checkState(to == other.from,
                "Can't compose %s -> %s and %s -> %s.", from, to, other.from, other.to);

        BijectiveMapFunction this0 = this;
        return (new BijectiveMapFunction(from, other.to) {
            @Override
            public SpaceSet construct(SpaceSet domain) {
                return other.construct(this0.construct(domain));
            }
        }).setFunc(item -> other.func.apply(func.apply(item)));
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

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The input item is in the domain that passed to {@link #construct(SpaceSet)}</li>
     * </ul>
     *
     * @param item The input item
     * @return The output item
     */
    public final SpaceItem apply(SpaceItem item) {
        return func.apply(item);
    }
}

package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public abstract class ArchetypeDataPool {
    public final ImmutableList<Class<? extends ICleanComponent>> components;

    protected int currentSize;
    public final int initSize;
    public final int growStep;
    public final int shrinkStep;

    protected final ComponentRegistry componentRegistry;

    /**
     * The number of entities this pool can contain
     *
     * @return The current pool size
     */
    public final int getCurrentSize() {
        return currentSize;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     * </ul>
     *
     * @param componentRegistry The component registry
     * @param components The component types for this archetype
     * @param initSize The number entities this pool can contain initially
     * @param growStep The size to grow when the pool is full
     * @param shrinkStep The threshold to judge when the pool is too large; Also the size to shrink
     */
    public ArchetypeDataPool(ComponentRegistry componentRegistry, List<Class<? extends ICleanComponent>> components, int initSize, int growStep, int shrinkStep) {
        this.componentRegistry = componentRegistry;
        this.components = ImmutableList.copyOf(components);
        this.initSize = initSize;
        this.growStep = growStep;
        this.shrinkStep = shrinkStep;
        currentSize = initSize;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li><code>clazz</code> is in {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     * @param clazz The component class
     * @return The instantiated component
     */
    @NonNull
    public abstract ICleanComponent getComponent(int entityID, Class<? extends ICleanComponent> clazz);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li>Entries from <code>componentArgs</code> corresponds to and establishes a bijection with {@link ArchetypeDataPool#components}</li>
     *     <li>Component arguments are in correct types and length</li>
     * </ul>
     *
     * @param entityID The id of the entity
     * @param componentArgs The data for each component type
     */
    public abstract void addEntity(int entityID, Map<Class<? extends ICleanComponent>, Object[]> componentArgs);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     */
    public abstract void removeEntity(int entityID);

    // todo: defragmentation?
}

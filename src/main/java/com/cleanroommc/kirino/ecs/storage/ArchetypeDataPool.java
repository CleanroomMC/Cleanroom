package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Besides the abstract methods, a pool must implement grow and shrink mechanism.
 *
 * @see #growStep
 * @see #shrinkStep
 */
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

    public abstract boolean containsEntity(int entityID);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li><code>component</code> is in {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     * @param component The component class
     * @return The instantiated component
     */
    @NonNull
    public abstract ICleanComponent getComponent(int entityID, Class<? extends ICleanComponent> component);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li>The class of <code>component</code> is in {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     * @param component The component
     */
    public abstract void setComponent(int entityID, ICleanComponent component);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     *     <li>Classes of <code>components</code> must correspond to and form a bijection with {@link ArchetypeDataPool#components}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     * @param components The components
     */
    public abstract void addEntity(int entityID, List<ICleanComponent> components);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The entity represented by <code>entityID</code> is in this {@link ArchetypeDataPool}</li>
     * </ul>
     *
     * @param entityID The id of the entity
     */
    public abstract void removeEntity(int entityID);

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li><code>component</code> is in {@link ArchetypeDataPool#components}</li>
     *     <li><code>fieldAccessChain</code> points to a valid field</li>
     * </ul>
     *
     * @param component The component class
     * @param fieldAccessChain A list of field names to identify a field
     * @return An array of field values
     */
    public abstract IPrimitiveArray getArray(Class<? extends ICleanComponent> component, String... fieldAccessChain);

    public abstract ArrayRange getArrayRange();

    public abstract String getSnapshot();
}

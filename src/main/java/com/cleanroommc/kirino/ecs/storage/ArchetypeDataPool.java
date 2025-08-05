package com.cleanroommc.kirino.ecs.storage;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

public abstract class ArchetypeDataPool {
    public final ImmutableList<Class<? extends ICleanComponent>> components;

    protected final ComponentRegistry componentRegistry;

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     * </ul>
     *
     * @param componentRegistry The component registry
     * @param components The component types for this archetype
     */
    public ArchetypeDataPool(ComponentRegistry componentRegistry, List<Class<? extends ICleanComponent>> components) {
        this.componentRegistry = componentRegistry;
        this.components = ImmutableList.copyOf(components);
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
}

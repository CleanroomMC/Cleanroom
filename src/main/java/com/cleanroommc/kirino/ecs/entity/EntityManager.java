package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ComponentRegistry;
import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import com.cleanroommc.kirino.ecs.storage.ArchetypeDataPool;
import com.cleanroommc.kirino.ecs.storage.ArchetypeKey;
import com.cleanroommc.kirino.ecs.storage.HeapPool;

import java.util.*;
import java.util.stream.Collectors;

public class EntityManager {
    private final ComponentRegistry componentRegistry;

    public EntityManager(ComponentRegistry componentRegistry) {
        this.componentRegistry = componentRegistry;
    }

    private final List<Integer> freeIndexes = new ArrayList<>();
    private int indexCounter = 0;

    // they share the same length
    private final List<List<Class<? extends ICleanComponent>>> entityComponents = new ArrayList<>();
    private final List<ArchetypeKey> entityArchetypeLocations = new ArrayList<>();
    private final List<Integer> entityGenerations = new ArrayList<>();

    private final Map<ArchetypeKey, ArchetypeDataPool> archetypes = new HashMap<>();

    protected final List<EntityCommand> commandBuffer = new ArrayList<>();

    /**
     * Consume all buffered commands.
     * </br></br>
     * Thread safety is not guaranteed. Call it from the main thread.
     */
    public void flush() {
        for (EntityCommand command : commandBuffer) {
            switch (command.type) {
                case CREATE -> {
                    List<Class<? extends ICleanComponent>> components = entityComponents.get(command.index);
                    ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                    ArchetypeDataPool pool = archetypes.computeIfAbsent(archetypeKey, k -> new HeapPool(componentRegistry, components, 100, 50, 50));
                    pool.addEntity(command.index, command.newComponents);
                }
                case DESTROY -> {
                    ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                    ArchetypeDataPool pool = archetypes.get(archetypeKey);
                    pool.removeEntity(command.index);
                }
                case SET_COM -> {
                    ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                    ArchetypeDataPool pool = archetypes.get(archetypeKey);
                    pool.setComponent(command.index, command.componentToSet);
                }
                case ADD_COM -> {
                    List<Class<? extends ICleanComponent>> components = entityComponents.get(command.index);
                    ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                    ArchetypeDataPool oldPool = archetypes.get(archetypeKey);

                    List<ICleanComponent> newComponents = new ArrayList<>();
                    for (Class<? extends ICleanComponent> component: components) {
                        newComponents.add(oldPool.getComponent(command.index, component));
                    }
                    newComponents.add(command.componentToAdd);

                    // update component info
                    components.add(command.componentToAdd.getClass());
                    // update archetype key
                    archetypeKey = new ArchetypeKey(components);
                    entityArchetypeLocations.set(command.index, archetypeKey);

                    ArchetypeDataPool newPool = archetypes.computeIfAbsent(archetypeKey, k -> new HeapPool(componentRegistry, components, 100, 50, 50));
                    oldPool.removeEntity(command.index);
                    newPool.addEntity(command.index, newComponents);
                }
                case REMOVE_COM -> {
                    List<Class<? extends ICleanComponent>> components = entityComponents.get(command.index);
                    ArchetypeKey archetypeKey = entityArchetypeLocations.get(command.index);
                    ArchetypeDataPool oldPool = archetypes.get(archetypeKey);

                    List<ICleanComponent> newComponents = new ArrayList<>();
                    for (Class<? extends ICleanComponent> component: components) {
                        newComponents.add(oldPool.getComponent(command.index, component));
                    }
                    newComponents.removeIf(c -> c.getClass().equals(command.componentToRemove));

                    // update component info
                    components.removeIf(c -> c.equals(command.componentToRemove));
                    // update archetype key
                    archetypeKey = new ArchetypeKey(components);
                    entityArchetypeLocations.set(command.index, archetypeKey);

                    ArchetypeDataPool newPool = archetypes.computeIfAbsent(archetypeKey, k -> new HeapPool(componentRegistry, components, 100, 50, 50));
                    oldPool.removeEntity(command.index);
                    newPool.addEntity(command.index, newComponents);
                }
            }
        }
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All component types are valid and registered in the component registry</li>
     *     <li>Stop modifying <code>components</code> from now on as the action is deferred</li>
     * </ul>
     * </br>
     * This method will allocate an entity handle and generate a command for all side effects.
     * Buffered commands will be consumed at {@link #flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @see #flush()
     *
     * @param components The component types this entity has
     * @return An entity handle
     */
    public synchronized CleanEntityHandle createEntity(ICleanComponent... components) {
        int index;
        if (freeIndexes.isEmpty()) {
            index = indexCounter++;
        } else {
            index = freeIndexes.removeLast();
        }

        // update component info
        List<Class<? extends ICleanComponent>> comTypes = Arrays.stream(components).map(ICleanComponent::getClass).collect(Collectors.toList());
        if (index > entityComponents.size() - 1) {
            entityComponents.add(comTypes);
        } else {
            entityComponents.set(index, comTypes);
        }

        // update archetype key
        ArchetypeKey archetypeKey = new ArchetypeKey(comTypes);
        if (index > entityArchetypeLocations.size() - 1) {
            entityArchetypeLocations.add(archetypeKey);
        } else {
            entityArchetypeLocations.set(index, archetypeKey);
        }

        // fetch generation
        int generation = 0;
        if (index > entityGenerations.size() - 1) {
            entityGenerations.add(generation);
        } else {
            generation = entityGenerations.get(index);
        }

        EntityCommand command = new EntityCommand(index, EntityCommand.Type.CREATE);
        command.newComponents = Arrays.asList(components);
        commandBuffer.add(command);

        return new CleanEntityHandle(this, index, generation);
    }

    /**
     * This method will destroy an entity and generate a command for all side effects.
     * Buffered commands will be consumed at {@link #flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @see #flush()
     * @param index The index of the entity
     */
    protected synchronized void destroyEntity(int index) {
        if (index < 0 || index > entityGenerations.size() - 1) {
            throw new IndexOutOfBoundsException("The index provided is invalid. Current array length is " + entityGenerations.size() + ".");
        }
        // update generation
        entityGenerations.set(index, entityGenerations.get(index) + 1);
        freeIndexes.add(index);

        EntityCommand command = new EntityCommand(index, EntityCommand.Type.DESTROY);
        commandBuffer.add(command);
    }

    protected int getLatestGeneration(int index) {
        if (index < 0 || index > entityGenerations.size() - 1) {
            throw new IndexOutOfBoundsException("The index provided is invalid. Current array length is " + entityGenerations.size() + ".");
        }
        return entityGenerations.get(index);
    }

    protected List<Class<? extends ICleanComponent>> getComponentTypes(int index) {
        if (index < 0 || index > entityComponents.size() - 1) {
            throw new IndexOutOfBoundsException("The index provided is invalid. Current array length is " + entityGenerations.size() + ".");
        }
        return entityComponents.get(index);
    }
}

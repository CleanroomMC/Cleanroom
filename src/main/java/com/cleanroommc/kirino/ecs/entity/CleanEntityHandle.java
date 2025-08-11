package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;

import java.util.Objects;

public class CleanEntityHandle {
    private final EntityManager entityManager;
    public final int index;
    public final int generation;

    protected CleanEntityHandle(EntityManager entityManager, int index, int generation) {
        this.entityManager = entityManager;
        this.index = index;
        this.generation = generation;
    }

    /**
     * Thread safety is guaranteed.
     *
     * @return Whether this entity handle is valid
     */
    public boolean valid() {
        return generation == entityManager.getLatestGeneration(index);
    }

    /**
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed).
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @return Whether you successfully executed the method
     */
    public boolean destroy() {
        if (!valid()) {
            return false;
        }

        entityManager.destroyEntity(index);

        return true;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The component type is valid and registered in the component registry from its {@link EntityManager}</li>
     *     <li>Stop modifying <code>component</code> from now on as the action is deferred</li>
     * </ul>
     * <br/>
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed).
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component
     * @return Whether you successfully executed the method
     */
    public synchronized boolean trySetComponent(ICleanComponent component) {
        if (!valid()) {
            return false;
        }

        EntityCommand command = new EntityCommand(index, EntityCommand.Type.SET_COM);
        command.componentToSet = component;
        entityManager.commandBuffer.add(command);

        return true;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>The component type is valid and registered in the component registry from its {@link EntityManager}</li>
     *     <li>Stop modifying <code>component</code> from now on as the action is deferred</li>
     * </ul>
     * <br/>
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed).
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component
     * @return Whether you successfully executed the method
     */
    public synchronized boolean tryAddComponent(ICleanComponent component) {
        if (!valid()) {
            return false;
        }

        EntityCommand command = new EntityCommand(index, EntityCommand.Type.ADD_COM);
        command.componentToAdd = component;
        entityManager.commandBuffer.add(command);

        return true;
    }

    /**
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed),
     * OR the entity doesn't have this type of component.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component type
     * @return Whether you successfully executed the method
     */
    public synchronized boolean tryRemoveComponent(Class<? extends ICleanComponent> component) {
        if (!valid()) {
            return false;
        }
        if (!entityManager.getComponentTypes(index).contains(component)) {
            return false;
        }

        EntityCommand command = new EntityCommand(index, EntityCommand.Type.REMOVE_COM);
        command.componentToRemove = component;
        entityManager.commandBuffer.add(command);

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CleanEntityHandle that)) return false;
        return index == that.index && generation == that.generation && Objects.equals(entityManager, that.entityManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityManager, index, generation);
    }
}

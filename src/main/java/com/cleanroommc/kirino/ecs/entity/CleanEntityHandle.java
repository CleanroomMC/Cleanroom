package com.cleanroommc.kirino.ecs.entity;

import com.cleanroommc.kirino.ecs.component.ICleanComponent;
import org.jspecify.annotations.NonNull;

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
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * However, this entity handle becomes invalid immediately after this call.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @return Whether you successfully executed the method
     *
     * @see EntityManager#flush()
     */
    public boolean tryDestroy() {
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
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed),
     * OR the entity doesn't have this type of component.
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component
     * @return Whether you successfully executed the method
     *
     * @see EntityManager#flush()
     */
    public boolean trySetComponent(@NonNull ICleanComponent component) {
        if (!valid()) {
            return false;
        }
        if (!entityManager.getComponentTypes(index).contains(component.getClass())) {
            return false;
        }

        synchronized (entityManager.commandBuffer) {
            EntityCommand command = new EntityCommand(index, EntityCommand.Type.SET_COM);
            command.componentToSet = component;
            entityManager.commandBuffer.add(command);
        }

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
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component
     * @return Whether you successfully executed the method
     *
     * @see EntityManager#flush()
     */
    public boolean tryAddComponent(@NonNull ICleanComponent component) {
        if (!valid()) {
            return false;
        }

        synchronized (entityManager.commandBuffer) {
            EntityCommand command = new EntityCommand(index, EntityCommand.Type.ADD_COM);
            command.componentToAdd = component;
            entityManager.commandBuffer.add(command);
        }

        return true;
    }

    /**
     * This method returns <code>false</code> if the entity handle is expired (i.e. corresponding entity is destroyed),
     * OR the entity doesn't have this type of component.
     * </br>
     * The action won't execute immediately but after {@link EntityManager#flush()}.
     * </br></br>
     * Thread safety is guaranteed.
     *
     * @param component The component type
     * @return Whether you successfully executed the method
     *
     * @see EntityManager#flush()
     */
    public boolean tryRemoveComponent(@NonNull Class<? extends ICleanComponent> component) {
        if (!valid()) {
            return false;
        }
        if (!entityManager.getComponentTypes(index).contains(component)) {
            return false;
        }

        synchronized (entityManager.commandBuffer) {
            EntityCommand command = new EntityCommand(index, EntityCommand.Type.REMOVE_COM);
            command.componentToRemove = component;
            entityManager.commandBuffer.add(command);
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CleanEntityHandle that)) {
            return false;
        }

        return index == that.index && generation == that.generation && Objects.equals(entityManager, that.entityManager);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityManager, index, generation);
    }
}

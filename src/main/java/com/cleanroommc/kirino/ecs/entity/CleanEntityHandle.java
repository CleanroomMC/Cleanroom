package com.cleanroommc.kirino.ecs.entity;

public class CleanEntityHandle {
    private final EntityManager entityManager;
    public final int id;
    public final int generation;

    public CleanEntityHandle(EntityManager entityManager, int id, int generation) {
        this.entityManager = entityManager;
        this.id = id;
        this.generation = generation;
    }
}

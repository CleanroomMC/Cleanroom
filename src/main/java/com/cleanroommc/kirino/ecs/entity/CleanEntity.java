package com.cleanroommc.kirino.ecs.entity;

public class CleanEntity {
    private final EntityManager entityManager;
    public final int id;
    public final int generation;

    public CleanEntity(EntityManager entityManager, int id, int generation) {
        this.entityManager = entityManager;
        this.id = id;
        this.generation = generation;
    }
}

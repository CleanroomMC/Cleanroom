package com.cleanroommc.kirino.ecs.world;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.system.CleanSystem;

import java.util.ArrayList;
import java.util.List;

public class CleanWorld {
    private final EntityManager entityManager;
    private final List<CleanSystem> systems = new ArrayList<>();

    public void addSystem(CleanSystem system) {
        systems.add(system);
    }

    public CleanWorld(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}

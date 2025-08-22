package com.cleanroommc.kirino.ecs.world;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.ecs.system.graph.SystemExeGraph;


public class CleanWorld {
    protected final EntityManager entityManager;

    private final SystemExeGraph systemExeGraph = new SystemExeGraph();

    // need more params to indicate dep
    public final void addSystem(CleanSystem system) {
        // systemExeGraph.add
    }

    public CleanWorld(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void update() {
        systemExeGraph.execute();
        entityManager.flush();
    }
}

package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;

import java.util.concurrent.ForkJoinPool;

public class ChunkMeshletGenSystem extends CleanSystem {
    @Override
    public void update(EntityManager entityManager, JobScheduler jobScheduler) {
        jobScheduler.executeParallel(entityManager, ChunkMeshletGenJob.class, null, ForkJoinPool.commonPool());
    }
}

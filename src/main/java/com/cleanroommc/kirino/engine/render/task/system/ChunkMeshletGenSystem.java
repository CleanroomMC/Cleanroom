package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class ChunkMeshletGenSystem extends CleanSystem {
    @Override
    public void update(EntityManager entityManager, JobScheduler jobScheduler) {
        Map<String, Object> externalData = Collections.synchronizedMap(new HashMap<>());
        // TODO: Replace 256 with a variable in case we ever want to give people an option to increase the world height
        for (int y = 0; y < 256; y += 16) {
            externalData.put("startY", y);
            jobScheduler.executeParallel(entityManager, ChunkMeshletGenJob.class, externalData, ForkJoinPool.commonPool());
        }
    }
}

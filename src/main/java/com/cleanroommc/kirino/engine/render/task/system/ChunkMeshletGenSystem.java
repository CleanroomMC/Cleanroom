package com.cleanroommc.kirino.engine.render.task.system;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.system.CleanSystem;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import org.jspecify.annotations.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

public class ChunkMeshletGenSystem extends CleanSystem {
    private final ChunkProviderClient chunkClient;
    private final GizmosManager gizmosManager;

    public ChunkMeshletGenSystem(ChunkProviderClient chunkClient, GizmosManager gizmosManager) {
        this.chunkClient = chunkClient;
        this.gizmosManager = gizmosManager;
    }

    @Override
    public void update(@NonNull EntityManager entityManager, @NonNull JobScheduler jobScheduler) {
        Map<String, Object> externalData = Collections.synchronizedMap(new HashMap<>());
        externalData.put("chunkProvider", chunkClient);
        externalData.put("gizmosManager", gizmosManager);
        jobScheduler.executeParallel(entityManager, ChunkMeshletGenJob.class, externalData, ForkJoinPool.commonPool());
    }
}

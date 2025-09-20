package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.KirinoRendering;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.storage.INativeArray;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;

public class ChunkMeshletGenJob implements IParallelJob {
    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public INativeArray<Integer> chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public INativeArray<Integer> chunkPosZArray;

    @Override
    public void execute(int index) {
        int x = chunkPosXArray.get(index);
        int z = chunkPosZArray.get(index);
        KirinoRendering.LOGGER.info("debug chunk xz: " + x + ", " + z);
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}

package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.INativeArray;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import net.minecraft.client.multiplayer.ChunkProviderClient;

public class ChunkMeshletGenJob implements IParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public INativeArray<Integer> chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public INativeArray<Integer> chunkPosZArray;

    @Override
    public void execute(int index) {
        int x = chunkPosXArray.get(index);
        int z = chunkPosZArray.get(index);
        KirinoCore.LOGGER.info("debug chunk xz: " + x + ", " + z);
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}

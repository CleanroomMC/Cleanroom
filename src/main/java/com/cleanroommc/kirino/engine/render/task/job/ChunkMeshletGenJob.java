package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import org.jspecify.annotations.NonNull;

public class ChunkMeshletGenJob implements IParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobExternalDataQuery
    public GizmosManager gizmosManager;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosY"})
    public IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"isDirty"})
    public IPrimitiveArray isChunkDirty;

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }

    @Override
    public void execute(@NonNull EntityManager entityManager, int index) {
        if (!isChunkDirty.getBool(index)) {
            return;
        }

        int x = chunkPosXArray.getInt(index);
        int y = chunkPosYArray.getInt(index);
        int z = chunkPosZArray.getInt(index);

        KirinoCore.LOGGER.info("debug: " + x + ", " + y + ", " + z);
    }
}

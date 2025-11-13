package com.cleanroommc.kirino.engine.render.scene;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.job.JobScheduler;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.system.ChunkMeshletGenSystem;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;
import org.apache.commons.lang3.time.StopWatch;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MinecraftScene extends CleanWorld {
    private final GizmosManager gizmosManager;

    public MinecraftScene(EntityManager entityManager, JobScheduler jobScheduler, GizmosManager gizmosManager) {
        super(entityManager, jobScheduler);
        this.gizmosManager = gizmosManager;
        gizmosManager.addBlockSurface(0, 100, 0, 0b111111, (new Color(0.8f, 0f, 0f, 0.5f)).getRGB());
    }

    private final Map<Long, CleanEntityHandle> chunkHandles = new HashMap<>();

    private boolean rebuildChunks = false;
    private ChunkProviderClient chunkProvider = null;

    public void tryUpdateChunkProvider(ChunkProviderClient chunkProvider) {
        if (this.chunkProvider != chunkProvider) {
            rebuildChunks = true;
            this.chunkProvider = chunkProvider;
            this.chunkProvider.loadChunkCallback = (x, z) -> {
                ChunkComponent chunkComponent = new ChunkComponent();
                chunkComponent.chunkPosX = x;
                chunkComponent.chunkPosZ = z;
                chunkHandles.put(ChunkPos.asLong(x, z), entityManager.createEntity(chunkComponent));
            };
            this.chunkProvider.unloadChunkCallback = (x, z) -> {
                chunkHandles.get(ChunkPos.asLong(x, z)).tryDestroy();
                chunkHandles.remove(ChunkPos.asLong(x, z));
            };
            // no need to & must not flush immediately
        }
    }

    @Override
    public void update() {
        if (rebuildChunks) {
            rebuildChunks = false;
            for (CleanEntityHandle handle : chunkHandles.values()) {
                handle.tryDestroy();
            }
            chunkHandles.clear();
            for (Long chunkKey : chunkProvider.getLoadedChunks().keySet()) {
                ChunkComponent chunkComponent = new ChunkComponent();
                chunkComponent.chunkPosX = ChunkPos.getX(chunkKey);
                chunkComponent.chunkPosZ = ChunkPos.getZ(chunkKey);
                chunkHandles.put(chunkKey, entityManager.createEntity(chunkComponent));
            }
            // no need to & must not flush immediately
        }

        // test
        if (c == 3) {
            StopWatch stopWatch = StopWatch.createStarted();

            (new ChunkMeshletGenSystem(chunkProvider, gizmosManager)).update(entityManager, jobScheduler);

            stopWatch.stop();
            KirinoCore.LOGGER.info("executed!!! " + stopWatch.getTime(TimeUnit.MILLISECONDS) + " ms");
            entityManager.abort();
        }
        c++;

        super.update();
    }

    static int c = 0;
}

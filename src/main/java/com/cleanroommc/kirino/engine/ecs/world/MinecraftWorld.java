package com.cleanroommc.kirino.engine.ecs.world;

import com.cleanroommc.kirino.ecs.entity.CleanEntityHandle;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import com.cleanroommc.kirino.engine.ecs.component.ChunkComponent;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.math.ChunkPos;

import java.util.HashMap;
import java.util.Map;

public class MinecraftWorld extends CleanWorld {
    public MinecraftWorld(EntityManager entityManager) {
        super(entityManager);
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
            // no need to flush immediately. it's fine that archetype data is not up to date
        }
    }

    private void buildChunks() {

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
            // no need to flush immediately. it's fine that archetype data is not up to date
        }

        buildChunks();

        super.update();
    }
}

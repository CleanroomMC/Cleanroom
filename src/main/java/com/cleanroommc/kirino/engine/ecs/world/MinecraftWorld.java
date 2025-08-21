package com.cleanroommc.kirino.engine.ecs.world;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.world.CleanWorld;
import net.minecraft.client.multiplayer.ChunkProviderClient;

public class MinecraftWorld extends CleanWorld {
    public MinecraftWorld(EntityManager entityManager) {
        super(entityManager);
    }

    private boolean rebuildChunks = false;
    private ChunkProviderClient chunkProvider = null;

    public void tryUpdateChunkProvider(ChunkProviderClient chunkProvider) {
        if (this.chunkProvider != chunkProvider) {
            this.chunkProvider = chunkProvider;
            rebuildChunks = true;
        }
    }

    private void buildChunks() {

    }

    @Override
    public void update() {
        if (rebuildChunks) {
            rebuildChunks = false;
            buildChunks();
        }

        super.update();
    }
}

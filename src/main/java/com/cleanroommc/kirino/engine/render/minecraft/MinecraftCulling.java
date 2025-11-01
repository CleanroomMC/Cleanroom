package com.cleanroommc.kirino.engine.render.minecraft;

import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.world.chunk.Chunk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MinecraftCulling {
    public final Set<Entity> entitiesInView = new HashSet<>();
    public final List<TileEntity> tileEntitiesInView = new ArrayList<>();

    // todo: integrate ECS-world and jobs
    public void collectEntitiesInView(Entity renderViewEntity, ICamera camera, ChunkProviderClient chunkProvider, float partialTicks) {
        entitiesInView.clear();
        tileEntitiesInView.clear();

        List<Chunk> chunksInView = new ArrayList<>();

        chunksInView.addAll(chunkProvider.getLoadedChunks().values());

        for (Chunk chunk : chunksInView) {
            tileEntitiesInView.addAll(chunk.getTileEntityMap().values());
            for (ClassInheritanceMultiMap<Entity> entitiesInCubicChunk: chunk.getEntityLists()) {
                entitiesInView.addAll(entitiesInCubicChunk);
            }
        }
    }
}

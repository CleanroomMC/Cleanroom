package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.geometry.AABB;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.task.data.KDTree;
import com.google.common.base.Preconditions;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.world.chunk.Chunk;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class ChunkMeshletGenJob implements IParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @Override
    public void execute(int index) {
        int x = chunkPosXArray.getInt(index);
        int z = chunkPosZArray.getInt(index);
        Chunk chunk = chunkProvider.provideChunk(x, z);
        KirinoCore.LOGGER.info("debug chunk xz: {}, {}", x, z);
        // TODO: Replace 256 with a variable in case we ever want to give people an option to increase the world height
        for (int y = 0; y < 256; y += 16) {
            Optional<KDTree> treeOptional = buildTree(chunk, y);
            if (treeOptional.isEmpty()) {
                continue;
            }
            KDTree tree = treeOptional.get();
        }
    }

    private static Optional<KDTree> buildTree(@NonNull Chunk chunk, int chunkY) {
        Preconditions.checkNotNull(chunk);

        // Skip the useless shit
        if (chunk.isEmptyBetween(chunkY, chunkY+16)) {
            return Optional.empty();
        }

        final int posX = chunk.x << 4;
        final int posZ = chunk.z << 4;

        KDTree tree = new KDTree(
                new AABB(
                        posX,
                        chunkY,
                        posZ,
                        posX+16,
                        chunkY+16,
                        posZ+16
                )
        );

        for (int x = posX;x < posX+16; x++) {
            for (int y = chunkY; y < chunkY+16; y++) {
                for (int z = posZ; z < posZ+16; z++) {
                    IBlockState state = chunk.getBlockState(x, y, z);
                    if (state.getMaterial() == Material.AIR) {
                        continue;
                    }
                    int faceFlags = 0b000000;
                    faceFlags |= isOpaqueBlockPresent(chunk, posX, chunkY, posZ, x, y-1, z) ? 0b000001 : 0;
                    faceFlags |= isOpaqueBlockPresent(chunk, posX, chunkY, posZ, x, y+1, z) ? 0b000010 : 0;
                    faceFlags |= isOpaqueBlockPresent(chunk, posX, chunkY, posZ, x, y, z-1) ? 0b000100 : 0;
                    faceFlags |= isOpaqueBlockPresent(chunk, posX, chunkY, posZ, x, y, z+1) ? 0b001000 : 0;
                    faceFlags |= isOpaqueBlockPresent(chunk, posX, chunkY, posZ, x-1, y, z) ? 0b010000 : 0;
                    faceFlags |= isOpaqueBlockPresent(chunk, posX, chunkY, posZ, x+1, y, z) ? 0b100000 : 0;
                    if (faceFlags != 0) {
                        tree.insert(new Block(x, y, z, faceFlags));
                    }
                }
            }
        }

        return tree.isEmpty() ? Optional.of(tree) : Optional.empty();
    }

    private static boolean isOpaqueBlockPresent(@NonNull Chunk chunk, int cpX, int cpY, int cpZ, int x, int y, int z) {
        if (y < cpY || y >= cpY+16) {
            return true;
        }
        if (x < cpX || x >= cpX+16) {
            return true;
        }
        if (z < cpZ || z >= cpZ+16) {
            return true;
        }
        return chunk.getBlockState(x,y,z).isOpaqueCube();
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}

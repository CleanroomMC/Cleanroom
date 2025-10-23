package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        }
    }

    private static int index(int x, int y, int z) {
        return (z*256)+(y*16)+x;
    }

    private void generateMeshlets(@NonNull Chunk chunk, int chunkY, EnumFacing side) {
        Stack<Meshlet> stack = new ReferenceArrayList<>();

        int chunkX = chunk.x << 4;
        int chunkZ = chunk.z << 4;

        KDTree meshlets = buildKDTree(chunk, chunkX, chunkY, chunkZ, side);


    }

    public @NonNull KDTree buildKDTree(@NonNull Chunk chunk, int chunkX, int chunkY, int chunkZ, @NonNull EnumFacing side) {
        Preconditions.checkNotNull(chunk);
        Preconditions.checkNotNull(side);

        List<Meshlet> toAdd = new ObjectArrayList<>();

        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int y = chunkY; y < chunkY + 16; y++) {
                for (int z = chunkZ; z < chunkZ + 16; z++) {
                    if (!isOpaqueBlockPresent(chunk,
                            chunkX, chunkY, chunkZ,
                            x + side.getXOffset(), y + side.getYOffset(), z + side.getZOffset())
                            && chunk.getBlockState(x, y, z).getMaterial() != Material.AIR) {
                        toAdd.add(new Meshlet(side, x, y, z)); // the tree building function KDTree::add uses a list for maximizing the balance
                    }
                }
            }
        }

        KDTree tree = new KDTree();
        tree.add(toAdd);
        return tree;
    }

    private static boolean isOpaqueBlockPresent(@NonNull Chunk chunk, int cpX, int cpY, int cpZ, int x, int y, int z) {
        if (y < cpY || y >= cpY+16) {
            return false;
        }
        if (x < cpX || x >= cpX+16) {
            return false;
        }
        if (z < cpZ || z >= cpZ+16) {
            return false;
        }
        return chunk.getBlockState(x,y,z).isOpaqueCube();
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}

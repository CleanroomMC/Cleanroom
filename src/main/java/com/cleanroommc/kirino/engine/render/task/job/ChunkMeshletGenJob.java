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
import com.google.common.base.Preconditions;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import org.jspecify.annotations.NonNull;

import java.awt.*;

public class ChunkMeshletGenJob implements IParallelJob {
    // todo: pass opaque or transparent
    public int pass = 0; // 0: opaque, 1: transparent

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

    record ChunkCluster(
            int chunkX, int chunkY, int chunkZ,
            Chunk center,
            Chunk xPlus,
            Chunk xMinus,
            Chunk zPlus,
            Chunk zMinus) {
    }

    @Override
    public void execute(@NonNull EntityManager entityManager, int index) {
        Preconditions.checkState(pass == 0 || pass == 1,
                "Invalid pass number %d. Must be either 0 or 1.", pass);

        if (!isChunkDirty.getBool(index)) {
            return;
        }

        int chunkX = chunkPosXArray.getInt(index);
        int chunkY = chunkPosYArray.getInt(index);
        int chunkZ = chunkPosZArray.getInt(index);

        ChunkCluster chunkCluster = new ChunkCluster(
                chunkX, chunkY, chunkZ,
                chunkProvider.provideChunk(chunkX, chunkZ),
                chunkProvider.provideChunk(chunkX + 1, chunkZ),
                chunkProvider.provideChunk(chunkX - 1, chunkZ),
                chunkProvider.provideChunk(chunkX, chunkZ + 1),
                chunkProvider.provideChunk(chunkX, chunkZ - 1));

        int[][][] faceMask = new int[16][16][16];

        buildFaceMask(faceMask, chunkCluster);
    }

    /**
     * <p>Notice: a chunk is a 16x16x16 cube here, and xyz is a local coordinate inside this cube.</p>
     *
     * Returns whether the target position has a block or not depending on the current pass, opaque or transparent.
     * <br>
     * <br>
     * If at least two arguments equal to -1 or 16 (i.e. the coordinate lies exactly on the edge of the 18x18x18 cube),
     * then this method straight up returns <code>false</code>.
     * <br>
     * <br>
     * If world y-coordinate is less than 0 or greater than 255,
     * then this method straight up returns <code>false</code>.
     *
     * @param x Local x-coordinate. Domain: [-1, 16]. -1 and 16 corresponds to two neighbor chunks.
     * @param y Local y-coordinate. Domain: [-1, 16]. -1 and 16 corresponds to two neighbor chunks.
     * @param z Local z-coordinate. Domain: [-1, 16]. -1 and 16 corresponds to two neighbor chunks.
     */
    boolean blockExists(ChunkCluster chunkCluster, int x, int y, int z) {
        Preconditions.checkArgument(x >= -1 && x <= 16,
                "Argument x=%d must be between [-1, 16].", x);
        Preconditions.checkArgument(y >= -1 && y <= 16,
                "Argument y=%d must be between [-1, 16].", y);
        Preconditions.checkArgument(z >= -1 && z <= 16,
                "Argument z=%d must be between [-1, 16].", z);

        if ((x == -1 || x == 16) && (y == -1 || y == 16) && (z == -1 || z == 16)) {
            return false;
        }
        if ((x == -1 || x == 16) && (y == -1 || y == 16)) {
            return false;
        }
        if ((x == -1 || x == 16) && (z == -1 || z == 16)) {
            return false;
        }
        if ((y == -1 || y == 16) && (z == -1 || z == 16)) {
            return false;
        }
        int yWorldCoord = chunkCluster.chunkY * 16;
        if (yWorldCoord + y < 0 || yWorldCoord + y > 255) {
            return false;
        }

        IBlockState result = null;
        if (x == -1) {
            result = chunkCluster.xMinus.getBlockState(15, yWorldCoord + y, z);
        } else if (x == 16) {
            result = chunkCluster.xPlus.getBlockState(0, yWorldCoord + y, z);
        } else if (z == -1) {
            result = chunkCluster.zMinus.getBlockState(x, yWorldCoord + y, 15);
        } else if (z == 16) {
            result = chunkCluster.zPlus.getBlockState(x, yWorldCoord + y, 0);
        } else if (y == -1 || y == 16) {
            result = chunkCluster.center.getBlockState(x, yWorldCoord + y, z);
        } else {
            // exactly inside the 16x16x16 cube
            result = chunkCluster.center.getBlockState(x, yWorldCoord + y, z);
        }

        Preconditions.checkNotNull(result); // impossible to be null
        Preconditions.checkNotNull(Blocks.AIR);

        if (result == Blocks.AIR.getDefaultState()) {
            return false;
        }

        if (pass == 0) {
            return result.isOpaqueCube();
        } else if (pass == 1) {
            return result.isTranslucent();
        }

        return false; // impossible
    }

    void buildFaceMask(int[][][] faceMask, ChunkCluster chunkCluster) {
        int xWorldCoord = chunkCluster.chunkX * 16;
        int yWorldCoord = chunkCluster.chunkY * 16;
        int zWorldCoord = chunkCluster.chunkZ * 16;

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (blockExists(chunkCluster, x, y, z)) {
                        gizmosManager.addBlockSurface(x, y, z, 0b111111, new Color(0.5f, 0, 0, 0.5f).getRGB());
                    }
                }
            }
        }
    }
}

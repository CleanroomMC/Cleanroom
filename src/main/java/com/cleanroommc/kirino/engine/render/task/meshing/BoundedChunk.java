package com.cleanroommc.kirino.engine.render.task.meshing;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class BoundedChunk {
    private static final int CHUNK_SIZE = 16;

    private final Chunk[][] chunks;
    public int centerX;
    public int centerZ;

    public BoundedChunk(IChunkProvider provider, int centerChunkX, int centerChunkZ) {
        this.chunks = new Chunk[3][3];
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                chunks[cx + 1][cz + 1] = provider.provideChunk(centerChunkX + cx, centerChunkZ + cz);
            }
        }
        centerX = centerChunkX;
        centerZ = centerChunkZ;
    }

    @NonNull
    public Chunk getCenter() {
        return chunks[1][1];
    }

    public Visibility getVisibility(int chunkX, int chunkY, int chunkZ, @Nullable EnumFacing facing) {
        IBlockState state = getBlockState(chunkX, chunkY, chunkZ, facing);
        if (state.isFullCube() && state.getMaterial() != Material.AIR) {
            if (state.isOpaqueCube()) {
                return Visibility.OPAQUE;
            } else {
                return Visibility.TRANSPARENT;
            }
        }
        return Visibility.EMPTY;
    }

    public IBlockState getBlockState(int chunkX, int chunkY, int chunkZ, @Nullable EnumFacing facing) {
        if (facing == null) {
            return chunks[1][1].getBlockState(chunkX, chunkY, chunkZ);
        }
        int x = chunkX + facing.getXOffset();
        int y = chunkY + facing.getYOffset();
        int z = chunkZ + facing.getZOffset();

        int chunkOffsetX = 0;
        int chunkOffsetZ = 0;

        if (x < 0) {
            x += 16;
            chunkOffsetX = -1;
        }
        else if (x >= 16) {
            x -= 16;
            chunkOffsetX = 1;
        }

        if (z < 0) {
            z += 16;
            chunkOffsetZ = -1;
        }
        else if (z >= 16) {
            z -= 16;
            chunkOffsetZ = 1;
        }

        Chunk chunk = chunks[1 - chunkOffsetX][1 - chunkOffsetZ];
        return chunk.getBlockState(x, y, z);
    }
}

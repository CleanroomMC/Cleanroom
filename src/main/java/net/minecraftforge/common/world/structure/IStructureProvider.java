package net.minecraftforge.common.world.structure;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.Random;

public interface IStructureProvider {
    String getName() ;

    /**
     * generate after {@link IChunkGenerator#generateChunk(int, int)}
     * usually invoke {@link net.minecraft.world.gen.structure.MapGenStructure#generate(World, int, int, ChunkPrimer)} here.
     *
     * @param generator the {@link IChunkGenerator}
     * @param world the {@link World}
     * @param chunk the {@link ChunkReflection}, a special {@link ChunkPrimer}
     * @param x the x of a chunk
     * @param z the z of a chunk
     */
    void generate(IChunkGenerator generator, World world, ChunkPrimer chunk, int x, int z);

    /**
     * invoke after {@link IChunkGenerator#generateStructures(Chunk, int, int)}
     * usually invoke {@link net.minecraft.world.gen.structure.MapGenStructure#generateStructure(World, Random, ChunkPos)} here
     *
     * @param generator the {@link IChunkGenerator}
     * @param world the {@link World}
     * @param random the random
     * @param x the x of a chunk
     * @param z the z of a chunk
     */
    void generateStructure(IChunkGenerator generator, World world, Random random, int x, int z);

    /**
     * locate the structure, for /locate command.
     *
     * @param generator
     * @param worldIn
     * @param structureName
     * @param position
     * @param findUnexplored
     * @return null if not found.
     */
    @Nullable
    BlockPos getNearestStructurePos(IChunkGenerator generator,World worldIn, String structureName, BlockPos position, boolean findUnexplored);

    /**
     * @param generator
     * @param worldIn
     * @param structureName
     * @param pos
     * @return false if not
     */
    boolean isInsideStructure(IChunkGenerator generator,World worldIn, String structureName, BlockPos pos);

    class ChunkReflection extends ChunkPrimer {
        private static final IBlockState DEFAULT_STATE = Blocks.AIR.getDefaultState();
        private int x;
        private int z;
        private Chunk chunk;
        public ChunkReflection(Chunk chunk, int x, int z){
            this.chunk = chunk;
            this.x = x;
            this.z = z;
        }

        @Override
        public void setBlockState(int x, int y, int z, IBlockState state) {
            chunk.setBlockState(new BlockPos(x, y, z), state);
        }

        @Override
        public IBlockState getBlockState(int x, int y, int z) {
            return chunk.getBlockState(x, y, z);
        }

        @Override
        public int findGroundBlockIdx(int x, int z)
        {
            for (int j = 255; j >= 0; --j)
            {
                IBlockState iblockstate = getBlockState(x, j, z);

                if (iblockstate != null && iblockstate != DEFAULT_STATE)
                {
                    return j;
                }
            }

            return 0;
        }
    }
}

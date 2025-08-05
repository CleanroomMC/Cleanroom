package net.minecraftforge.common.world.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraft.world.gen.structure.MapGenStructure;

import javax.annotation.Nullable;
import java.util.Random;

public record SingleStructureProvider(MapGenStructure structure) implements IStructureProvider {

    @Override
    public String getName() {
        return structure.getStructureName();
    }

    @Override
    public void generate(IChunkGenerator generator, World world, ChunkPrimer chunk, int x, int z) {
        structure.generate(world, x, z, chunk);
    }

    @Override
    public void generateStructure(IChunkGenerator generator, World world, Random random, int x, int z) {
        structure.generateStructure(world, random, new ChunkPos(x,z));
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(IChunkGenerator generator, World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        return structure.getNearestStructurePos(worldIn, position, findUnexplored);
    }

    @Override
    public boolean isInsideStructure(IChunkGenerator generator, World worldIn, String structureName, BlockPos pos){
        return structure.isInsideStructure(pos);
    }
}

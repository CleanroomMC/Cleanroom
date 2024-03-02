package net.minecraftforge.common.world.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.IChunkGenerator;

import javax.annotation.Nullable;
import java.util.HashMap;

public class StructureAttachRegistry {
    private static final HashMap<IChunkGenerator,StructureCollection> REGISTRY = new HashMap<>();
    public static StructureCollection newStructureCollectionFor(WorldServer worldServer, IChunkGenerator chunkGenerator){
        StructureCollection collection = new StructureCollection(worldServer.getSeed(), worldServer);
        REGISTRY.put(chunkGenerator, collection);
        return collection;
    }
    public static void postGenerateChunk(IChunkGenerator generator, World world, Chunk chunk, int x, int z){
        ChunkPrimer chunkPrimer = new IStructureProvider.ChunkReflection(chunk, x, z);
        StructureCollection collection = REGISTRY.get(generator);
        if (collection != null) collection.generate(generator, world, chunkPrimer, x, z);
    }
    public static void postPopulate(IChunkGenerator generator, World world, int x, int z){
        StructureCollection collection = REGISTRY.get(generator);
        if (collection != null) collection.generateStructure(generator, world, collection.random, x, z);
    }

    @Nullable
    public static BlockPos getNearestStructurePos(IChunkGenerator generator, World worldIn, String structureName, BlockPos position, boolean findUnexplored)
    {
        StructureCollection collection = REGISTRY.get(generator);
        if (collection != null) return collection.getNearestStructurePos(generator, worldIn, structureName, position, findUnexplored);
        return null;
    }
    public static boolean isInsideStructure(IChunkGenerator generator, World worldIn, String structureName, BlockPos pos){
        StructureCollection collection = REGISTRY.get(generator);
        if (collection != null) return collection.isInsideStructure(generator, worldIn, structureName, pos);
        return false;
    }
}

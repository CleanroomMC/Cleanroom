package net.minecraftforge.common.world.structure;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.StructureAttachEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class StructureCollection implements IStructureProvider, Iterable<IStructureProvider>{
    public final HashMap<String,IStructureProvider> providers = new HashMap<>();
    public final Random random;
    public StructureCollection(long seed, WorldServer worldServer) {
        this.random = new Random(seed);
        MinecraftForge.EVENT_BUS.post(new StructureAttachEvent(this, worldServer));
    }

    public StructureCollection add(IStructureProvider provider){
        providers.put(provider.getName(), provider);
        return this;
    }
    @Override
    public String getName() {
        return "StructureCollection";
    }

    @Override
    public void generate(IChunkGenerator generator, World world, ChunkPrimer chunk, int x, int z) {
        for(IStructureProvider structureProvider: providers.values()){
            structureProvider.generate(generator, world, chunk, x, z);
        }
    }

    @Override
    public void generateStructure(IChunkGenerator generator, World world, Random random, int x, int z) {
        for(IStructureProvider structureProvider: providers.values()){
            structureProvider.generateStructure(generator, world, random, x, z);
        }
    }

    @Nullable
    @Override
    public BlockPos getNearestStructurePos(IChunkGenerator generator, World worldIn, String structureName, BlockPos position, boolean findUnexplored) {
        IStructureProvider provider = providers.get(structureName);
        if (provider != null) return provider.getNearestStructurePos(generator, worldIn, structureName, position, findUnexplored);
        else return null;
    }

    @Override
    public boolean isInsideStructure(IChunkGenerator generator, World worldIn, String structureName, BlockPos pos) {
        IStructureProvider provider = providers.get(structureName);
        if (provider != null) return provider.isInsideStructure(generator, worldIn, structureName, pos);
        else return false;
    }

    @Override
    public Iterator<IStructureProvider> iterator() {
        return providers.values().iterator();
    }
}

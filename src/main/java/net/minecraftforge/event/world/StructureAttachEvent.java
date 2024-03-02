package net.minecraftforge.event.world;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.world.structure.StructureCollection;
import net.minecraftforge.fml.common.eventhandler.Event;

public class StructureAttachEvent extends Event {
    private StructureCollection structureCollection;
    private WorldServer worldServer;
    public StructureAttachEvent(StructureCollection collection, WorldServer worldServer){
        this.structureCollection = collection;
        this.worldServer = worldServer;
    }

    public StructureCollection getStructureCollection() {
        return structureCollection;
    }

    public WorldServer getWorldServer() {
        return worldServer;
    }
}

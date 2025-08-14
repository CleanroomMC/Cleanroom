package net.minecraftforge.event.world;

import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.IChunkGenerator;
import net.minecraftforge.common.world.structure.StructureCollection;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * StructureAttachEvent is fired when a dimension is ready to be appended to the structures.<br>
 * <br>
 * This event is not {@link net.minecraftforge.fml.common.eventhandler.Cancelable;}.<br>
 * <br>
 * This event does not have a result. {@link HasResult}<br>
 * <br>
 * This event is fired on the {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}.
 **/
public class StructureAttachEvent extends Event {

    private final StructureCollection structureCollection;

    private final WorldServer worldServer;

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
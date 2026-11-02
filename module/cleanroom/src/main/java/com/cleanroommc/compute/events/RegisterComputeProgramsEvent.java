package com.cleanroommc.compute.events;

import com.cleanroommc.compute.Compute;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

public class RegisterComputeProgramsEvent extends Event {
    public void register(ResourceLocation location) {
        Compute.instance().registerProgram(location);
    }

    public void registerAll(ResourceLocation... locations) {
        for(ResourceLocation location : locations)
            Compute.instance().registerProgram(location);
    }
}

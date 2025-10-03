package com.cleanroommc.kirino.engine.render.shader.event;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class ShaderRegistrationEvent extends Event {
    private final List<ResourceLocation> shaderResourceLocations = new ArrayList<>();

    public void register(ResourceLocation resourceLocation) {
        shaderResourceLocations.add(resourceLocation);
    }
}

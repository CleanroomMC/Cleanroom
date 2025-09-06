package com.cleanroommc.kirino.engine.shader.event;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.ArrayList;
import java.util.List;

public class ShaderRegistrationEvent extends Event {
    public final List<ResourceLocation> shaderResourceLocations = new ArrayList<>();
}

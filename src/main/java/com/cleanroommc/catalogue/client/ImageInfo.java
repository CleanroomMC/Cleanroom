package com.cleanroommc.catalogue.client;

import net.minecraft.util.ResourceLocation;

/// @author MrCrayfish
public record ImageInfo(ResourceLocation resource, int width, int height, Runnable unregister) {
}

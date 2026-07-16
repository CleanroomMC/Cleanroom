package com.cleanroommc.client.modlist;

import net.minecraft.util.ResourceLocation;

public record ImageInfo(ResourceLocation resource, int width, int height, Runnable unregister) {
    public static final Runnable EMPTY_UNREGISTER = () -> {
    };

    public ImageInfo(ResourceLocation resource, int width, int height) {
        this(resource, width, height, EMPTY_UNREGISTER);
    }
}

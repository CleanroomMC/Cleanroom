/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.modlist;

import net.minecraft.util.ResourceLocation;

public record ImageInfo(ResourceLocation resource, int width, int height, Runnable unregister) {

    public static final Runnable EMPTY_UNREGISTER = () -> { };

    public ImageInfo(ResourceLocation resource, int width, int height) {
        this(resource, width, height, EMPTY_UNREGISTER);
    }

}

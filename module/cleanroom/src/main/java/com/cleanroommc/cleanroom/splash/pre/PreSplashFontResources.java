/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre;

import com.cleanroommc.kirino.ui.font.McTtfFontManager;
import com.cleanroommc.kirino.ui.simpletext.backend.freetype.FreeTypeManager;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import net.minecraft.client.resources.DefaultResourcePack;
import net.minecraft.client.resources.ResourceIndex;
import net.minecraft.client.resources.ResourceIndexFolder;

import java.io.File;

/**
 * <p>Note: Loaded only by the screen thread.</p>
 */
final class PreSplashFontResources {

    private PreSplashFontResources() {
    }

    static McTtfFontManager create(File assetsDirectory, @Nullable String assetIndex, FreeTypeManager freeType) {
        Preconditions.checkNotNull(assetsDirectory);
        Preconditions.checkNotNull(freeType);

        ResourceIndex index = assetIndex == null
                ? new ResourceIndexFolder(assetsDirectory)
                : new ResourceIndex(assetsDirectory, assetIndex);

        McTtfFontManager fonts = new McTtfFontManager(new DefaultResourcePack(index), freeType);
        fonts.loggerName = "pre_splash";
        return fonts;
    }
}

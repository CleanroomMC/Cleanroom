/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.discovery;

import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public record IdentifiedMods(List<ModContainer> mods, List<File> nonModLibs) {

    public IdentifiedMods {
        mods = new ArrayList<>(mods); // Can't be unmodifiable
        nonModLibs = List.copyOf(nonModLibs);
    }

}

package com.cleanroommc.discovery;

import net.minecraftforge.fml.common.ModContainer;

import java.io.File;
import java.util.List;

public record IdentifiedMods(List<ModContainer> mods, List<File> nonModLibs) {

    public IdentifiedMods {
        mods = List.copyOf(mods);
        nonModLibs = List.copyOf(nonModLibs);
    }

}

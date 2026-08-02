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

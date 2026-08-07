package com.cleanroommc.discovery;

import java.io.File;
import java.util.List;
import java.util.jar.Attributes;

public record DiscoveredMod(
        File file,
        Attributes attributes,
        List<String> modIds,
        String modType,
        boolean hasMixinManifestAttributes,
        boolean mixinTweakerForceMod,
        boolean coreModContainsMod,
        String coremod,
        String tweaker) {

    public DiscoveredMod {
        file = file.getAbsoluteFile();
        modIds = List.copyOf(modIds);
    }

    public boolean isDirectoryBased() {
        return file.isDirectory();
    }

}

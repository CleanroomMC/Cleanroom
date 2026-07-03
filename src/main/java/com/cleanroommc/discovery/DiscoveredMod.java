package com.cleanroommc.discovery;

import java.io.File;
import java.util.List;
import java.util.jar.Attributes;

public record DiscoveredMod(
        File file,
        Attributes manifestAttributes,
        List<String> modIds,
        String modType,
        boolean hasMixinManifestAttributes,
        boolean forceLoadAsMod,
        boolean forceReparseable,
        String coremod,
        String tweaker) {

    public DiscoveredMod {
        file = file.getAbsoluteFile();
        modIds = List.copyOf(modIds);
    }

}

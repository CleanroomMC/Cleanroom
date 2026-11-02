/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.InjectedModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

public class CleanroomContainer extends DummyModContainer {

    private static ModContainer instance;

    public CleanroomContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = CleanroomVersion.MOD_ID;
        meta.name = "Cleanroom";
        meta.description = """
                           Cleanroom is a 1.12.2 Forge fork, providing newer toolchain, new APIs and 99% compatibility.
                           Our plan is to make 1.12.2 up-to-date with latest toolchain while adding more feature to Forge & vanilla, so we don't need to care about higher versions' X point release * Y Mod API headache.
                           """;
        meta.version = CleanroomVersion.getVersion();
        meta.authorList = Arrays.asList("LexManos", "cpw", "fry", "Rongmario", "kappa_maintainer", "Li");
        meta.credits = "MrCrayFish for the Catalogue mod as base for the revamped mod list.";
        meta.url = "https://cleanroommc.com";
        meta.modProperties.put("issueTrackerUrl", "https://github.com/CleanroomMC/Cleanroom/issues");
        meta.updateJSON = "https://download.cleanroommc.com/api/forge";
        meta.logoFile = "/assets/cleanroom/banner.png";
        meta.modProperties.put("iconFile", "/assets/cleanroom/icon.png");
        meta.modProperties.put("backgroundFile", "/assets/cleanroom/background.png");
    }

    public static ModContainer instance() {
        if (instance == null) {
            instance = new InjectedModContainer(new CleanroomContainer(), FMLSanityChecker.fmlLocation);
        }
        return instance;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public URL getUpdateUrl() {
        try {
            return new URI("https://download.cleanroommc.com/api/forge").toURL();
        } catch (MalformedURLException | URISyntaxException ignored) { }
        return null;
    }

}

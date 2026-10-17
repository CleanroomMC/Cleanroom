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

import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;

@SuppressWarnings("deprecation") // Cleanroom: legacy-number uses in this file are the legacy view
public class ConfigAnytimeContainer extends DummyModContainer {

    public ConfigAnytimeContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "configanytime";
        meta.name = "ConfigAnytime";
        meta.description = "Allows Forge configurations to be setup at any point in time.";
        meta.version = ForgeEarlyConfig.CUSTOM_BUILT_IN_MOD_VERSION ? ForgeEarlyConfig.CONFIG_ANY_TIME_VERSION : "3.0";
        meta.authorList.add("Rongmario");
        meta.logoFile = "/assets/configanytime/icon.png";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public ArtifactVersion getProcessedVersion() {
        return new DefaultArtifactVersion("configanytime", true);
    }

}

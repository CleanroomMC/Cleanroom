/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch;

import com.cleanroommc.cleanroom.compatibility.CompatibilityConfig;

import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

public final class ModPatchMixinPlugin implements IMixinConfigPlugin {

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return switch (mixinClassName.split("\\.")[5]) {
            case "betterrecords" -> CompatibilityConfig.mods.betterRecords;
            case "codechickenlib" -> CompatibilityConfig.mods.codeChickenLib;
            case "creativecore" -> CompatibilityConfig.mods.creativeCore;
            case "custommainmenu" -> CompatibilityConfig.mods.customMainMenu;
            case "howlingmoon" -> CompatibilityConfig.mods.howlingMoon;
            case "lockdown" -> CompatibilityConfig.mods.lockdown;
            case "mcjtylib" -> CompatibilityConfig.mods.mcjtyLib;
            case "projectred_core" -> CompatibilityConfig.mods.projectRedCore;
            case "waterpower" -> CompatibilityConfig.mods.waterPower;
            case "xaeroplus" -> CompatibilityConfig.mods.xaeroPlus;
            default ->
                throw new UnsupportedOperationException("Mod " + mixinClassName.split("\\.")[5] + " is not supported");
        };
    }

}

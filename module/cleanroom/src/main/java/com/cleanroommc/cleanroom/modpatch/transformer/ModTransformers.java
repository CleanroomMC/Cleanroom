/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.transformer;

import com.cleanroommc.cleanroom.compatibility.CompatibilityConfig;

public final class ModTransformers {

    private ModTransformers() { }

    public static byte[] transform(String name, byte[] bytes) {
        if (CompatibilityConfig.mods.betterSprinting && name.startsWith("chylex.bettersprinting.")) {
            return BetterSprinting.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.colytra && name.startsWith("c4.colytra.")) {
            return Colytra.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.crossbow && name.startsWith("git.jbredwards.crossbow.")) {
            return Crossbow.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.ears && name.startsWith("com.unascribed.ears.")) {
            return Ears.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.enchantmentsControl && name.startsWith("austeretony.enchcontrol.")) {
            return EnchantmentControl.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.enderCore && name.startsWith("com.enderio.core.")) {
            return EnderCore.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.futureMC && name.startsWith("thedarkcolour.futuremc.")) {
            return FutureMC.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.moreSoundConfig && name.startsWith("ichttt.mods.moresoundconfig.")) {
            return MoreSoundConfig.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.sereneSeasons && name.startsWith("sereneseasons.")) {
            return SereneSeasons.transform(name, bytes);
        }
        if (CompatibilityConfig.mods.subaquatic && name.startsWith("git.jbredwards.subaquatic.")) {
            return Subaquatic.transform(name, bytes);
        }
        return bytes;
    }

}

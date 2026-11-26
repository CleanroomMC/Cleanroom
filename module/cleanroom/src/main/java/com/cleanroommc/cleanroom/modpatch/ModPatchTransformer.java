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

import com.cleanroommc.cleanroom.modpatch.transformer.ModTransformers;
import com.cleanroommc.discovery.CleanroomModDiscoverer;
import top.outlands.foundation.boot.ActualClassLoader;
import top.outlands.foundation.boot.TransformerHolder;
import top.outlands.foundation.function.ExplicitTransformerFunction;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.edit.MixinEdits;

import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

public final class ModPatchTransformer {

    private ModPatchTransformer() { }

    public static byte[] transform(String name, byte[] bytes) {
        if (bytes == null || name.startsWith("com.cleanroommc.cleanroom.")) {
            return bytes;
        }
        if (isJava9OrHigher(bytes)) {
            return bytes;
        }
        bytes = ModTransformers.transform(name, bytes);
        return bytes;
    }

    public static void install() {
        if (FMLLaunchHandler.side().isClient()) {
            MixinEdits.of("org.embeddedt.vintagefix.mixin.bugfix.tab_complete_ddos.ChatTabCompleterMixin").cancel();
        }
        TransformerHolder holder = ActualClassLoader.getTransformerHolder();
        ExplicitTransformerFunction previous = holder.runExplicitTransformersFunction;
        holder.runExplicitTransformersFunction = (name, bytes) -> transform(name, previous.apply(name, bytes));
        Mixins.addConfiguration("mixin.cleanroom.modpatch.json");
    }

    private static boolean isJava9OrHigher(byte[] bytes) {
        return (short) (((bytes[6] & 0xFF) << 8) | (bytes[7] & 0xFF)) > 52;
    }

}

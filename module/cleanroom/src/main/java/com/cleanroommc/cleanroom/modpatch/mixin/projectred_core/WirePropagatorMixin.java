/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.mixin.projectred_core;

import mrtjp.projectred.core.WirePropagator$;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.lang.reflect.Field;

@Mixin(value = mrtjp.projectred.core.WirePropagator$.class, remap = false)
public class WirePropagatorMixin {

    @Final @Shadow @Mutable public static WirePropagator$ MODULE$;

    @Inject(method = "liftedTree1$1", at = @At("HEAD"), cancellable = true)
    private void findFieldDirectly(CallbackInfoReturnable<Field> cir) {
        try {
            Field c = ObfuscationReflectionHelper.findField(BlockRedstoneWire.class, "field_150181_a");
            c.setAccessible(true);
            cir.setReturnValue(c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

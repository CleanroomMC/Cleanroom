/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.mixin.howlingmoon;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.raz.howlingmoon.WerewolfCapability$4", remap = false)
public class WerewolfCapabilityMixin {

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lcom/google/common/base/Predicates;assignableFrom(Ljava/lang/Class;)Lcom/google/common/base/Predicate;"
        )
    )
    private Predicate redirect(Class aClass) {
        return Predicates.instanceOf(aClass);
    }

}

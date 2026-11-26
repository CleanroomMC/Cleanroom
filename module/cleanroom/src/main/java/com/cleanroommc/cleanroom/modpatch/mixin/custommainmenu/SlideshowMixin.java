/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.mixin.custommainmenu;

import lumien.custommainmenu.configuration.elements.Slideshow;
import lumien.custommainmenu.lib.textures.ITexture;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Mixin(value = Slideshow.class, remap = false)
public class SlideshowMixin {

    @Shadow public ITexture[] ressources;

    @Inject(method = "shuffle", at = @At("HEAD"), cancellable = true)
    private void shuffle(CallbackInfo ci) {
        List<ITexture> list = Arrays.asList(this.ressources);
        Collections.shuffle(list);
        this.ressources = list.toArray(new ITexture[0]);
        ci.cancel();
    }

}

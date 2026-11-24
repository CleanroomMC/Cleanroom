/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.mixin.xaeroplus;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import xaeroplus.feature.highlights.ChunkHighlightSavingCache;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = ChunkHighlightSavingCache.class, remap = false)
public abstract class ChunkHighlightSavingCacheMixin {

    @Shadow
    public abstract void reset();

    @Inject(method = "onDisable", at = @At("HEAD"), cancellable = true)
    public void onDisable(CallbackInfo ci) {
        Futures.whenAllComplete(this.saveAllChunks()).call(
            () -> {
                this.reset();
                return null;
            },
            Runnable::run
        );
        ci.cancel();
    }

    @Inject(method = "handleWorldChange", at = @At("HEAD"), cancellable = true)
    public void handleWorldChange(CallbackInfo ci) {
        Futures.whenAllComplete(this.saveAllChunks()).call(
            () -> {
                this.reset();
                this.initializeWorld();
                this.loadChunksInActualDimension();
                return null;
            },
            Runnable::run
        );
        ci.cancel();
    }

    @Shadow
    protected abstract List<ListenableFuture<?>> saveAllChunks();

    @Shadow
    protected abstract void initializeWorld();

    @Shadow
    protected abstract void loadChunksInActualDimension();

}

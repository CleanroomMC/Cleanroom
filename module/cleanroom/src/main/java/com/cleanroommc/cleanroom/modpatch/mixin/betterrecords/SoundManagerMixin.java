/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.modpatch.mixin.betterrecords;

import tech.feldman.betterrecords.api.sound.Sound;
import tech.feldman.betterrecords.client.sound.SoundManager;
import tech.feldman.betterrecords.client.sound.SoundPlayer;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(value = SoundManager.class, remap = false)
public class SoundManagerMixin {

    @Unique private static final Map<Thread, AtomicBoolean> threads = new ConcurrentHashMap<>();

    @Redirect(method = "stopQueueAt", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;stop()V"))
    private void stopQueueAt(Thread thread) {
        Optional.ofNullable(threads.get(thread)).ifPresent(t -> t.set(false));
        thread.interrupt();
        threads.remove(thread);
    }

    @Inject(
        method = "queueSongsAt",
        at = @At(
            value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
        )
    )
    private void hackThread(
        BlockPos pos, int dimension, List<Sound> sounds, boolean shuffle, boolean repeat, CallbackInfo ci,
        @Local LocalRef<Thread> job) {
        AtomicBoolean interrupted = new AtomicBoolean(false);
        Thread thread = new SoundThread(interrupted, sounds, pos, dimension, repeat);
        job.set(thread);
        threads.put(thread, interrupted);
    }

    public static class SoundThread extends Thread {

        private final AtomicBoolean interrupted;
        private final List<Sound> sounds;
        private final BlockPos pos;
        private final int dim;
        private final boolean repeat;

        public SoundThread(AtomicBoolean interrupted, List<Sound> sounds, BlockPos pos, int dim, boolean repeat) {
            super("Better Records Sound Thread");
            this.interrupted = interrupted;
            this.sounds = sounds;
            this.pos = pos;
            this.dim = dim;
            this.repeat = repeat;
        }

        @Override
        public void interrupt() {
            interrupted.set(true);
        }

        @Override
        public void run() {
            while (!interrupted.get()) {
                sounds.forEach(sound -> SoundPlayer.INSTANCE.playSound(pos, dim, sound));
                if (!repeat) {
                    return;
                }
            }
        }

    }

}

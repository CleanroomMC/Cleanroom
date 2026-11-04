/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/**
 * This is not a client-side only class, but it's only intended to work for client-side bootstrapping.
 */
public final class PreSplashManager {

    public static final PreSplashManager INSTANCE = new PreSplashManager();

    private final PreSplashPhaseFSM fsm = new PreSplashPhaseFSM();
    private volatile @Nullable PreSplashPhase currentPhase = null;

    private PreSplashManager() {
    }

    /**
     * Thread-safe.
     *
     * @param phase The next observed phase
     */
    @ApiStatus.Internal
    public synchronized void notifyPhase(PreSplashPhase phase) {
        Preconditions.checkNotNull(phase);
        Preconditions.checkState(phase == fsm.getState(),
                "Expected pre splash phase: %s; Observed: %s", fsm.getState(), phase);

        fsm.next();
        currentPhase = phase;
    }

    /**
     * Thread-safe. Only intended to be read by {@link PreSplashScreenContainer}.
     * The screen starts after the first phase has been published.
     */
    @ApiStatus.Internal
    PreSplashPhase readPhase() {
        return Preconditions.checkNotNull(currentPhase);
    }
}

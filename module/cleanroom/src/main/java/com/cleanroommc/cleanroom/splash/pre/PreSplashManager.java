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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This is not a client-side only class, but it's only intended to work for client-side bootstrapping.
 */
public final class PreSplashManager {

    public static final PreSplashManager INSTANCE = new PreSplashManager();

    private final PreSplashPhaseFSM fsm = new PreSplashPhaseFSM();
    private final Queue<PreSplashState> states = new ConcurrentLinkedQueue<>();
    private volatile @Nullable PreSplashPhase currentPhase = null;

    private PreSplashManager() {
    }

    private static String title() {
        return "Cleanroom";
    }

    private static String phaseMsg(PreSplashPhase phase) {
        return switch (phase) {
            case FML_SETUP -> "Preparing Forge Mod Loader";
            case INITIAL_MOD_SCAN -> "Discovering mods";
            case CORE_MOD_LAUNCH -> "Loading core mods";
            case MINECRAFT_INIT -> "Initializing Minecraft";
            case RIGHT_BEFORE_SPLASH -> "Starting mod loading";
        };
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

        states.add(new PreSplashState(title(), phaseMsg(phase), ""));
        currentPhase = phase;
    }

    /**
     * Thread-safe. Must be called after the first {@link #notifyPhase(PreSplashPhase)}.
     *
     * @param phase The current phase that owns this detail update (acts like a filter)
     */
    @SuppressWarnings("DataFlowIssue")
    @ApiStatus.Internal
    public synchronized void notifyDetails(PreSplashPhase phase, String details) {
        Preconditions.checkNotNull(phase);
        Preconditions.checkNotNull(details);
        Preconditions.checkNotNull(currentPhase);

        if (currentPhase == phase) {
            states.add(new PreSplashState(title(), phaseMsg(currentPhase), details));
        }
    }

    /**
     * Thread-safe. Only intended to be read after the first phase has been published.
     */
    @ApiStatus.Internal
    PreSplashPhase readPhase() {
        return Preconditions.checkNotNull(currentPhase);
    }

    /**
     * Thread-safe.
     */
    @Nullable
    PreSplashState peek() {
        return states.peek();
    }

    /**
     * Thread-safe.
     */
    @Nullable
    PreSplashState poll() {
        return states.poll();
    }
}

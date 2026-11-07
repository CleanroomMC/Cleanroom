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
import org.jspecify.annotations.Nullable;

public final class PreSplashPresentation {

    private final PreSplashManager manager;
    private PreSplashState current;

    public PreSplashPresentation(PreSplashManager manager) {
        this.manager = manager;
        current = Preconditions.checkNotNull(manager.peek(),
                "The initial pre splash frame must be published.");
    }

    public boolean isFinished() {
        return manager.readPhase() == PreSplashPhase.RIGHT_BEFORE_SPLASH;
    }

    @Nullable
    public PreSplashState peek() {
        return manager.peek();
    }

    @Nullable
    public PreSplashState poll() {
        PreSplashState next = manager.poll();
        if (next != null) {
            current = next;
        }
        return next;
    }

    public PreSplashState nextFrame() {
        if (peek() != null) {
            poll();
        }
        return current;
    }
}

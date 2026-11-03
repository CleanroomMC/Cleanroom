package com.cleanroommc.cleanroom.splash.pre;

import com.google.common.base.Preconditions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * This is not a client-side only class, but it's only intended to work for client-side bootstrapping.
 */
public final class PreSplashManager {

    private PreSplashManager() {
    }

    public static final PreSplashManager INSTANCE = new PreSplashManager();

    public static final Logger LOGGER = LogManager.getLogger("Pre Splash");

    @ApiStatus.Internal
    public void notifyPhase(PreSplashPhase phase) {
        Preconditions.checkNotNull(phase);

    }
}

package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Instance listeners of every non-public visibility. Their registration must not depend on
 * {@code EventSubscriberTransformer} publicising {@code @SubscribeEvent} methods at class load.
 */
public class NonPublicInstanceListeners {
    private int privateCount = 0;
    protected int protectedCount = 0;
    int packageCount = 0;

    @SubscribeEvent
    private void onPrivate(ExampleEvent event) {
        privateCount++;
    }

    @SubscribeEvent
    protected void onProtected(ExampleEvent event) {
        protectedCount++;
    }

    @SubscribeEvent
    void onPackage(ExampleEvent event) {
        packageCount++;
    }

    public int total() {
        return privateCount + protectedCount + packageCount;
    }
}

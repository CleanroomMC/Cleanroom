package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Static listeners of non-public visibility. Their registration must not depend on
 * {@code EventSubscriberTransformer} publicising {@code @SubscribeEvent} methods at class load.
 */
public class NonPublicStaticListeners {
    private static int privateCount = 0;
    protected static int protectedCount = 0;

    @SubscribeEvent
    private static void onPrivate(ExampleEvent event) {
        privateCount++;
    }

    @SubscribeEvent
    protected static void onProtected(ExampleEvent event) {
        protectedCount++;
    }

    public static int total() {
        return privateCount + protectedCount;
    }
}

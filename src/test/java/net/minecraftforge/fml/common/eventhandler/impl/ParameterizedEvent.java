package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * An event class without a no-arg constructor, mirroring e.g.
 * {@code net.minecraftforge.client.event.TextureStitchEvent$Pre}. The removed
 * {@code EventSubscriptionTransformer} used to inject a no-arg constructor into such classes
 * purely so {@code EventBus.register} could instantiate them; now registration must fall back
 * to the {@link net.minecraftforge.fml.common.eventhandler.EventCompatProbe} cache.
 */
public class ParameterizedEvent extends Event {

    private final int seed;

    public ParameterizedEvent(int seed) {
        this.seed = seed;
    }

    public int getSeed() {
        return seed;
    }

    public static class Listener {

        public int calls = 0;

        @SubscribeEvent
        public void onParameterized(ParameterizedEvent event) {
            calls++;
        }
    }
}

package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Verifies the legacy inheritance semantics: a subclass override without the annotation still
 * inherits the listener registration of the annotated supertype declaration.
 */
public class InheritedListeners {
    public static class Base {
        public int baseCalls = 0;

        @SubscribeEvent
        public void onEvent(ExampleEvent event) {
            baseCalls++;
        }
    }

    public static class Derived extends Base {
        @Override
        public void onEvent(ExampleEvent event) {
            super.onEvent(event);
        }
    }
}

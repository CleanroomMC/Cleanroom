package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PolymorphicEvents {

    public static class ParentEvent extends Event {
    }

    public static class ChildEvent extends ParentEvent {
    }

    public static class ParentListener {

        public int calls = 0;

        @SubscribeEvent
        public void onParent(ParentEvent event) {
            calls++;
        }
    }

    public static class ChildListener {

        public int calls = 0;

        @SubscribeEvent
        public void onChild(ChildEvent event) {
            calls++;
        }
    }
}

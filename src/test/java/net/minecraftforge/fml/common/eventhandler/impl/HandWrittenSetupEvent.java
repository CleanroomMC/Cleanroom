package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The legacy full handwritten pattern: an event class implementing both {@code setup()} and
 * {@code getListenerList()} with its own lazily-initialized static list. The removed
 * {@code EventSubscriptionTransformer} skipped such classes entirely (its {@code hasSetup}
 * branch), so their behavior must be identical with and without the transformer: the base
 * constructor's {@code setup()} virtual call runs the handwritten setup, and registration and
 * posting both resolve the handwritten list.
 */
public class HandWrittenSetupEvent extends Event {

    private static ListenerList LISTENER_LIST;

    @Override
    protected void setup() {
        super.setup();
        if (LISTENER_LIST != null) {
            return;
        }
        LISTENER_LIST = new ListenerList(super.getListenerList());
    }

    @Override
    public ListenerList getListenerList() {
        return LISTENER_LIST;
    }

    public static class Listener {

        public int calls = 0;

        @SubscribeEvent
        public void on(HandWrittenSetupEvent event) {
            calls++;
        }
    }
}

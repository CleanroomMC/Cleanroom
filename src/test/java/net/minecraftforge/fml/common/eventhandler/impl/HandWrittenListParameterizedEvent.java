package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * An event class with a handwritten {@code getListenerList()} override but no no-arg
 * constructor. This is the hardest case for registration: the removed
 * {@code EventSubscriptionTransformer} injected a no-arg constructor into such classes (so
 * {@code EventBus.register} could instantiate them and reach the override via virtual
 * dispatch), and without it registration must allocate an instance without a constructor
 * call so that registration and posting resolve to the same handwritten list.
 */
public class HandWrittenListParameterizedEvent extends Event {

    private static final ListenerList MY_LIST = new ListenerList();

    private final int seed;

    public HandWrittenListParameterizedEvent(int seed) {
        this.seed = seed;
    }

    @Override
    public ListenerList getListenerList() {
        return MY_LIST;
    }

    public static class Listener {

        public int calls = 0;

        @SubscribeEvent
        public void on(HandWrittenListParameterizedEvent event) {
            calls++;
        }
    }
}

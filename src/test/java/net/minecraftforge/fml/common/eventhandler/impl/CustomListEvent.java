package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The legacy handwritten pattern: an event class managing its own listener list via a
 * {@code getListenerList()} override. Virtual dispatch must prefer this over the
 * {@link net.minecraftforge.fml.common.eventhandler.EventCompatProbe} cache. The list is
 * static, mirroring the injected {@code LISTENER_LIST} field shared by all instances.
 */
public class CustomListEvent extends Event {

    private static final ListenerList custom = new ListenerList();

    public static class Listener {

        public int calls = 0;

        @SubscribeEvent
        public void onCustom(CustomListEvent event) {
            calls++;
        }
    }

    @Override
    public ListenerList getListenerList() {
        return custom;
    }

    public static class Subclass extends CustomListEvent {
    }
}

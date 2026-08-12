package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author ZZZank
 */
public class CancelableEvents {

    @Cancelable
    public static class CancelableEvent extends Event {
    }

    public static class NonCancelableEvent extends Event {
    }

    /**
     * The legacy handwritten pattern: an explicit {@code isCancelable()} override without
     * the {@code @Cancelable} annotation. Virtual dispatch must prefer this over the
     * annotation probe in {@link net.minecraftforge.fml.common.eventhandler.EventCompatProbe}.
     */
    public static class HandWritten extends Event {

        @Override
        public boolean isCancelable() {
            return true;
        }
    }

    /**
     * {@code receiveCanceled = false} on a cancelable event class: the cancel check is kept
     * and a canceled event must be skipped.
     */
    public static class SkipCanceled {
        public int calls;

        @SubscribeEvent(receiveCanceled = false)
        public void onEvent(CancelableEvent e) {
            calls++;
        }
    }

    /**
     * {@code receiveCanceled = false} on a non-cancelable event class: the cancel check is
     * optimized away, so the listener still receives the event.
     */
    public static class NonCancelableListener {
        public int calls;

        @SubscribeEvent(receiveCanceled = false)
        public void onEvent(NonCancelableEvent e) {
            calls++;
        }
    }
}

package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

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
}

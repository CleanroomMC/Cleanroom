package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class StaticListeners {
    public static int recorded = -1;
    public static List<EventPriority> triggered = new ArrayList<>();
    public static EventPriority stage;

    @SubscribeEvent
    public static void recordId(ExampleEvent event) {
        recorded = event.id;
        setStage(EventPriority.NORMAL);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recordIdHigher(ExampleEvent event) {
        recorded = event.id;
        setStage(EventPriority.HIGHEST);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void recordIdLower(ExampleEvent event) {
        recorded = event.id;
        setStage(EventPriority.LOWEST);
    }

    public static void shouldNotRegister(ExampleEvent event) {
        recorded = -1;
        setStage(null);
    }

    private static void setStage(EventPriority stage) {
        StaticListeners.stage = stage;
        triggered.add(stage);
    }
}

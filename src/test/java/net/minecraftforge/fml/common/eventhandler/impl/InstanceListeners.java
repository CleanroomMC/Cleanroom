package net.minecraftforge.fml.common.eventhandler.impl;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZZZank
 */
public class InstanceListeners {
    public int recorded = -1;
    public List<EventPriority> triggered = new ArrayList<>();
    public EventPriority stage;

    @SubscribeEvent
    public void recordId(ExampleEvent event) {
        recorded = event.id;
        setStage(EventPriority.NORMAL);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void recordIdHigher(ExampleEvent event) {
        recorded = event.id;
        setStage(EventPriority.HIGHEST);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void recordIdLower(ExampleEvent event) {
        recorded = event.id;
        setStage(EventPriority.LOWEST);
    }

    public void shouldNotRegister(ExampleEvent event) {
        recorded = -1;
        setStage(null);
    }

    private void setStage(EventPriority stage) {
        this.stage = stage;
        this.triggered.add(stage);
    }
}

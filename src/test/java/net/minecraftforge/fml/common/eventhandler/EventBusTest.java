package net.minecraftforge.fml.common.eventhandler;

import net.minecraftforge.fml.common.eventhandler.impl.AbnormalListeners;
import net.minecraftforge.fml.common.eventhandler.impl.ExampleEvent;
import net.minecraftforge.fml.common.eventhandler.impl.InstanceListeners;
import net.minecraftforge.fml.common.eventhandler.impl.StaticListeners;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author ZZZank
 */
public class EventBusTest {

    @Test
    public void registerInstance() {
        var bus = new EventBus();

        var listeners = new InstanceListeners();
        bus.register(listeners);

        var event = new ExampleEvent();
        bus.post(event);

        Assertions.assertEquals(event.id, listeners.recorded);
        Assertions.assertEquals(
            List.of(EventPriority.HIGHEST, EventPriority.NORMAL, EventPriority.LOWEST),
            listeners.triggered
        );
    }

    @Test
    public void registerStatic() {
        var bus = new EventBus();

        bus.register(StaticListeners.class);

        var event = new ExampleEvent();
        bus.post(event);

        Assertions.assertEquals(event.id, StaticListeners.recorded);
        Assertions.assertEquals(
            List.of(EventPriority.HIGHEST, EventPriority.NORMAL, EventPriority.LOWEST),
            StaticListeners.triggered
        );
    }

    @Test
    public void registerAbnormal() {
        var bus = new EventBus();

        bus.register(AbnormalListeners.Actual.class);

        var event = new ExampleEvent();
        bus.post(event);

        Assertions.assertTrue(AbnormalListeners.nonVoid, "listener with non-void return type is valid");
        Assertions.assertTrue(AbnormalListeners.isPrivate, "listener with private access is valid");
    }
}

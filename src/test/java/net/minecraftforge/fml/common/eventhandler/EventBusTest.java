package net.minecraftforge.fml.common.eventhandler;

import net.minecraftforge.fml.common.eventhandler.impl.ExampleEvent;
import net.minecraftforge.fml.common.eventhandler.impl.ExampleListeners;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author ZZZank
 */
public class EventBusTest {
    public static final EventBus BUS = new EventBus();

    @Test
    public void registerInstance() {
        var listeners = new ExampleListeners();
        BUS.register(listeners);

        var event = new ExampleEvent();
        BUS.post(event);

        Assertions.assertEquals(event.id, listeners.recorded);
        Assertions.assertEquals(
            List.of(EventPriority.HIGHEST, EventPriority.NORMAL, EventPriority.LOWEST),
            listeners.triggered
        );
    }
}

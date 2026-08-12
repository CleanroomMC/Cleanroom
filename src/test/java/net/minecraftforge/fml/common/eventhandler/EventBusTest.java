package net.minecraftforge.fml.common.eventhandler;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.impl.AbnormalListeners;
import net.minecraftforge.fml.common.eventhandler.impl.CancelableEvents;
import net.minecraftforge.fml.common.eventhandler.impl.CustomListEvent;
import net.minecraftforge.fml.common.eventhandler.impl.ExampleEvent;
import net.minecraftforge.fml.common.eventhandler.impl.HandWrittenListParameterizedEvent;
import net.minecraftforge.fml.common.eventhandler.impl.HandWrittenSetupEvent;
import net.minecraftforge.fml.common.eventhandler.impl.HasResultEvents;
import net.minecraftforge.fml.common.eventhandler.impl.InheritedListeners;
import net.minecraftforge.fml.common.eventhandler.impl.InstanceListeners;
import net.minecraftforge.fml.common.eventhandler.impl.NonPublicInstanceListeners;
import net.minecraftforge.fml.common.eventhandler.impl.NonPublicStaticListeners;
import net.minecraftforge.fml.common.eventhandler.impl.ParameterizedEvent;
import net.minecraftforge.fml.common.eventhandler.impl.PolymorphicEvents;
import net.minecraftforge.fml.common.eventhandler.impl.StaticListeners;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
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
    }

    @Test
    public void registerIllegalParamType() throws Exception {
        var bus = new EventBus();

        var listener = new AbnormalListeners.IllegalParamType();

        var method = EventBus.class.getDeclaredMethod(
            "register",
            Class.class, Object.class, Method.class, ModContainer.class
        );
        method.setAccessible(true);
        method.invoke(
            bus,
            ExampleEvent.class,
            listener,
            AbnormalListeners.IllegalParamType.class.getMethod("onEvent", Object.class),
            Loader.instance().getMinecraftModContainer()
        );

        var event = new ExampleEvent();
        bus.post(event);

        Assertions.assertEquals(event.id, listener.captured);
    }

    @Test
    public void registerNonPublicInstance() {
        var bus = new EventBus();

        var listeners = new NonPublicInstanceListeners();
        bus.register(listeners);

        bus.post(new ExampleEvent());
        bus.post(new ExampleEvent());

        Assertions.assertEquals(6, listeners.total(), "private/protected/package-private instance listeners must all fire");
    }

    @Test
    public void registerNonPublicStatic() {
        var bus = new EventBus();

        bus.register(NonPublicStaticListeners.class);

        bus.post(new ExampleEvent());

        Assertions.assertEquals(2, NonPublicStaticListeners.total(), "private/protected static listeners must all fire");
    }

    @Test
    public void registerInheritedAnnotation() {
        var bus = new EventBus();

        var listeners = new InheritedListeners.Derived();
        bus.register(listeners);

        bus.post(new ExampleEvent());

        Assertions.assertEquals(1, listeners.baseCalls, "override without @SubscribeEvent inherits the annotated supertype declaration");
    }

    @Test
    public void cancelableEvent() {
        // the @Cancelable annotation must make setCanceled legal without any class-load injection
        var event = new CancelableEvents.CancelableEvent();
        event.setCanceled(true);
        Assertions.assertTrue(event.isCanceled());

        var bus = new EventBus();
        Assertions.assertTrue(bus.post(event), "post returns true when the event is cancelable and canceled");
    }

    @Test
    public void nonCancelableEvent() {
        var event = new CancelableEvents.NonCancelableEvent();
        Assertions.assertThrows(
            UnsupportedOperationException.class,
            () -> event.setCanceled(true),
            "setCanceled on a non-cancelable event must throw"
        );
    }

    @Test
    public void hasResultEvent() {
        Assertions.assertTrue(new HasResultEvents.Result().hasResult());
        Assertions.assertFalse(new HasResultEvents.NoResult().hasResult());
    }

    @Test
    public void handWrittenOverrideRespected() {
        // an explicit override without the annotation must win over the annotation probe
        // (virtual dispatch never reaches the Event base implementation)
        var event = new CancelableEvents.HandWritten();
        Assertions.assertTrue(event.isCancelable());
        event.setCanceled(true);
        Assertions.assertTrue(event.isCanceled());
    }

    @Test
    public void polymorphicPost() {
        var bus = new EventBus();
        var parent = new PolymorphicEvents.ParentListener();
        var child = new PolymorphicEvents.ChildListener();
        bus.register(parent);
        bus.register(child);

        // posting a subclass event fires both subclass and superclass listeners
        // (the subclass listener list is chained to the superclass list)
        bus.post(new PolymorphicEvents.ChildEvent());
        Assertions.assertEquals(1, parent.calls, "superclass listeners fire for subclass events");
        Assertions.assertEquals(1, child.calls);

        // posting a superclass event fires only superclass listeners (the chain is one-way)
        bus.post(new PolymorphicEvents.ParentEvent());
        Assertions.assertEquals(2, parent.calls);
        Assertions.assertEquals(1, child.calls, "subclass listeners must not fire for superclass events");
    }

    @Test
    public void registerHandWrittenListParameterizedEvent() {
        // handwritten getListenerList() override + no no-arg constructor: registration must
        // allocate an instance without a constructor call (constructor injection is gone) so
        // it resolves the same handwritten list that posting resolves
        var bus = new EventBus();
        var listener = new HandWrittenListParameterizedEvent.Listener();
        bus.register(listener);

        bus.post(new HandWrittenListParameterizedEvent(1));
        Assertions.assertEquals(1, listener.calls);
    }

    @Test
    public void registerHandWrittenSetupEvent() {
        // the legacy full handwritten pattern: setup() + getListenerList(). The transformer
        // skipped such classes (hasSetup branch), so the base constructor's setup() virtual
        // call must still run and registration/posting must resolve the handwritten list
        var bus = new EventBus();
        var listener = new HandWrittenSetupEvent.Listener();
        bus.register(listener);

        bus.post(new HandWrittenSetupEvent());
        Assertions.assertEquals(1, listener.calls);
    }

    @Test
    public void registerParameterizedEvent() {
        // event classes without a no-arg constructor (e.g. TextureStitchEvent$Pre) can no longer
        // rely on the transformer-injected constructor; registration must fall back to the probe
        var bus = new EventBus();
        var listener = new ParameterizedEvent.Listener();
        bus.register(listener);

        bus.post(new ParameterizedEvent(42));
        Assertions.assertEquals(1, listener.calls);
    }

    @Test
    public void handWrittenListenerListRespected() {
        var bus = new EventBus();
        var listener = new CustomListEvent.Listener();
        bus.register(listener);

        // registration and posting both go through the handwritten getListenerList() override
        bus.post(new CustomListEvent());
        Assertions.assertEquals(1, listener.calls);
    }
}

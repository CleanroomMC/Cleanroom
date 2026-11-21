package net.minecraftforge.fml.common.eventhandler;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.impl.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

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
    public void registerWithMissingPrivateSignatureType() throws Exception {
        Class<?> subscriber = loadWithoutOptionalDependency(OptionalDependencySubscriber.class);
        Assertions.assertThrows(NoClassDefFoundError.class, subscriber::getDeclaredMethods);

        var bus = new EventBus();
        bus.register(subscriber);
        bus.register(subscriber.getConstructor().newInstance());
        var event = new OptionalDependencyEvent();
        bus.post(event);
        Assertions.assertEquals(1, event.staticCalls);
        Assertions.assertEquals(1, event.instanceCalls);
    }

    @Test
    public void constructHandwrittenEventWithMissingPrivateSignatureType() throws Exception {
        var type = loadWithoutOptionalDependency(OptionalDependencyHandwrittenEvent.class).asSubclass(Event.class);
        Assertions.assertThrows(NoClassDefFoundError.class, type::getDeclaredMethods);

        var event = type.getConstructor().newInstance();
        Assertions.assertInstanceOf(OptionalDependencyListenerList.class, event.getListenerList());
        event.setCanceled(true);

        var bus = new EventBus();
        var delivered = new AtomicReference<Event>();
        var remove = bus.addManagedListener(type, null, EventPriority.NORMAL, true, null, delivered::set,
            ModEventContext.registrationOwner(), "optional dependency fixture");
        try {
            Assertions.assertTrue(bus.post(event));
            Assertions.assertSame(event, delivered.get());
        } finally {
            remove.run();
        }
    }

    @Test
    public void constructHandwrittenEventFromLaunchLoaderClassBytes() throws Exception {
        var fixture = OptionalDependencyHandwrittenEvent.class;
        String className = fixture.getName();
        String resourcePath = className.replace('.', '/') + ".class";
        String missingType = OptionalDependency.class.getName();
        var previous = Launch.classLoader;
        try (var loader = new LaunchClassLoader(fixture.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(missingType)) {
                    throw new ClassNotFoundException(name);
                }
                return name.equals(className) ? super.loadClass(name, resolve) : getParent().loadClass(name);
            }

            @Override
            public URL getResource(String name) {
                return name.equals(resourcePath) ? null : super.getResource(name);
            }
        }) {
            loader.addURL(fixture.getProtectionDomain().getCodeSource().getLocation());
            var type = loader.findClass(className).asSubclass(Event.class);
            Assertions.assertSame(loader, type.getClassLoader());
            Assertions.assertThrows(NoClassDefFoundError.class, type::getDeclaredMethods);

            var event = type.getConstructor().newInstance();
            EventBus.requireCancelable(type);
            Assertions.assertInstanceOf(OptionalDependencyListenerList.class, event.getListenerList());
        } finally {
            Launch.classLoader = previous;
        }
    }

    private static Class<?> loadWithoutOptionalDependency(Class<?> fixture) throws IOException {
        String missingType = OptionalDependency.class.getName();
        byte[] bytes;
        try (var resource = fixture.getResourceAsStream('/' + fixture.getName().replace('.', '/') + ".class")) {
            Assertions.assertNotNull(resource);
            bytes = resource.readAllBytes();
        }
        var loader = new ClassLoader(fixture.getClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.equals(missingType)) {
                    throw new ClassNotFoundException(name);
                }
                return super.loadClass(name, resolve);
            }

            Class<?> defineFixture() {
                return defineClass(fixture.getName(), bytes, 0, bytes.length);
            }
        };
        return loader.defineFixture();
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
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void rawManagedEventTypeMustExtendEvent() {
        var shapeFailure = Assertions.assertThrows(IllegalArgumentException.class, () -> EventShape.of(String.class));
        Assertions.assertTrue(shapeFailure.getMessage().contains("java.lang.String is not an Event"));
        var bus = new EventBus();
        var failure = Assertions.assertThrows(IllegalArgumentException.class,
            () -> bus.addManagedListener((Class) String.class, null, EventPriority.NORMAL, false, null, event -> {},
                ModEventContext.registrationOwner(), "example"));
        Assertions.assertTrue(failure.getMessage().contains("java.lang.String is not an Event"));
    }

    @Test
    public void registerNonPublic() {
        var bus = new EventBus();

        // static
        {
            bus.register(NonPublicListeners.class);

            var event = new ExampleEvent();
            bus.post(event);
            Assertions.assertEquals(Set.of("packaged static", "protected static"), event.sink);

            bus.unregister(NonPublicListeners.class);
        }

        // instance
        {
            var listener = new NonPublicListeners.OverrideWithNoSub();
            bus.register(listener);

            var event = new ExampleEvent();
            bus.post(event);
            Assertions.assertEquals(Set.of("packaged", "protected (subclass)"), event.sink);

            bus.unregister(listener);
        }
    }

    public static final class OptionalDependencyEvent extends Event {
        public int staticCalls;
        public int instanceCalls;
    }

    public static final class OptionalDependencySubscriber {
        @SubscribeEvent
        public static void staticListener(OptionalDependencyEvent event) {
            event.staticCalls++;
        }

        @SubscribeEvent
        public void instanceListener(OptionalDependencyEvent event) {
            event.instanceCalls++;
        }

        private void optionalHelper(OptionalDependency dependency) {}
    }

    public static final class OptionalDependencyHandwrittenEvent extends Event {
        private static OptionalDependencyListenerList listeners;

        @Override
        protected void setup() {
            if (listeners == null) {
                listeners = new OptionalDependencyListenerList(super.getListenerList());
            }
        }

        @Override
        public OptionalDependencyListenerList getListenerList() {
            return listeners;
        }

        @Override
        public boolean isCancelable() {
            return true;
        }

        private void getListenerList(OptionalDependency dependency) {}
    }

    public static final class OptionalDependencyListenerList extends ListenerList {
        public OptionalDependencyListenerList(ListenerList parent) {
            super(parent);
        }
    }

    private static final class OptionalDependency {}
}

package net.minecraftforge.fml.common.eventhandler.differential.fixtures;

import static net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacyMetadataScenarios.busId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.lang.module.ModuleFinder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.tools.ToolProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacySubscriberScenarios.CallbackEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LegacyEventBusContractTest {
    @Test
    void unsupportedAnnotationBelowAnInheritedHandwrittenGetter() {
        var exception = assertThrows(IllegalArgumentException.class, () -> {
            var event = new LegacyMetadataScenarios.UnannotatedBelowConflict();
            new EventBus().post(event);
        });
        String message = exception.getMessage();
        assertTrue(message.contains(LegacyMetadataScenarios.InheritedConflict.class.getName())
            || message.contains(LegacyMetadataScenarios.UnannotatedBelowConflict.class.getName()), message);
        assertTrue(message.contains("isCancelable") || message.contains("hasResult"), message);
    }

    @Test
    void unsupportedHandwrittenListGapEvenWithACompletePairBelowIt() {
        diagnoseGap(LegacyMetadataScenarios.ListGap.class, LegacyMetadataScenarios.ListGap::new);
        diagnoseGap(LegacyMetadataScenarios.PairAfterGap.class, LegacyMetadataScenarios.PairAfterGap::new);
    }

    private static void diagnoseGap(Class<? extends Event> type, java.util.function.Supplier<Event> construct) {
        var exception = assertThrows(IllegalArgumentException.class, () -> new EventBus().post(construct.get()));
        String message = exception.getMessage();
        assertTrue(message.contains(type.getName()) || message.contains(LegacyMetadataScenarios.ListGap.class.getName()), message);
        assertTrue(message.contains("setup") || message.contains("getListenerList"), message);
    }

    @Test
    void registrationReportsAnInvalidHandwrittenGetter() {
        var bus = new EventBus();
        var nullFailure = assertThrows(IllegalArgumentException.class, () -> bus.register(NullGetterSubscriber.class));
        assertTrue(nullFailure.getMessage().contains(LegacyDispatchScenarios.BadGetterEvent.class.getName()));
        assertTrue(nullFailure.getMessage().contains("getListenerList"));
        var throwingFailure = assertThrows(IllegalArgumentException.class, () -> bus.register(ThrowingGetterSubscriber.class));
        assertTrue(throwingFailure.getMessage().contains(ThrowingGetterEvent.class.getName()));
        assertTrue(throwingFailure.getMessage().contains("getListenerList"));
    }

    @Test
    void concurrentFirstRegistrationAndPublicListInvalidation() throws Exception {
        int count = 8;
        var barrier = new CyclicBarrier(count);
        var acknowledged = new AtomicInteger();
        var direct = new AtomicInteger();
        var auxiliary = new AtomicInteger();
        IEventListener directListener = event -> direct.incrementAndGet();
        var register = new ArrayList<Callable<EventBus>>(count);
        for (int i = 0; i < count; i++) {
            register.add(() -> {
                barrier.await(10, TimeUnit.SECONDS);
                var bus = new EventBus();
                bus.register(ConcurrentSubscriber.class);
                barrier.await(10, TimeUnit.SECONDS);
                return bus;
            });
        }
        try (var pool = Executors.newFixedThreadPool(count)) {
            var registrations = pool.invokeAll(register, 30, TimeUnit.SECONDS);
            var buses = new ArrayList<EventBus>(count);
            var ids = new HashSet<Integer>();
            for (var registration : registrations) {
                EventBus bus = registration.get();
                buses.add(bus);
                assertTrue(ids.add(busId(bus)), "Two buses allocated the same ListenerList ID");
            }
            assertEquals(0, ConcurrentEvent.constructions.get(), "Ordinary registration must avoid constructing event probes");

            var list = new ConcurrentEvent(acknowledged).getListenerList();
            var post = new ArrayList<Callable<ListenerList>>(count);
            for (EventBus bus : buses) {
                post.add(() -> {
                    barrier.await(10, TimeUnit.SECONDS);
                    int id = busId(bus);
                    var first = new ConcurrentEvent(acknowledged);
                    var current = first.getListenerList();
                    var otherBus = new EventBus();
                    int otherId = busId(otherBus);
                    var other = new AuxiliaryEvent();
                    IEventListener otherListener = event -> auxiliary.incrementAndGet();
                    other.getListenerList().register(otherId, EventPriority.NORMAL, otherListener);
                    current.register(id, EventPriority.HIGH, directListener);
                    bus.post(first);
                    otherBus.post(other);
                    ListenerList.unregisterAll(otherId, otherListener);
                    otherBus.post(new AuxiliaryEvent());
                    ListenerList.unregisterAll(id, directListener);
                    bus.unregister(ConcurrentSubscriber.class);
                    bus.post(new ConcurrentEvent(acknowledged));
                    return current;
                });
            }
            for (var posted : pool.invokeAll(post, 30, TimeUnit.SECONDS)) {
                assertSame(list, posted.get(), "The default listener list must have one canonical identity");
            }
            assertEquals(count, acknowledged.get());
            assertEquals(count, direct.get());
            assertEquals(count, auxiliary.get());
        }
    }

    @Test
    void threeForgeBusesAndACustomBusRemainIsolated() {
        var buses = List.of(MinecraftForge.EVENT_BUS, MinecraftForge.TERRAIN_GEN_BUS,
            MinecraftForge.ORE_GEN_BUS, new EventBus());
        var callbacks = List.of(new ScopeSubscriber("forge"), new ScopeSubscriber("terrain"),
            new ScopeSubscriber("ore"), new ScopeSubscriber("custom"));
        try {
            for (int i = 0; i < buses.size(); i++) {
                buses.get(i).register(callbacks.get(i));
            }
            for (int i = 0; i < buses.size(); i++) {
                var event = new ScopeEvent();
                buses.get(i).post(event);
                assertEquals(List.of(callbacks.get(i).label()), event.trace);
            }
        } finally {
            for (int i = 0; i < buses.size(); i++) {
                buses.get(i).unregister(callbacks.get(i));
            }
        }
    }

    @Test
    void callbackInChildLoaderWithParentEvent() throws Exception {
        Class<?> original = CrossLoaderSubscriber.class;
        String name = original.getName();
        var parent = original.getClassLoader();
        byte[] bytes;
        try (var stream = parent.getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (stream == null) {
                throw new IllegalStateException("Missing cross-loader subscriber fixture " + name);
            }
            bytes = stream.readAllBytes();
        }
        var child = new ClassLoader("eventbus child subscriber", parent) {
            @Override
            protected Class<?> loadClass(String requested, boolean resolve) throws ClassNotFoundException {
                if (!requested.equals(name)) {
                    return super.loadClass(requested, resolve);
                }
                synchronized (getClassLoadingLock(requested)) {
                    Class<?> type = findLoadedClass(requested);
                    if (type == null) {
                        type = defineClass(requested, bytes, 0, bytes.length);
                    }
                    if (resolve) {
                        resolveClass(type);
                    }
                    return type;
                }
            }
        };
        Class<?> callback = Class.forName(name, true, child);
        assertSame(child, callback.getClassLoader());
        assertSame(CallbackEvent.class, child.loadClass(CallbackEvent.class.getName()));
        var bus = new EventBus();
        bus.register(callback.getConstructor().newInstance());
        var trace = new ArrayList<String>();
        bus.post(new CallbackEvent(trace));
        assertEquals(List.of("cross-loader"), trace);
    }

    @Test
    void exportedPublicCallbackFromAnUnopenedNamedModule(@TempDir Path working) throws Exception {
        String moduleName = "legacy.eventbus.fixture";
        Path descriptor = working.resolve("module-info.java");
        Path source = working.resolve("ModuleSubscriber.java");
        Path classes = Files.createDirectory(working.resolve("classes"));
        Files.writeString(descriptor, "module " + moduleName + " { exports legacy.fixture; }");
        Files.writeString(source, """
            package legacy.fixture;

            import net.minecraftforge.fml.common.eventhandler.Event;
            import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

            public class ModuleSubscriber {
                @SubscribeEvent
                public void on(Event event) {
                    event.setResult(Event.Result.DENY);
                }
            }
            """);
        Path forge = Path.of(Event.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        var diagnostics = new ByteArrayOutputStream();
        assertEquals(0, ToolProvider.getSystemJavaCompiler().run(null, null, diagnostics, "-proc:none",
            "-classpath", forge.toString(), "--add-reads", moduleName + "=ALL-UNNAMED", "-d", classes.toString(),
            descriptor.toString(), source.toString()), () -> diagnostics.toString(StandardCharsets.UTF_8));
        var configuration = ModuleLayer.boot().configuration().resolve(ModuleFinder.of(classes), ModuleFinder.of(), Set.of(moduleName));
        var controller = ModuleLayer.defineModulesWithOneLoader(configuration, List.of(ModuleLayer.boot()), Event.class.getClassLoader());
        Module module = controller.layer().findModule(moduleName).orElseThrow();
        controller.addReads(module, Event.class.getModule());
        assertTrue(module.isNamed() && !module.isOpen("legacy.fixture", EventBus.class.getModule()));
        var subscriber = Class.forName("legacy.fixture.ModuleSubscriber", true, controller.layer().findLoader(moduleName));
        var bus = new EventBus();
        bus.register(subscriber.getConstructor().newInstance());
        var event = new Event();
        bus.post(event);
        assertEquals(Event.Result.DENY, event.getResult());
    }

    public static final class NullGetterSubscriber {
        @SubscribeEvent
        public static void on(LegacyDispatchScenarios.BadGetterEvent event) {
        }
    }

    public static final class ThrowingGetterSubscriber {
        @SubscribeEvent
        public static void on(ThrowingGetterEvent event) {
        }
    }

    public static final class ThrowingGetterEvent extends Event {
        @Override
        protected void setup() {
            super.setup();
        }

        @Override
        public ListenerList getListenerList() {
            throw new IllegalStateException("deliberately thrown by the handwritten getter");
        }
    }

    public static final class ConcurrentEvent extends Event {
        private static final AtomicInteger constructions = new AtomicInteger();
        private final AtomicInteger acknowledged;

        public ConcurrentEvent() {
            this(new AtomicInteger());
        }

        ConcurrentEvent(AtomicInteger acknowledged) {
            constructions.incrementAndGet();
            this.acknowledged = acknowledged;
        }
    }

    public static final class ConcurrentSubscriber {
        @SubscribeEvent
        public static void on(ConcurrentEvent event) {
            event.acknowledged.incrementAndGet();
        }
    }

    public static final class AuxiliaryEvent extends Event {
    }

    public static final class ScopeEvent extends Event {
        private final List<String> trace = new ArrayList<>();
    }

    public record ScopeSubscriber(String label) {
        @SubscribeEvent
        public void on(ScopeEvent event) {
            event.trace.add(label);
        }
    }

    public static final class CrossLoaderSubscriber {
        @SubscribeEvent
        public void on(CallbackEvent event) {
            event.record("cross-loader");
        }
    }
}

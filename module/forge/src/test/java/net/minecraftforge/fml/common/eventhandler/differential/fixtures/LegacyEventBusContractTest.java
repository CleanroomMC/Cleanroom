package net.minecraftforge.fml.common.eventhandler.differential.fixtures;

import static net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacyMetadataScenarios.busId;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.lang.invoke.MethodHandles;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.tools.ToolProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;
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
    void publicRegistrationSkipsPreparationFailuresAndManagedRegistrationRejectsTheBatch() {
        var bus = new EventBus();
        var target = new MixedSubscriber();
        bus.register(target);
        var delivered = new ScopeEvent();
        bus.post(delivered);
        assertEquals(List.of("valid"), delivered.trace);

        var managed = new EventBus();
        var lookup = MethodHandles.lookup();
        var owner = ModEventContext.registrationOwner();
        assertThrows(IllegalArgumentException.class, () -> managed.registerManaged(lookup, target, owner));
        var skipped = new ScopeEvent();
        managed.post(skipped);
        assertEquals(List.of(), skipped.trace);

        var nullFailure = assertThrows(IllegalArgumentException.class,
            () -> managed.registerManaged(lookup, NullGetterSubscriber.class, owner));
        assertTrue(nullFailure.getMessage().contains(LegacyDispatchScenarios.BadGetterEvent.class.getName()));
        assertTrue(nullFailure.getMessage().contains("getListenerList"));
        var throwingFailure = assertThrows(IllegalArgumentException.class,
            () -> managed.registerManaged(lookup, ThrowingGetterSubscriber.class, owner));
        assertTrue(throwingFailure.getMessage().contains(ThrowingGetterEvent.class.getName()));
        assertTrue(throwingFailure.getMessage().contains("getListenerList"));
    }

    @Test
    void invalidSignatureStillThrowsAfterInstallingEarlierSubscribers() {
        var bus = new EventBus();
        var parameter = assertThrows(IllegalArgumentException.class, () -> bus.register(new ValidBeforeWrongType()));
        assertTrue(parameter.getMessage().contains("is not an Event class java.lang.Object"));
        var event = new ScopeEvent();
        bus.post(event);
        assertEquals(List.of("earlier"), event.trace);

        var count = assertThrows(IllegalArgumentException.class, () -> new EventBus().register(new WrongArity()));
        assertTrue(count.getMessage().contains("requires 0 arguments"));
    }

    @Test
    void historicalReflectionShowsTheInstalledListenersAndOwners() throws Exception {
        var bus = new EventBus();
        int id = busId(bus);
        var maxID = EventBus.class.getDeclaredField("maxID");
        maxID.setAccessible(true);
        assertEquals(int.class, maxID.getType());
        assertTrue(Modifier.isStatic(maxID.getModifiers()));
        assertTrue(maxID.getInt(null) > id);
        int nextId = busId(new EventBus());
        assertTrue(nextId > id);
        assertTrue(maxID.getInt(null) > nextId);

        var subscriber = new ScopeSubscriber("reflection");
        var owner = ModEventContext.registrationOwner();
        bus.register(subscriber);
        var entries = legacyListeners(bus);
        var registered = entries.get(subscriber);
        assertEquals(ArrayList.class, registered.getClass());
        assertEquals(1, registered.size());
        var handler = registered.getFirst();
        assertSame(handler, new ScopeEvent().getListenerList().getListeners(id)[1]);

        var ownersField = EventBus.class.getDeclaredField("listenerOwners");
        ownersField.setAccessible(true);
        assertEquals(Map.class, ownersField.getType());
        var owners = (Map<?, ?>) ownersField.get(bus);
        assertSame(owner, owners.get(subscriber));
        assertNull(owners.get(new ScopeSubscriber("reflection")), "The historical owner map used weak identity keys");

        var subInfo = ASMEventHandler.class.getDeclaredField("subInfo");
        subInfo.setAccessible(true);
        assertEquals(SubscribeEvent.class, subInfo.getType());
        assertEquals(ScopeSubscriber.class.getDeclaredMethod("on", ScopeEvent.class).getDeclaredAnnotation(SubscribeEvent.class),
            subInfo.get(handler));

        bus.unregister(subscriber);
        assertFalse(entries.containsKey(subscriber));
    }

    @Test
    void unregisterRejectsCustomEntriesInTheReflectedBusRegistry() throws Exception {
        var bus = new EventBus();
        var subscriber = new ScopeSubscriber("supported");
        bus.register(subscriber);
        var registry = legacyListeners(bus);
        var entries = registry.get(subscriber);
        IEventListener custom = ignored -> {};
        entries.add(custom);
        try {
            var failure = assertThrows(UnsupportedOperationException.class, () -> bus.unregister(subscriber));
            assertTrue(failure.getMessage().contains("Custom IEventListener"));
            assertSame(entries, registry.get(subscriber));
            var event = new ScopeEvent();
            bus.post(event);
            assertEquals(List.of("supported"), event.trace);
        } finally {
            entries.remove(custom);
            bus.unregister(subscriber);
        }
    }

    @Test
    void listenerListReflectionReadsInheritedPriorityAndInvalidatesDescendants() throws Exception {
        var bus = new EventBus();
        int id = busId(bus);
        var parent = new Event().getListenerList();
        var child = new ScopeEvent().getListenerList();
        IEventListener direct = event -> {};
        IEventListener inherited = event -> {};
        child.register(id, EventPriority.HIGH, direct);
        parent.register(id, EventPriority.HIGH, inherited);

        Object parentNode = getListInstance(parent, id);
        Object childNode = getListInstance(child, id);
        Class<?> type = parentNode.getClass();
        assertTrue(Modifier.isPrivate(type.getModifiers()));
        var getPriority = type.getMethod("getListeners", EventPriority.class);
        getPriority.setAccessible(true);
        assertEquals(List.of(direct, inherited), getPriority.invoke(childNode, EventPriority.HIGH));
        assertEquals(List.of(), getPriority.invoke(childNode, EventPriority.LOW));

        var snapshot = child.getListeners(id);
        var getArray = type.getMethod("getListeners");
        getArray.setAccessible(true);
        assertSame(snapshot, getArray.invoke(childNode));
        assertTrue(Modifier.isPublic(type.getMethod("dispose").getModifiers()));
        assertTrue(Modifier.isPublic(type.getMethod("unregister", IEventListener.class).getModifiers()));
        var shouldRebuild = type.getDeclaredMethod("shouldRebuild");
        shouldRebuild.setAccessible(true);
        var forceRebuild = type.getDeclaredMethod("forceRebuild");
        forceRebuild.setAccessible(true);
        assertTrue(Modifier.isProtected(forceRebuild.getModifiers()));
        assertEquals(false, shouldRebuild.invoke(childNode));
        forceRebuild.invoke(parentNode);
        assertEquals(true, shouldRebuild.invoke(childNode));
        assertNotSame(snapshot, child.getListeners(id));
        child.unregister(id, direct);
        parent.unregister(id, inherited);
    }

    @Test
    void unregisterRemovesActualAndCopiedEntriesWithoutScanningOtherLists() throws Exception {
        var bus = new EventBus();
        var otherBus = new EventBus();
        int id = busId(bus);
        int otherId = busId(otherBus);
        var subscriber = new ScopeSubscriber("copied");
        bus.register(subscriber);
        var handler = legacyListeners(bus).get(subscriber).getFirst();
        var primary = new ScopeEvent().getListenerList();
        var boundVisits = new AtomicInteger();
        var copy = new ListenerList() {
            @Override
            public void unregister(int busID, IEventListener listener) {
                if (busID == id) {
                    boundVisits.incrementAndGet();
                }
                super.unregister(busID, listener);
            }
        };
        primary.register(otherId, EventPriority.NORMAL, handler);
        copy.register(id, EventPriority.HIGH, handler);
        Object node = getListInstance(copy, id);
        var reflectiveRegister = node.getClass().getMethod("register", EventPriority.class, IEventListener.class);
        reflectiveRegister.setAccessible(true);
        reflectiveRegister.invoke(node, EventPriority.LOW, handler);
        assertEquals(4, copy.getListeners(id).length);

        var visits = new AtomicInteger();
        new ListenerList() {
            @Override
            public void unregister(int busID, IEventListener listener) {
                if (busID == id) {
                    visits.incrementAndGet();
                }
                super.unregister(busID, listener);
            }
        };
        primary.unregister(id, handler);
        bus.unregister(subscriber);
        assertEquals(0, visits.get());
        assertEquals(1, boundVisits.get());
        assertEquals(0, primary.getListeners(id).length);
        assertEquals(0, copy.getListeners(id).length);
        var other = new ScopeEvent();
        otherBus.post(other);
        assertEquals(List.of("copied"), other.trace);
        primary.unregister(otherId, handler);
    }

    @Test
    void reflectiveRegistrationAllowsRetryAfterInstallThrowsAndRejectsNullTargets() throws Exception {
        var bus = new EventBus();
        var target = new RetrySubscriber();
        var register = EventBus.class.getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
        register.setAccessible(true);
        var owner = ModEventContext.registrationOwner();
        int id = busId(bus);
        RetryEvent.failingBuses.add(id);
        try {
            register.invoke(bus, RetryEvent.class, target, RetrySubscriber.class.getMethod("on", RetryEvent.class), owner);
            assertFalse(legacyListeners(bus).containsKey(target));
            bus.register(target);
            assertFalse(legacyListeners(bus).containsKey(target));
            var failed = new RetryEvent();
            bus.post(failed);
            assertEquals(0, failed.calls);
        } finally {
            RetryEvent.failingBuses.remove(id);
        }

        register.invoke(bus, RetryEvent.class, null, RetrySubscriber.class.getMethod("onStatic", RetryEvent.class), owner);
        register.invoke(bus, String.class, target, RetrySubscriber.class.getMethod("on", RetryEvent.class), owner);
        assertTrue(legacyListeners(bus).isEmpty());
        bus.register(target);
        var retried = new RetryEvent();
        bus.post(retried);
        assertEquals(1, retried.calls);
        bus.unregister(target);
    }

    @Test
    void managedInstallFailureRollsBackEarlierAndPartiallyInstalledSubscribers() throws Exception {
        var bus = new EventBus();
        int id = busId(bus);
        RetryEvent.failingBuses.add(id);
        try {
            assertThrows(IllegalStateException.class, () -> bus.registerManaged(MethodHandles.lookup(), new ManagedRetrySubscriber(),
                ModEventContext.registrationOwner()));
        } finally {
            RetryEvent.failingBuses.remove(id);
        }
        var scope = new ScopeEvent();
        bus.post(scope);
        assertEquals(List.of(), scope.trace);
        var retry = new RetryEvent();
        bus.post(retry);
        assertEquals(0, retry.calls);
    }

    @Test
    void explicitlyDeclaredPrivateManagedSubscriberRequiresAnAccessibleLookup() throws IllegalAccessException {
        var bus = new EventBus();
        var owner = ModEventContext.registrationOwner();
        var target = new PrivateChild();
        bus.register(target);
        var legacy = new ScopeEvent();
        bus.post(legacy);
        assertEquals(List.of("public"), legacy.trace);
        bus.unregister(target);

        assertThrows(IllegalArgumentException.class, () -> bus.registerManaged(MethodHandles.publicLookup(), target, owner));
        var unrelated = MethodHandles.privateLookupIn(EventBus.class, MethodHandles.lookup());
        assertTrue(unrelated.hasFullPrivilegeAccess());
        assertThrows(IllegalArgumentException.class, () -> bus.registerManaged(unrelated, target, owner));
        var denied = new ScopeEvent();
        bus.post(denied);
        assertEquals(List.of(), denied.trace);

        var close = bus.registerManaged(MethodHandles.lookup(), target, owner);
        try {
            var event = new ScopeEvent();
            bus.post(event);
            assertEquals(List.of("declared", "public"), event.trace);
        } finally {
            close.run();
        }
    }

    @SuppressWarnings("unchecked")
    private static ConcurrentHashMap<Object, ArrayList<IEventListener>> legacyListeners(EventBus bus) throws Exception {
        var field = EventBus.class.getDeclaredField("listeners");
        field.setAccessible(true);
        assertEquals(ConcurrentHashMap.class, field.getType());
        return (ConcurrentHashMap<Object, ArrayList<IEventListener>>) field.get(bus);
    }

    private static Object getListInstance(ListenerList list, int id) throws Exception {
        var method = ListenerList.class.getDeclaredMethod("getInstance", int.class);
        method.setAccessible(true);
        return method.invoke(list, id);
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
    void inaccessibleSubscriberFromUnopenedNamedModuleDoesNotBlockPublicCallback(@TempDir Path working) throws Exception {
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

                @SubscribeEvent
                void inaccessible(Event event) {
                    throw new AssertionError("package-private callback must be skipped");
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
        var target = subscriber.getConstructor().newInstance();
        bus.register(target);
        var event = new Event();
        bus.post(event);
        assertEquals(Event.Result.DENY, event.getResult());
        assertThrows(IllegalArgumentException.class, () -> new EventBus().registerManaged(MethodHandles.lookup(), target,
            ModEventContext.registrationOwner()));
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

    public static final class MixedSubscriber extends ValidSubscriber {
        @SubscribeEvent
        public void invalidStructure(LegacyMetadataScenarios.ListGap event) {
        }

        @SubscribeEvent
        public void invalidGetter(LegacyDispatchScenarios.BadGetterEvent event) {
        }
    }

    public static class ValidSubscriber {
        @SubscribeEvent
        public void valid(ScopeEvent event) {
            event.trace.add("valid");
        }
    }

    public static class WrongType {
        @SubscribeEvent
        public void wrong(Object event) {
        }
    }

    public static final class ValidBeforeWrongType extends WrongType {
        @SubscribeEvent
        public void valid(ScopeEvent event) {
            event.trace.add("earlier");
        }
    }

    public static final class WrongArity {
        @SubscribeEvent
        public void wrong() {
        }
    }

    public static final class RetryEvent extends Event {
        private static final Set<Integer> failingBuses = ConcurrentHashMap.newKeySet();
        private static final ListenerList LIST = new ListenerList(new Event().getListenerList()) {
            @Override
            public void register(int id, EventPriority priority, IEventListener listener) {
                super.register(id, priority, listener);
                if (failingBuses.contains(id)) {
                    throw new IllegalStateException("install fails after listener placement");
                }
            }
        };
        private int calls;

        @Override
        protected void setup() {
            super.setup();
        }

        @Override
        public ListenerList getListenerList() {
            return LIST;
        }
    }

    public static class RetrySubscriber {
        @SubscribeEvent
        public void on(RetryEvent event) {
            event.calls++;
        }

        @SubscribeEvent
        public static void onStatic(RetryEvent event) {
            throw new AssertionError("static listener must not install with a null target");
        }
    }

    public static final class ManagedRetrySubscriber extends RetrySubscriber {
        @SubscribeEvent
        public void earlier(ScopeEvent event) {
            event.trace.add("earlier");
        }
    }

    public static class PrivateParent {
        @SubscribeEvent
        private void inherited(ScopeEvent event) {
            event.trace.add("unexpected inherited private");
        }

        @SubscribeEvent
        public void visible(ScopeEvent event) {
            event.trace.add("public");
        }
    }

    public static final class PrivateChild extends PrivateParent {
        @SubscribeEvent(priority = EventPriority.HIGH)
        private void declared(ScopeEvent event) {
            event.trace.add("declared");
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

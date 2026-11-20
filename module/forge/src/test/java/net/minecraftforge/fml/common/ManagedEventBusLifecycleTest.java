package net.minecraftforge.fml.common;

import com.cleanroommc.cleanroom.event.EventChannel;
import com.cleanroommc.cleanroom.event.LegacyEventChannel;
import com.cleanroommc.cleanroom.event.Subscription;
import com.cleanroommc.cleanroom.event.SubscriptionGroup;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.FMLThrowingEventBus;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagedEventBusLifecycleTest {
    @Test
    void nestedPostsRestoreOwnerAndLogContextOnReturnAndFailure() {
        Loader loader = Loader.instance();
        ModContainer previous = ModEventContext.owner();
        String previousLog = ThreadContext.get("mod");
        ModContainer publisher = owner("publisher");
        ModContainer first = owner("first");
        ModContainer second = owner("second");
        BusGroup busGroup = BusGroup.create("managed-lifecycle-test");
        var channel = EventChannel.of(busGroup, ChannelMessage.class);
        var callbackFailure = new IllegalStateException("subscriber");
        var handlerFailure = new IllegalArgumentException("exception handler");
        var legacy = LegacyEventChannel.of(new EventBus((bus, event, listeners, index, failure) -> {
            assertSame(handlerFailure, failure);
            assertContext(publisher, "publisher log");
        }), LegacyMessage.class);
        var trace = new ArrayList<String>();
        Thread postingThread = Thread.currentThread();
        loader.setActiveModContainer(first);
        try (var firstGroup = new SubscriptionGroup();
             var secondGroup = new SubscriptionGroup(second);
             var guava = new FMLThrowingEventBus((failure, context) -> {
                 assertSame(callbackFailure, failure);
                 assertContext(first, first.getModId());
                 trace.add("guava exception handler");
                 throw handlerFailure;
             }, first)) {
            assertSame(first, firstGroup.owner());
            var subscriber = new LifecycleSubscriber(message -> {
                assertContext(first, first.getModId());
                assertSame(postingThread, Thread.currentThread());
                trace.add("guava subscriber");
                if (message.fail()) throw callbackFailure;
            });
            guava.register(subscriber);
            channel.subscribe(secondGroup, Priority.NORMAL, "channel listener", message -> {
                assertContext(second, listenerLog(second, "publisher log"));
                assertSame(postingThread, Thread.currentThread());
                trace.add("channel before Guava");
                try {
                    guava.post(new GuavaMessage(message.fail()));
                    trace.add("channel after Guava");
                } finally {
                    assertContext(second, listenerLog(second, "publisher log"));
                }
            });
            legacy.subscribe(firstGroup, "legacy listener", message -> {
                assertContext(first, listenerLog(first, "publisher log"));
                assertSame(postingThread, Thread.currentThread());
                trace.add("legacy before channel");
                if (message.fail) {
                    assertSame(handlerFailure, assertThrows(IllegalArgumentException.class,
                        () -> channel.post(new ChannelMessage(true))));
                    assertContext(first, listenerLog(first, "publisher log"));
                    trace.add("legacy after channel exception");
                    throw handlerFailure;
                }
                channel.post(new ChannelMessage(false));
                assertContext(first, listenerLog(first, "publisher log"));
                trace.add("legacy after channel");
            });

            loader.setActiveModContainer(publisher);
            ThreadContext.put("mod", "publisher log");
            assertFalse(legacy.post(new LegacyMessage(false)));
            assertContext(publisher, "publisher log");
            assertSame(handlerFailure, assertThrows(IllegalArgumentException.class,
                () -> legacy.post(new LegacyMessage(true))));
            assertContext(publisher, "publisher log");
            assertEquals(List.of("legacy before channel", "channel before Guava", "guava subscriber",
                "channel after Guava", "legacy after channel", "legacy before channel", "channel before Guava",
                "guava subscriber", "guava exception handler", "legacy after channel exception"), trace);
        } finally {
            busGroup.dispose();
            ModEventContext.restoreLogMod(previousLog);
            loader.setActiveModContainer(previous);
        }
    }

    @Test
    void contextSetterRunsBeforeEachLegacyListenerAndAnEmptyLogContextIsRestored() {
        Loader loader = Loader.instance();
        ModContainer previous = ModEventContext.owner();
        String previousLog = ThreadContext.get("mod");
        var first = owner("first");
        var second = owner("second");
        var trace = new ArrayList<String>();
        var message = new ContextMessage(trace);
        var failure = new IllegalStateException("second listener");
        var channel = LegacyEventChannel.of(new EventBus((bus, event, listeners, index, throwable) -> {
            assertNull(ModEventContext.owner());
            assertNull(ThreadContext.get("mod"));
            assertSame(failure, throwable);
        }), ContextMessage.class);
        try (var firstGroup = new SubscriptionGroup(first); var secondGroup = new SubscriptionGroup(second)) {
            loader.setActiveModContainer(null);
            ThreadContext.remove("mod");
            channel.subscribe(firstGroup, "first listener", event -> {
                assertSame(message, event);
                assertSame(first, event.appliedOwner);
                assertContext(first, listenerLog(first, null));
                trace.add("listen first");
            });
            channel.subscribe(secondGroup, "second listener", event -> {
                assertSame(message, event);
                assertSame(second, event.appliedOwner);
                assertContext(second, listenerLog(second, null));
                trace.add("listen second");
                throw failure;
            });

            assertSame(failure, assertThrows(IllegalStateException.class, () -> channel.post(message)));
            assertNull(ModEventContext.owner());
            assertNull(ThreadContext.get("mod"));
            assertEquals(List.of("set first", "listen first", "set second", "listen second"), trace);
        } finally {
            ModEventContext.restoreLogMod(previousLog);
            loader.setActiveModContainer(previous);
        }
    }

    @Test
    void defaultGroupFollowsTheOwnerAndIsRetiredWhenTheModIsDisabled() throws Exception {
        Loader loader = Loader.instance();
        LoadController controller = new LoadController(loader);
        ModContainer mod = owner("defaults");
        var modBus = new FMLThrowingEventBus((failure, context) -> {
            throw new AssertionError(failure);
        }, mod);
        Field eventChannels = LoadController.class.getDeclaredField("eventChannels");
        Field modObjectList = LoadController.class.getDeclaredField("modObjectList");
        eventChannels.setAccessible(true);
        modObjectList.setAccessible(true);
        eventChannels.set(controller, ImmutableMap.of(mod.getModId(), modBus));
        modObjectList.set(controller, HashBiMap.create());
        ModContainer previous = ModEventContext.owner();
        var legacyBus = new EventBus();
        var channel = LegacyEventChannel.of(legacyBus, LegacyMessage.class);
        var trace = new ArrayList<String>();
        try {
            loader.setActiveModContainer(mod);
            assertSame(SubscriptionGroup.defaultGroup(mod), SubscriptionGroup.current());
            channel.subscribe(message -> trace.add("lifetime"));
            legacyBus.post(new LegacyMessage(false));
            assertEquals(List.of("lifetime"), trace);

            assertThrows(UnsupportedOperationException.class, () -> SubscriptionGroup.current().close());
            legacyBus.post(new LegacyMessage(false));
            assertEquals(List.of("lifetime", "lifetime"), trace);

            controller.disableMod(mod);
            legacyBus.post(new LegacyMessage(false));
            assertEquals(List.of("lifetime", "lifetime"), trace);

            loader.setActiveModContainer(mod);
            channel.subscribe(message -> trace.add("survives as a new group"));
        } finally {
            loader.setActiveModContainer(previous);
        }
        legacyBus.post(new LegacyMessage(false));
        assertEquals(List.of("lifetime", "lifetime", "survives as a new group"), trace);
        SubscriptionGroup.closeDefaultGroup(mod);
    }

    @Test
    @SuppressWarnings("unchecked")
    void discoveredSubscribersJoinTheGroupAndKeepRegisterSemantics() throws Exception {
        var bus = new EventBus();
        ModContainer mod = owner("discovered");
        DiscoveredSubscribers.calls = 0;
        var lookup = MethodHandles.privateLookupIn(DiscoveredSubscribers.class, MethodHandles.lookup());
        try (var group = new SubscriptionGroup(mod)) {
            group.track("discovered", bus.registerManagedLenient(lookup, DiscoveredSubscribers.class, mod));
            // Duplicate call after already registered is a no-op and does not double-fire or clobber entries.
            group.track("duplicate", bus.registerManagedLenient(lookup, DiscoveredSubscribers.class, mod));
            Field ownersField = EventBus.class.getDeclaredField("listenerOwners");
            ownersField.setAccessible(true);
            assertSame(mod, ((Map<Object, ModContainer>) ownersField.get(bus)).get(DiscoveredSubscribers.class));

            bus.post(new LegacyMessage(false));
            // Private subscribers stay skipped, exactly as with register(Object).
            assertEquals(1, DiscoveredSubscribers.calls);

            bus.register(DiscoveredSubscribers.class);
            bus.post(new LegacyMessage(false));
            assertEquals(2, DiscoveredSubscribers.calls);
        }
        bus.post(new LegacyMessage(false));
        assertEquals(2, DiscoveredSubscribers.calls);
        assertFalse(bus.hasManagedListeners(new LegacyMessage(false)));
    }

    @Test
    void unifiedGroupRegisterCoversBothGenerationsWithForge7AnnotationAndRollsBackAtomically() {
        var mod = owner("unified");
        var forge7Channel = EventChannel.record(UnifiedForge7Event.class);
        var legacyChannel = LegacyEventChannel.of(UnifiedLegacyCancelable.class);
        var handler = new UnifiedHandler();
        var lookup = MethodHandles.lookup();
        try (var group = new SubscriptionGroup(mod)) {
            List<Subscription> installed = group.register(lookup, handler);
            assertEquals(2, installed.size());
            forge7Channel.post(new UnifiedForge7Event("ok"));
            assertTrue(legacyChannel.post(new UnifiedLegacyCancelable()));
            assertEquals(List.of("forge7:ok", "legacy-f7-annotation"), handler.trace);

            assertThrows(IllegalArgumentException.class, () -> group.register(lookup, new InvalidUnifiedHandler()));
        }
        assertFalse(forge7Channel.hasListeners());
        assertFalse(legacyChannel.hasListeners());
    }

    @Test
    void discoveredForge7SubscribersRetireWhenOwningModIsDisabled() throws Exception {
        Loader loader = Loader.instance();
        LoadController controller = new LoadController(loader);
        ModContainer mod = owner("discovered-f7");
        var modBus = new FMLThrowingEventBus((failure, context) -> {
            throw new AssertionError(failure);
        }, mod);
        Field eventChannels = LoadController.class.getDeclaredField("eventChannels");
        Field modObjectList = LoadController.class.getDeclaredField("modObjectList");
        eventChannels.setAccessible(true);
        modObjectList.setAccessible(true);
        eventChannels.set(controller, ImmutableMap.of(mod.getModId(), modBus));
        modObjectList.set(controller, HashBiMap.create());

        DiscoveredForge7Subscribers.trace.clear();
        var forge7Channel = EventChannel.record(UnifiedForge7Event.class);
        var legacyChannel = LegacyEventChannel.of(UnifiedLegacyCancelable.class);
        var lookup = MethodHandles.privateLookupIn(DiscoveredForge7Subscribers.class, MethodHandles.lookup());
        try {
            Runnable legacyRemoval = MinecraftForge.EVENT_BUS.registerManagedLenient(
                lookup, DiscoveredForge7Subscribers.class, mod
            );
            SubscriptionGroup.defaultGroup(mod).track(DiscoveredForge7Subscribers.class.getName(), legacyRemoval);
            SubscriptionGroup.registerDiscovered(mod, lookup, DiscoveredForge7Subscribers.class);

            assertTrue(forge7Channel.hasListeners());
            assertTrue(legacyChannel.hasListeners());
            forge7Channel.post(new UnifiedForge7Event("boot"));
            assertTrue(legacyChannel.post(new UnifiedLegacyCancelable()));
            assertEquals(List.of("forge7:boot", "legacy-static"), DiscoveredForge7Subscribers.trace);

            controller.disableMod(mod);
            assertFalse(forge7Channel.hasListeners());
            assertFalse(legacyChannel.hasListeners());
        } finally {
            SubscriptionGroup.closeDefaultGroup(mod);
            MinecraftForge.EVENT_BUS.unregister(DiscoveredForge7Subscribers.class);
        }
    }

    @Test
    void rootLegacyChannelDispatchesTheOriginalEventAndAllowsUncancelling() {
        var bus = new EventBus();
        var root = LegacyEventChannel.of(bus, Event.class);
        var child = LegacyEventChannel.of(bus, CancellableMessage.class);
        var message = new CancellableMessage();
        var trace = new ArrayList<String>();
        try (var subscriptions = new SubscriptionGroup(owner("canceller"))) {
            root.subscribe(subscriptions, Priority.HIGH, event -> {
                assertSame(message, event);
                event.setCanceled(true);
                trace.add("cancel");
            });
            child.subscribe(subscriptions, "skip while cancelled", event -> trace.add("skipped"));
            child.subscribe(subscriptions, Priority.LOW, true, event -> {
                assertSame(message, event);
                assertTrue(event.isCanceled());
                event.setCanceled(false);
                trace.add("uncancel");
            });
            root.subscribe(subscriptions, Priority.LOWEST, "after uncancel", event -> trace.add("continue"));

            assertFalse(root.post(message));
            assertFalse(message.isCanceled());
            assertEquals(List.of("cancel", "uncancel", "continue"), trace);
        }
        assertFalse(child.post(message));
        assertEquals(List.of("cancel", "uncancel", "continue"), trace);
    }

    @Test
    void legacyAnnotationBatchOwnsItsExactRegistrations() {
        var bus = new EventBus();
        var trace = new ArrayList<String>();
        var target = new LegacySubscriber(trace);
        try (var subscriptions = new SubscriptionGroup(owner("annotated legacy"))) {
            bus.register(target);
            try (var managed = subscriptions.registerLegacy(bus, MethodHandles.lookup(), target)) {
                bus.post(new LegacyMessage(false));
                assertEquals(List.of("callback", "callback"), trace);
            }
            bus.post(new LegacyMessage(false));
            assertEquals(List.of("callback", "callback", "callback"), trace);
        } finally {
            bus.unregister(target);
        }
    }

    @Test
    void explicitTableRegistersAnAbstractHandwrittenEvent() {
        var bus = new EventBus();
        var event = new DescriptorEvent();
        var trace = new ArrayList<DescriptorParent>();
        var unresolved = LegacyEventChannel.of(bus, DescriptorParent.class);
        var channel = unresolved.withListenerList(DescriptorParent.LIST);
        assertTrue(unresolved.hasListeners());
        assertFalse(unresolved.hasListeners(event));
        assertFalse(channel.hasListeners());
        try (var subscriptions = new SubscriptionGroup(owner("descriptor"))) {
            channel.subscribe(subscriptions, "hand-written parent", trace::add);
            assertTrue(channel.hasListeners());
            assertTrue(unresolved.hasListeners(event));
            channel.post(event);
            assertEquals(List.of(event), trace);
        }
        assertFalse(channel.hasListeners());
        assertFalse(unresolved.hasListeners(event));
        bus.post(new DescriptorEvent());
        assertEquals(List.of(event), trace);
    }

    public static final class DiscoveredSubscribers {
        static int calls;

        @SubscribeEvent
        public static void visible(LegacyMessage event) {
            calls++;
        }

        @SubscribeEvent
        private static void hidden(LegacyMessage event) {
            calls += 100;
        }
    }

    public static final class LegacySubscriber {
        private final List<String> trace;

        LegacySubscriber(List<String> trace) {
            this.trace = trace;
        }

        @SubscribeEvent
        public void receive(LegacyMessage event) {
            trace.add("callback");
        }
    }

    public abstract static class DescriptorParent extends Event {
        private static ListenerList LIST;

        @Override
        protected void setup() {
            super.setup();
            if (LIST == null) {
                LIST = new ListenerList(super.getListenerList());
            }
        }

        @Override
        public ListenerList getListenerList() {
            return LIST;
        }
    }

    public static final class DescriptorEvent extends DescriptorParent {
        private static ListenerList list;

        @Override
        protected void setup() {
            super.setup();
            if (list == null) {
                list = new ListenerList(super.getListenerList());
            }
        }

        @Override
        public ListenerList getListenerList() {
            return list;
        }
    }

    static ModContainer owner(String id) {
        var metadata = new ModMetadata();
        metadata.modId = id;
        metadata.name = "Mod " + id;
        return new DummyModContainer(metadata);
    }

    private static String listenerLog(ModContainer owner, String previous) {
        return Boolean.getBoolean("fml.LogContext") ? owner.getName() : previous;
    }

    private static void assertContext(ModContainer expected, String log) {
        assertSame(expected, ModEventContext.owner());
        assertSame(expected, Loader.instance().activeModContainer());
        assertEquals(log, ThreadContext.get("mod"));
    }

    public record ChannelMessage(boolean fail) implements RecordEvent {}

    public record GuavaMessage(boolean fail) {}

    public record LifecycleSubscriber(Consumer<GuavaMessage> callback) {
        @Subscribe
        public void receive(GuavaMessage event) {
            callback.accept(event);
        }
    }

    public static final class LegacyMessage extends Event {
        private final boolean fail;

        LegacyMessage(boolean fail) {
            this.fail = fail;
        }
    }

    public static final class ContextMessage extends Event implements IContextSetter {
        private final List<String> trace;
        private ModContainer appliedOwner;

        ContextMessage(List<String> trace) {
            this.trace = trace;
        }

        @Override
        public void setModContainer(ModContainer owner) {
            assertSame(owner, ModEventContext.owner());
            assertSame(owner, Loader.instance().activeModContainer());
            appliedOwner = owner;
            trace.add("set " + owner.getModId());
        }
    }

    @Cancelable
    public static final class CancellableMessage extends Event {}

    public record UnifiedForge7Event(String value) implements RecordEvent {}

    @Cancelable
    public static final class UnifiedLegacyCancelable extends Event {}

    private static final class UnifiedHandler {
        private final List<String> trace = new ArrayList<>();

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private void onForge7(UnifiedForge7Event event) {
            trace.add("forge7:" + event.value());
        }

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private boolean onLegacy(UnifiedLegacyCancelable event) {
            trace.add("legacy-f7-annotation");
            return true;
        }
    }

    private static final class InvalidUnifiedHandler {
        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private void ok(UnifiedForge7Event event) {}

        @SubscribeEvent
        private void bad(UnifiedLegacyCancelable event, boolean extra) {}
    }

    public static final class DiscoveredForge7Subscribers {
        private static final List<String> trace = new ArrayList<>();

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private static void onForge7(UnifiedForge7Event event) {
            trace.add("forge7:" + event.value());
        }

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent(alwaysCancelling = true)
        private static void onLegacy(UnifiedLegacyCancelable event) {
            trace.add("legacy-static");
        }
    }
}

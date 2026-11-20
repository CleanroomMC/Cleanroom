package net.minecraftforge.fml.common;

import com.cleanroommc.cleanroom.event.LegacyEventChannel;
import com.cleanroommc.cleanroom.event.NativeEventChannel;
import com.cleanroommc.cleanroom.event.SubscriptionGroup;
import com.google.common.eventbus.Subscribe;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.FMLThrowingEventBus;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.ThreadContext;
import org.junit.jupiter.api.Test;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
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
        var nativeChannel = NativeEventChannel.record(busGroup, NativeMessage.class);
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
             var guava = FMLThrowingEventBus.forLifecycle((failure, context) -> {
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
            nativeChannel.subscribe(secondGroup, Priority.NORMAL, "native listener", message -> {
                assertContext(second, listenerLog(second, "publisher log"));
                assertSame(postingThread, Thread.currentThread());
                trace.add("native before Guava");
                try {
                    guava.post(new GuavaMessage(message.fail()));
                    trace.add("native after Guava");
                } finally {
                    assertContext(second, listenerLog(second, "publisher log"));
                }
            });
            legacy.subscribe(firstGroup, "legacy listener", message -> {
                assertContext(first, listenerLog(first, "publisher log"));
                assertSame(postingThread, Thread.currentThread());
                trace.add("legacy before native");
                if (message.fail) {
                    assertSame(handlerFailure, assertThrows(IllegalArgumentException.class,
                        () -> nativeChannel.post(new NativeMessage(true))));
                    assertContext(first, listenerLog(first, "publisher log"));
                    trace.add("legacy after native exception");
                    throw handlerFailure;
                }
                nativeChannel.post(new NativeMessage(false));
                assertContext(first, listenerLog(first, "publisher log"));
                trace.add("legacy after native");
            });

            loader.setActiveModContainer(publisher);
            ThreadContext.put("mod", "publisher log");
            assertFalse(legacy.post(new LegacyMessage(false)));
            assertContext(publisher, "publisher log");
            assertSame(handlerFailure, assertThrows(IllegalArgumentException.class,
                () -> legacy.post(new LegacyMessage(true))));
            assertContext(publisher, "publisher log");
            assertEquals(List.of("legacy before native", "native before Guava", "guava subscriber",
                "native after Guava", "legacy after native", "legacy before native", "native before Guava",
                "guava subscriber", "guava exception handler", "legacy after native exception"), trace);
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
    void rootLegacyChannelDispatchesTheOriginalEventAndAllowsUncancelling() {
        var bus = new EventBus();
        var root = LegacyEventChannel.of(bus, Event.class);
        var child = LegacyEventChannel.of(bus, CancellableMessage.class);
        var message = new CancellableMessage();
        var trace = new ArrayList<String>();
        try (var subscriptions = new SubscriptionGroup(owner("canceller"))) {
            root.subscribe(subscriptions, EventPriority.HIGH, false, "cancel", event -> {
                assertSame(message, event);
                event.setCanceled(true);
                trace.add("cancel");
            });
            child.subscribe(subscriptions, "skip while cancelled", event -> trace.add("skipped"));
            child.subscribe(subscriptions, EventPriority.LOW, true, "uncancel", event -> {
                assertSame(message, event);
                assertTrue(event.isCanceled());
                event.setCanceled(false);
                trace.add("uncancel");
            });
            root.subscribe(subscriptions, EventPriority.LOWEST, false, "after uncancel", event -> trace.add("continue"));

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
        var channel = LegacyEventChannel.of(bus, DescriptorParent.class).withListenerList(DescriptorParent.LIST);
        try (var subscriptions = new SubscriptionGroup(owner("descriptor"))) {
            channel.subscribe(subscriptions, "hand-written parent", trace::add);
            channel.post(event);
            assertEquals(List.of(event), trace);
        }
        bus.post(new DescriptorEvent());
        assertEquals(List.of(event), trace);
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

    public record NativeMessage(boolean fail) implements RecordEvent {}

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
}

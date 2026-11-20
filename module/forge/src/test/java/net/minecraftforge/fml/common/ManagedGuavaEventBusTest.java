package net.minecraftforge.fml.common;

import com.google.common.eventbus.AllowConcurrentEvents;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.eventbus.SubscriberExceptionContext;
import net.minecraftforge.fml.common.eventhandler.FMLThrowingEventBus;
import org.junit.jupiter.api.Test;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ManagedGuavaEventBusTest {
    @Test
    void reentrantPostsInheritedSubscribersExceptionsAndDeadEventsMatchGuava() throws Exception {
        var reference = new EventBusFixture();
        List<String> expected = dispatch(new EventBus(reference), reference);
        var fixture = new EventBusFixture();
        try (var managed = FMLThrowingEventBus.forLifecycle(fixture, ManagedEventBusLifecycleTest.owner("guava"))) {
            assertEquals(expected, dispatch(managed, fixture));
        }
        assertEquals(List.of("child outer", "child resumed", "parent outer", "child inner", "parent inner",
            "throwing subscriber", "exception handler", "child after failure", "parent after failure", "dead event"), expected);
    }

    @Test
    void concurrentPostsMatchGuavaSubscriberSerialization() throws Exception {
        try (var managed = FMLThrowingEventBus.forLifecycle((failure, context) -> fail(failure), ManagedEventBusLifecycleTest.owner("guava"))) {
            assertEquals(Concurrency.SERIAL, observeConcurrency(new EventBus(), false));
            assertEquals(Concurrency.SERIAL, observeConcurrency(managed, false));
            assertEquals(Concurrency.CONCURRENT, observeConcurrency(new EventBus(), true));
            assertEquals(Concurrency.CONCURRENT, observeConcurrency(managed, true));
        }
    }

    @Test
    void closingManagedBusRevokesRegisteredSubscribers() {
        var fixture = new EventBusFixture();
        var subscriber = new TraceSubscriber(fixture);
        try (var managed = FMLThrowingEventBus.forLifecycle(fixture, ManagedEventBusLifecycleTest.owner("guava"))) {
            fixture.bus = managed;
            managed.register(subscriber);
            managed.post(new ChildMessage("before close"));
            managed.close();
            managed.post(new ChildMessage("after close"));
        }
        assertEquals(List.of("child before close", "parent before close"), fixture.trace);
    }

    @Test
    void reentrantPostsHoldTheirSubscriberSnapshotAcrossMutations() {
        List<String> expected = snapshotTrace(new EventBus());
        try (var managed = FMLThrowingEventBus.forLifecycle((failure, context) -> fail(failure), null)) {
            assertEquals(expected, snapshotTrace(managed));
        }
        assertEquals(List.of("poster resumed", "original queued", "replacement nested", "replacement later"), expected);
    }

    @Test
    void anErrorAbandonsQueuedPostsAndTheNextPostCanRun() {
        List<String> expected = abortedTrace(new EventBus());
        try (var managed = FMLThrowingEventBus.forLifecycle((failure, context) -> fail(failure), null)) {
            assertEquals(expected, abortedTrace(managed));
        }
        assertEquals(List.of("after failure"), expected);
    }

    private static List<String> snapshotTrace(EventBus bus) {
        var trace = new ArrayList<String>();
        Object original = new Object() {
            @Subscribe
            void receive(String message) {
                trace.add("original " + message);
            }
        };
        Object replacement = new Object() {
            @Subscribe
            void receive(String message) {
                trace.add("replacement " + message);
            }
        };
        bus.register(original);
        bus.register(new Object() {
            @Subscribe
            void receive(Post event) {
                bus.post("queued");
                bus.unregister(original);
                bus.register(replacement);
                bus.post("nested");
                trace.add("poster resumed");
            }
        });
        bus.post(Post.FIRST);
        bus.post("later");
        return trace;
    }

    private static List<String> abortedTrace(EventBus bus) {
        var trace = new ArrayList<String>();
        var failure = new AssertionError("stop outer delivery");
        Object aborting = new Object() {
            @Subscribe
            void receive(Post event) {
                bus.post("abandoned");
                throw failure;
            }
        };
        bus.register(aborting);
        bus.register(new Object() {
            @Subscribe
            void receive(String message) {
                trace.add(message);
            }
        });
        assertSame(failure, assertThrows(AssertionError.class, () -> bus.post(Post.FIRST)));
        bus.unregister(aborting);
        bus.post("after failure");
        return trace;
    }

    private static List<String> dispatch(EventBus bus, EventBusFixture fixture) throws NoSuchMethodException {
        fixture.bus = bus;
        var subscriber = new TraceSubscriber(fixture);
        bus.register(subscriber);
        try {
            bus.post(new ChildMessage("outer"));
            bus.post(fixture.failedMessage);
            bus.post(new ChildMessage("after failure"));
            bus.post(fixture.unhandled);
            assertSame(fixture.failure, fixture.observedFailure);
            assertSame(bus, fixture.exceptionContext.getEventBus());
            assertSame(fixture.failedMessage, fixture.exceptionContext.getEvent());
            assertSame(subscriber, fixture.exceptionContext.getSubscriber());
            assertEquals(TraceSubscriber.class.getMethod("receiveFailure", FailedMessage.class),
                fixture.exceptionContext.getSubscriberMethod());
            return List.copyOf(fixture.trace);
        } finally {
            bus.unregister(subscriber);
        }
    }

    private static Concurrency observeConcurrency(EventBus bus, boolean allowConcurrent) throws Exception {
        var probe = new BlockingProbe();
        Object subscriber = allowConcurrent ? new ConcurrentSubscriber(probe) : new SerialSubscriber(probe);
        bus.register(subscriber);
        try (var executor = Executors.newFixedThreadPool(2, Thread.ofPlatform().daemon().factory())) {
            var first = executor.submit(() -> bus.post(Post.FIRST));
            try {
                assertTrue(probe.firstEntered.await(5, TimeUnit.SECONDS));
                var secondThread = new AtomicReference<Thread>();
                var secondPosting = new CountDownLatch(1);
                var second = executor.submit(() -> {
                    secondThread.set(Thread.currentThread());
                    secondPosting.countDown();
                    bus.post(Post.SECOND);
                });
                Concurrency result;
                try {
                    assertTrue(secondPosting.await(5, TimeUnit.SECONDS));
                    result = probe.observe(secondThread.get());
                } finally {
                    probe.releaseFirst.countDown();
                }
                first.get(5, TimeUnit.SECONDS);
                second.get(5, TimeUnit.SECONDS);
                assertEquals(0, probe.secondEntered.getCount());
                return result;
            } finally {
                probe.releaseFirst.countDown();
            }
        } finally {
            bus.unregister(subscriber);
        }
    }

    private static class EventBusFixture implements com.google.common.eventbus.SubscriberExceptionHandler {
        final List<String> trace = new ArrayList<>();
        final Object unhandled = new Object();
        final FailedMessage failedMessage = new FailedMessage();
        final RuntimeException failure = new IllegalStateException("subscriber failure");
        EventBus bus;
        Throwable observedFailure;
        SubscriberExceptionContext exceptionContext;

        @Override
        public void handleException(Throwable exception, SubscriberExceptionContext context) {
            observedFailure = exception;
            exceptionContext = context;
            trace.add("exception handler");
        }
    }

    public static class ParentMessage {
        final String name;

        ParentMessage(String name) {
            this.name = name;
        }
    }

    public static final class ChildMessage extends ParentMessage {
        ChildMessage(String name) {
            super(name);
        }
    }

    public static final class FailedMessage {}

    public static class ParentSubscriber {
        final EventBusFixture fixture;

        ParentSubscriber(EventBusFixture fixture) {
            this.fixture = fixture;
        }

        @Subscribe
        public void receiveParent(ParentMessage event) {
            fixture.trace.add("parent " + event.name);
        }
    }

    public static final class TraceSubscriber extends ParentSubscriber {
        TraceSubscriber(EventBusFixture fixture) {
            super(fixture);
        }

        @Subscribe
        public void receiveChild(ChildMessage event) {
            fixture.trace.add("child " + event.name);
            if (event.name.equals("outer")) {
                fixture.bus.post(new ChildMessage("inner"));
                fixture.trace.add("child resumed");
            }
        }

        @Subscribe
        public void receiveFailure(FailedMessage event) {
            fixture.trace.add("throwing subscriber");
            throw fixture.failure;
        }

        @Subscribe
        public void receiveDead(DeadEvent event) {
            assertSame(fixture.bus, event.getSource());
            assertSame(fixture.unhandled, event.getEvent());
            fixture.trace.add("dead event");
        }
    }

    public enum Post { FIRST, SECOND }

    private enum Concurrency { SERIAL, CONCURRENT }

    private static final class BlockingProbe {
        final CountDownLatch firstEntered = new CountDownLatch(1);
        final CountDownLatch releaseFirst = new CountDownLatch(1);
        final CountDownLatch secondEntered = new CountDownLatch(1);
        private long firstThreadId;

        void receive(Post event) throws InterruptedException {
            if (event == Post.FIRST) {
                firstThreadId = Thread.currentThread().threadId();
                firstEntered.countDown();
                releaseFirst.await();
            } else {
                secondEntered.countDown();
            }
        }

        Concurrency observe(Thread secondThread) throws InterruptedException {
            var threads = ManagementFactory.getThreadMXBean();
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (System.nanoTime() < deadline) {
                if (secondEntered.await(1, TimeUnit.MILLISECONDS)) return Concurrency.CONCURRENT;
                var second = threads.getThreadInfo(secondThread.threadId());
                if (second != null && second.getThreadState() == Thread.State.BLOCKED
                    && second.getLockOwnerId() == firstThreadId) {
                    return Concurrency.SERIAL;
                }
            }
            return fail("The second post neither entered the subscriber nor waited on the first subscriber invocation");
        }
    }

    public record SerialSubscriber(BlockingProbe probe) {
        @Subscribe
        public void receive(Post event) throws InterruptedException {
            probe.receive(event);
        }
    }

    public record ConcurrentSubscriber(BlockingProbe probe) {
        @Subscribe
        @AllowConcurrentEvents
        public void receive(Post event) throws InterruptedException {
            probe.receive(event);
        }
    }
}

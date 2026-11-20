package net.minecraftforge.fml.common.eventhandler.differential.fixtures;

import static net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacyMetadataScenarios.busId;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.IGenericEvent;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class LegacyDispatchScenarios {
    private LegacyDispatchScenarios() {
    }

    public static List<String> priorityAndInheritance() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        int id = busId(bus);
        var parent = new ParentEvent();
        var child = new ChildEvent();
        Thread postingThread = Thread.currentThread();
        parent.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("parent:" + event.getPhase()));
        child.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("child1:" + event.getPhase()));
        child.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("child2:" + event.getPhase()));
        parent.getListenerList().register(id, EventPriority.HIGH, event -> trace.add("high:" + event.getPhase()));
        child.getListenerList().register(id, EventPriority.LOW, event -> trace.add("same thread="
            + (postingThread == Thread.currentThread())));
        bus.post(child);
        return trace;
    }

    public static List<String> dynamicCancellation() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new CancellationSubscribers());
        var event = new DynamicEvent(trace);
        trace.add("post=" + bus.post(event));
        return trace;
    }

    public static List<String> overriddenCanceledGetter() {
        var bus = new EventBus();
        bus.register(new CanceledGetterSubscriber());
        var event = new CanceledGetterEvent();
        boolean canceled = bus.post(event);
        event.trace.add("post=" + canceled);
        return event.trace;
    }

    public static List<String> genericIdentityAndShortCircuit() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new GenericSubscribers());
        var event = new DynamicGenericEvent<String>();
        event.trace = trace;
        event.selector = String.class;
        trace.add("matched=" + bus.post(event));
        event = new DynamicGenericEvent<>();
        event.trace = trace;
        event.selector = Integer.class;
        trace.add("mismatched=" + bus.post(event));
        event = new DynamicGenericEvent<>();
        event.trace = trace;
        event.setCanceled(true);
        trace.add("canceled=" + bus.post(event) + "/" + event.getPhase());

        var nestedBus = new EventBus();
        nestedBus.register(new NestedGenericSubscriber());
        var nested = new DynamicGenericEvent<List<String>>();
        nested.trace = trace;
        Type donor = NestedGenericSubscriber.class.getMethod("donor", List.class).getGenericParameterTypes()[0];
        Type filter = ((ParameterizedType) NestedGenericSubscriber.class.getMethod("on", DynamicGenericEvent.class)
            .getGenericParameterTypes()[0]).getActualTypeArguments()[0];
        trace.add("equal distinct selector=" + (filter.equals(donor) && filter != donor));
        nested.selector = donor;
        trace.add("equal selector post=" + nestedBus.post(nested));
        return trace;
    }

    public static List<String> emptyAndShutdown() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        var event = new DynamicEvent(trace);
        event.setCanceled(true);
        trace.clear();
        trace.add("empty=" + bus.post(event));
        event.setResult(Event.Result.ALLOW);
        trace.add("result without annotation=" + event.getResult());
        bus.shutdown();
        trace.add("shutdown=" + bus.post(event));
        return trace;
    }

    public static List<String> publicListInvalidation() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        var otherBus = new EventBus();
        int id = busId(bus);
        var parent = new ParentEvent().getListenerList();
        var child = new ChildEvent().getListenerList();
        IEventListener listener = event -> trace.add("shared:" + event.getPhase());
        bus.post(new ChildEvent());
        parent.register(id, EventPriority.NORMAL, listener);
        child.register(id, EventPriority.HIGH, listener);
        trace.add("registered");
        bus.post(new ChildEvent());
        trace.add("other bus");
        otherBus.post(new ChildEvent());
        parent.unregister(id, listener);
        trace.add("direct unregister");
        bus.post(new ChildEvent());
        ListenerList.unregisterAll(id, listener);
        trace.add("global unregister");
        bus.post(new ChildEvent());
        return trace;
    }

    public static List<String> nestedRegistration() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        int id = busId(bus);
        var outer = new ChildEvent();
        var list = outer.getListenerList();
        var old = new SnapshotSubscriber("old", trace);
        var added = new SnapshotSubscriber("new", trace);
        bus.register(old);
        list.register(id, EventPriority.HIGHEST, event -> {
            if (event != outer) {
                trace.add("nested");
                return;
            }
            trace.add("outer");
            bus.unregister(old);
            bus.register(added);
            bus.post(new ChildEvent());
            trace.add("resume");
        });
        bus.post(outer);
        return trace;
    }

    public static List<String> mutableArray() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus((source, event, listeners, index, cause) -> {
            var target = (ArrayEvent) event;
            trace.add("handler index=" + index + " cause=" + cause.getClass().getSimpleName());
            trace.add("same bus=" + (source == target.source) + " same array=" + (listeners == target.array));
            target.failure = cause;
        });
        int id = busId(bus);
        var first = new ArrayEvent(bus, false);
        var list = first.getListenerList();
        list.register(id, EventPriority.HIGH, event -> {
            var target = (ArrayEvent) event;
            trace.add("first:" + target.getPhase());
            // First exposure happens in the callback, while the bus executes this array.
            target.array = target.getListenerList().getListeners(id);
            target.array[2] = target.nullSlot ? null : replacement -> trace.add("replacement:" + replacement.getPhase());
        });
        list.register(id, EventPriority.HIGH, event -> trace.add("original"));
        list.register(id, EventPriority.LOW, event -> trace.add("last:" + event.getPhase()));
        bus.post(first);
        var second = new ArrayEvent(bus, true);
        try {
            bus.post(second);
        } catch (NullPointerException thrown) {
            trace.add("same thrown=" + (second.failure == thrown));
        }
        return trace;
    }

    public static List<String> dispatchExceptions() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var failure = new IOException("listener");
        var phaseFailure = new IllegalStateException("phase");
        var bus = new EventBus((source, event, listeners, index, cause) -> {
            var target = (ThrowingEvent) event;
            trace.add("index=" + index + " cause=" + cause.getClass().getSimpleName());
            trace.add("identities=" + (source == target.bus) + "/" + (listeners == target.array)
                + "/" + (cause == (target.throwDuringPhase ? phaseFailure : failure)));
        });
        int id = busId(bus);
        var first = new ThrowingEvent(bus, phaseFailure, true);
        var list = first.getListenerList();
        list.register(id, EventPriority.NORMAL, event -> LegacyDispatchScenarios.<RuntimeException>raise(failure));
        list.register(id, EventPriority.LOW, event -> trace.add("unreachable"));
        first.array = list.getListeners(id);
        try {
            bus.post(first);
        } catch (IllegalStateException thrown) {
            trace.add("phase thrown by identity=" + (thrown == phaseFailure));
        }
        var second = new ThrowingEvent(bus, phaseFailure, false);
        second.array = first.array;
        try {
            bus.post(second);
        } catch (RuntimeException thrown) {
            trace.add("wrapped checked=" + (thrown.getClass() == RuntimeException.class && thrown.getCause() == failure));
        }
        return trace;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void raise(Throwable throwable) throws T {
        throw (T) throwable;
    }

    public static List<String> getterExceptions() {
        var trace = new ArrayList<String>();
        var bus = new EventBus((source, event, listeners, index, cause) -> trace.add("unexpected handler"));
        var failure = new IllegalStateException("getter");
        try {
            bus.post(new BadGetterEvent(failure));
        } catch (IllegalStateException thrown) {
            trace.add("getter thrown by identity=" + (thrown == failure));
        }
        try {
            bus.post(new BadGetterEvent());
        } catch (NullPointerException thrown) {
            trace.add("null getter=" + thrown.getClass().getSimpleName());
        }
        return trace;
    }

    public static List<String> equalRegistrationKeys() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        var first = new EqualSubscriber("first", trace);
        var equal = new EqualSubscriber("equal", trace);
        trace.add("distinct equal=" + (first != equal && first.equals(equal)));
        bus.register(first);
        bus.register(equal);
        bus.post(new ChildEvent());
        bus.unregister(equal);
        trace.add("unregistered equal");
        bus.post(new ChildEvent());
        return trace;
    }

    public static class ParentEvent extends Event {
    }

    public static final class ChildEvent extends ParentEvent {
    }

    public static final class DynamicEvent extends Event {
        final List<String> trace;
        boolean cancelable = true;

        public DynamicEvent() {
            this(new ArrayList<>());
        }

        public DynamicEvent(List<String> trace) {
            this.trace = trace;
        }

        @Override
        public boolean isCancelable() {
            trace.add("cancelable=" + cancelable);
            return cancelable;
        }

        @Override
        public boolean isCanceled() {
            boolean canceled = super.isCanceled();
            trace.add("canceled=" + canceled);
            return canceled;
        }

        @Override
        public void setPhase(EventPriority value) {
            super.setPhase(value);
            trace.add("phase=" + value);
        }
    }

    public static final class CancellationSubscribers {
        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void cancel(DynamicEvent event) {
            event.trace.add("cancel");
            event.setCanceled(true);
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void skip(Event event) {
            ((DynamicEvent) event).trace.add("unexpected skipped callback");
        }

        @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
        public void resume(DynamicEvent event) {
            event.trace.add("resume");
            event.setCanceled(false);
            event.cancelable = false;
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public void continueWithBaseType(Event event) {
            ((DynamicEvent) event).trace.add("base callback=" + event.getPhase());
        }
    }

    @Cancelable
    public static final class CanceledGetterEvent extends Event {
        private final List<String> trace = new ArrayList<>();

        @Override
        public boolean isCanceled() {
            trace.add("get canceled");
            return true;
        }
    }

    public static final class CanceledGetterSubscriber {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public void skip(CanceledGetterEvent event) {
            event.trace.add("unexpected skipped callback");
        }

        @SubscribeEvent(priority = EventPriority.LOW, receiveCanceled = true)
        public void receive(CanceledGetterEvent event) {
            event.trace.add("receive");
        }
    }

    @Cancelable
    public static final class DynamicGenericEvent<T> extends Event implements IGenericEvent<T> {
        List<String> trace;
        Type selector;

        @Override
        public Type getGenericType() {
            trace.add("selector=" + selector.getTypeName());
            return selector;
        }
    }

    public static final class GenericSubscribers {
        // The raw parameter receives all selectors and can change the value seen by the typed method.
        @SuppressWarnings({"rawtypes", "unchecked"})
        @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
        public void changeSelector(DynamicGenericEvent event) {
            event.trace.add("change=" + event.getPhase());
            if (!event.isCanceled() && event.selector == Integer.class) {
                event.selector = Double.class;
            }
        }

        @SubscribeEvent(priority = EventPriority.LOW)
        public void on(DynamicGenericEvent<String> event) {
            event.trace.add("typed=" + event.getPhase());
        }
    }

    public static final class NestedGenericSubscriber {
        @SubscribeEvent
        public void on(DynamicGenericEvent<List<String>> event) {
            event.trace.add("unexpected equal callback");
        }

        public void donor(List<String> unused) {
        }
    }

    public static final class SnapshotSubscriber {
        private final String label;
        private final List<String> trace;

        SnapshotSubscriber(String label, List<String> trace) {
            this.label = label;
            this.trace = trace;
        }

        @SubscribeEvent
        public void on(ChildEvent event) {
            trace.add(label);
        }
    }

    public static final class ArrayEvent extends Event {
        final EventBus source;
        final boolean nullSlot;
        IEventListener[] array;
        Throwable failure;

        ArrayEvent(EventBus source, boolean nullSlot) {
            this.source = source;
            this.nullSlot = nullSlot;
        }
    }

    public static final class ThrowingEvent extends Event {
        final EventBus bus;
        final IllegalStateException phaseFailure;
        final boolean throwDuringPhase;
        IEventListener[] array;

        ThrowingEvent(EventBus bus, IllegalStateException phaseFailure, boolean throwDuringPhase) {
            this.bus = bus;
            this.phaseFailure = phaseFailure;
            this.throwDuringPhase = throwDuringPhase;
        }

        @Override
        public void setPhase(EventPriority value) {
            if (throwDuringPhase) {
                throw phaseFailure;
            }
            super.setPhase(value);
        }
    }

    public static final class BadGetterEvent extends Event {
        private final IllegalStateException failure;

        public BadGetterEvent() {
            this(null);
        }

        public BadGetterEvent(IllegalStateException failure) {
            this.failure = failure;
        }

        @Override
        protected void setup() {
            super.setup();
        }

        @Override
        public ListenerList getListenerList() {
            if (failure != null) {
                throw failure;
            }
            return null;
        }
    }

    public static final class EqualSubscriber {
        private final String label;
        private final List<String> trace;

        EqualSubscriber(String label, List<String> trace) {
            this.label = label;
            this.trace = trace;
        }

        @SubscribeEvent
        public void on(ChildEvent event) {
            trace.add(label);
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof EqualSubscriber;
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }
}

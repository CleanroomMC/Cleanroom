package net.minecraftforge.fml.common.eventhandler.differential.fixtures;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class LegacyMetadataScenarios {
    private LegacyMetadataScenarios() {
    }

    public static List<String> inheritedAnnotations() {
        var event = new AnnotatedChild();
        var bus = new EventBus();
        event.setCanceled(true);
        event.setResult(Event.Result.DENY);
        return List.of("cancelable=" + event.isCancelable(), "result=" + event.hasResult(),
            "value=" + event.getResult(), "post=" + bus.post(event));
    }

    public static List<String> builtinAnnotations() {
        var pre = new RenderGameOverlayEvent.Pre(new RenderGameOverlayEvent(0, null),
            RenderGameOverlayEvent.ElementType.ALL);
        var post = new RenderGameOverlayEvent.Post(new RenderGameOverlayEvent(0, null),
            RenderGameOverlayEvent.ElementType.ALL);
        var config = new ConfigChangedEvent.OnConfigChangedEvent("test", null, false, false);
        var bus = new EventBus();
        pre.setCanceled(true);
        config.setResult(Event.Result.ALLOW);
        return List.of("pre=" + bus.post(pre), "post cancelable=" + post.isCancelable(),
            "config has result=" + config.hasResult(), "config result=" + config.getResult());
    }

    public static List<String> superChain() {
        var event = new SecondOverride();
        event.trace.add("cancelable=" + event.isCancelable());
        event.trace.add("has result=" + event.hasResult());
        var annotatedOverride = new AnnotatedOverride();
        event.trace.add("same-class=" + annotatedOverride.isCancelable() + "/" + annotatedOverride.hasResult());
        return event.trace;
    }

    public static List<String> abstractChild() {
        var event = new ConcreteChild();
        event.setCanceled(true);
        return List.of("cancelable=" + event.isCancelable(), "has result=" + event.hasResult(),
            "post=" + new EventBus().post(event));
    }

    public static List<String> abstractRegistration() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new AbstractSubscriber(trace));
        bus.post(new ConcreteChild());
        return trace;
    }

    public static List<String> constructorProbe() {
        var bus = new EventBus();
        ProbeEvent.constructions = 0;
        bus.register(new ProbeSubscriber());
        return List.of("constructed=" + ProbeEvent.constructions);
    }

    public static List<String> injectedConstructorRegistration() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new ParameterizedSubscriber(trace));
        var event = new ParameterizedEvent("explicit");
        bus.post(event);
        return trace;
    }

    public static List<String> inheritedHandwrittenPropertyConflict() {
        var event = new UnannotatedBelowConflict();
        return List.of("cancelable=" + event.isCancelable(), "has result=" + event.hasResult());
    }

    public static List<String> helperSuperCall() {
        var event = new HelperSuper();
        return List.of("virtual=" + event.isCancelable(), "helper=" + event.callSuper());
    }

    public static List<String> handwrittenGap() {
        var first = new FirstList().getListenerList();
        var gap = new ListGap().getListenerList();
        var last = new PairAfterGap().getListenerList();
        return List.of("distinct gap=" + (gap != first), "distinct final pair=" + (last != gap));
    }

    public static List<String> handwrittenListChain() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        var parent = new OrdinaryParent();
        var first = new FirstList();
        var leaf = new SecondList();
        var subscriber = new HandwrittenSubscriber(trace);
        bus.register(subscriber);
        int id = busId(bus);
        parent.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("parent"));
        first.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("first"));
        leaf.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("leaf"));
        trace.add("distinct=" + (first.getListenerList() != parent.getListenerList()
            && leaf.getListenerList() != first.getListenerList()));
        trace.add("shared=" + (leaf.getListenerList() == new SecondList().getListenerList()));
        bus.post(leaf);
        trace.add("unregister");
        bus.unregister(subscriber);
        bus.post(new SecondList());
        return trace;
    }

    public static List<String> instanceLists() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new InstanceListSubscriber(trace));
        int id = busId(bus);
        var first = new InstanceListEvent(false);
        var second = new InstanceListEvent(true);
        second.getListenerList().register(id, EventPriority.NORMAL, event -> trace.add("other"));
        trace.add("distinct=" + (first.getListenerList() != second.getListenerList()));
        bus.post(first);
        bus.post(second);
        return trace;
    }

    static int busId(EventBus bus) throws ReflectiveOperationException {
        Field id = EventBus.class.getDeclaredField("busID");
        id.setAccessible(true);
        return id.getInt(bus);
    }

    @Cancelable
    @Event.HasResult
    public static class AnnotatedParent extends Event {
    }

    public static class AnnotatedChild extends AnnotatedParent {
    }

    public static class FirstOverride extends AnnotatedParent {
        final List<String> trace = new ArrayList<>();

        @Override
        public boolean isCancelable() {
            trace.add("first cancelable");
            return super.isCancelable();
        }

        @Override
        public boolean hasResult() {
            trace.add("first result");
            return super.hasResult();
        }
    }

    public static class SecondOverride extends FirstOverride {
        @Override
        public boolean isCancelable() {
            trace.add("second cancelable");
            return super.isCancelable();
        }

        @Override
        public boolean hasResult() {
            trace.add("second result");
            return super.hasResult();
        }
    }

    @Cancelable
    @Event.HasResult
    public static class AnnotatedOverride extends Event {
        @Override
        public boolean isCancelable() {
            return super.isCancelable();
        }

        @Override
        public boolean hasResult() {
            return super.hasResult();
        }
    }

    @Cancelable
    @Event.HasResult
    public abstract static class AbstractParent extends Event {
    }

    public static final class ConcreteChild extends AbstractParent {
    }

    public record AbstractSubscriber(List<String> trace) {
        @SubscribeEvent
        public void on(AbstractParent event) {
            trace.add("abstract=" + event.hasResult());
        }
    }

    public static final class ProbeEvent extends Event {
        static int constructions;

        public ProbeEvent() {
            constructions++;
        }
    }

    public static final class ProbeSubscriber {
        @SubscribeEvent
        public void on(ProbeEvent event) {
        }
    }

    public static final class ParameterizedEvent extends Event {
        private final String value;

        public ParameterizedEvent(String value) {
            this.value = value;
        }
    }

    public record ParameterizedSubscriber(List<String> trace) {
        @SubscribeEvent
        public void on(ParameterizedEvent event) {
            trace.add("value=" + event.value);
        }
    }

    @Cancelable
    @Event.HasResult
    public static class InheritedConflict extends AnnotatedOverride {
    }

    public static final class UnannotatedBelowConflict extends InheritedConflict {
    }

    @Cancelable
    public static final class HelperSuper extends Event {
        public boolean callSuper() {
            return super.isCancelable();
        }
    }

    public static class OrdinaryParent extends Event {
    }

    public static class FirstList extends OrdinaryParent {
        private static ListenerList first;

        @Override
        protected void setup() {
            super.setup();
            if (first == null) {
                first = new ListenerList(super.getListenerList());
            }
        }

        @Override
        public ListenerList getListenerList() {
            return first;
        }
    }

    public static class SecondList extends FirstList {
        private static ListenerList second;

        @Override
        protected void setup() {
            super.setup();
            if (second == null) {
                second = new ListenerList(super.getListenerList());
            }
        }

        @Override
        public ListenerList getListenerList() {
            return second;
        }
    }

    public static final class HandwrittenSubscriber {
        private final List<String> trace;

        HandwrittenSubscriber(List<String> trace) {
            this.trace = trace;
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void on(SecondList event) {
            trace.add("registered=" + event.getPhase());
        }
    }

    public static class ListGap extends FirstList {
    }

    public static final class PairAfterGap extends ListGap {
        private static ListenerList afterGap;

        @Override
        protected void setup() {
            super.setup();
            if (afterGap == null) {
                afterGap = new ListenerList(super.getListenerList());
            }
        }

        @Override
        public ListenerList getListenerList() {
            return afterGap;
        }
    }

    public static class InstanceListEvent extends Event {
        private static final ListenerList first = new ListenerList();
        private static final ListenerList second = new ListenerList();
        private final boolean alternate;

        public InstanceListEvent() {
            this(false);
        }

        public InstanceListEvent(boolean alternate) {
            this.alternate = alternate;
        }

        @Override
        protected void setup() {
            super.setup();
        }

        @Override
        public ListenerList getListenerList() {
            return alternate ? second : first;
        }
    }

    public record InstanceListSubscriber(List<String> trace) {
        @SubscribeEvent
        public void on(InstanceListEvent event) {
            trace.add("registered");
        }
    }
}

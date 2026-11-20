package net.minecraftforge.fml.common.eventhandler.differential.fixtures;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.inherited.PackageParent;

public final class LegacySubscriberScenarios {
    private LegacySubscriberScenarios() {
    }

    public static List<String> staticAndInstance() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(StaticChild.class);
        bus.post(new CallbackEvent(trace));
        trace.add("static hidden");
        bus.register(StaticHides.class);
        bus.post(new CallbackEvent(trace));
        bus.unregister(StaticHides.class);
        trace.add("static base");
        bus.register(StaticParent.class);
        bus.post(new CallbackEvent(trace));
        bus.unregister(StaticParent.class);
        trace.add("instance");
        bus.register(new StaticParent());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> inheritedAnnotation() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new InheritedChild());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> publicizedDispatchTarget() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new CrossPackageChild());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> subscriberAccess() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new PackageSubscriber());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> privateSubscriber() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new PrivateSubscriber());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> defaultsAndDiamond() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new InterfaceSubscriber());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> covariantBridge() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        bus.register(new CovariantChild());
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> objectParameter() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var listener = new ObjectSubscriber(trace);
        var event = new CallbackEvent(trace);
        var bus = new EventBus();
        var register = EventBus.class.getDeclaredMethod("register", Class.class, Object.class,
            java.lang.reflect.Method.class, ModContainer.class);
        register.setAccessible(true);
        register.invoke(bus, CallbackEvent.class, listener, ObjectSubscriber.class.getMethod("on", Object.class),
            Loader.instance().getMinecraftModContainer());
        bus.post(event);
        trace.add("same event=" + (listener.captured == event));
        return trace;
    }

    public static List<String> receiverBinding() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        var first = new ReceiverSubscriber("first");
        var second = new ReceiverSubscriber("second");
        bus.register(first);
        bus.register(second);
        bus.post(new CallbackEvent(trace));
        bus.unregister(first);
        bus.post(new CallbackEvent(trace));
        return trace;
    }

    public static List<String> ownerContext() {
        var trace = new ArrayList<String>();
        var bus = new EventBus();
        var loader = Loader.instance();
        ModContainer previous = loader.activeModContainer();
        var registrationOwner = owner("subscriber-owner");
        var postingOwner = owner("posting-owner");
        try {
            loader.setActiveModContainer(registrationOwner);
            bus.register(new ContextSubscriber());
            loader.setActiveModContainer(postingOwner);
            var event = new OwnerEvent(trace);
            bus.post(event);
            trace.add("event owner=" + (event.owner == registrationOwner));
            trace.add("restored=" + (loader.activeModContainer() == postingOwner));
        } finally {
            loader.setActiveModContainer(previous);
        }
        return trace;
    }

    private static ModContainer owner(String id) {
        var metadata = new ModMetadata();
        metadata.modId = id;
        metadata.name = id;
        return new DummyModContainer(metadata);
    }

    public static final class CallbackEvent extends Event {
        private final List<String> trace;

        public CallbackEvent() {
            this(new ArrayList<>());
        }

        public CallbackEvent(List<String> trace) {
            this.trace = trace;
        }

        public void record(String message) {
            trace.add(message);
        }
    }

    public static class StaticParent {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public static void staticCallback(CallbackEvent event) {
            event.record("base static");
        }

        @SubscribeEvent
        public int instanceCallback(CallbackEvent event) {
            event.record("base instance");
            return 1;
        }
    }

    public static final class StaticChild extends StaticParent {
    }

    public static final class StaticHides extends StaticParent {
        @SubscribeEvent
        public static void staticCallback(CallbackEvent event) {
            event.record("derived static");
        }
    }

    public static class InheritedParent {
        @SubscribeEvent(priority = EventPriority.HIGH)
        public void on(CallbackEvent event) {
            event.record("unexpected parent");
        }
    }

    public static final class InheritedChild extends InheritedParent {
        @Override
        public void on(CallbackEvent event) {
            event.record("child:" + event.getPhase());
        }
    }

    public static final class CrossPackageChild extends PackageParent {
        // In ordinary Java this is unrelated to the package-private method on PackageParent.
        public void on(CallbackEvent event) {
            event.record("cross-package child:" + event.getPhase());
        }
    }

    public static final class PrivateSubscriber {
        @SubscribeEvent
        private void hidden(CallbackEvent event) {
            event.record("unexpected private");
        }

        @SubscribeEvent
        public void visible(CallbackEvent event) {
            event.record("public");
        }
    }

    public interface DefaultRoot {
        @SubscribeEvent(priority = EventPriority.HIGH)
        default void defaults(CallbackEvent event) {
            event.record("default:" + event.getPhase());
        }
    }

    public interface Left extends DefaultRoot {
    }

    public interface Right extends DefaultRoot {
    }

    public interface InterfaceContract {
        @SubscribeEvent
        void on(CallbackEvent event);
    }

    public static final class InterfaceSubscriber implements Left, Right, InterfaceContract {
        @Override
        public void on(CallbackEvent event) {
            event.record("interface annotation:" + event.getPhase());
        }
    }

    public static class CovariantParent {
        @SubscribeEvent
        public Number on(CallbackEvent event) {
            event.record("unexpected covariant parent");
            return 0;
        }
    }

    public static final class CovariantChild extends CovariantParent {
        @Override
        public Integer on(CallbackEvent event) {
            event.record("covariant child");
            return 1;
        }
    }

    public static final class ObjectSubscriber {
        private Object captured;
        private final List<String> trace;

        ObjectSubscriber(List<String> trace) {
            this.trace = trace;
        }

        @SubscribeEvent
        public void on(Object event) {
            captured = event;
            trace.add("object callback");
        }
    }

    public static final class ReceiverSubscriber {
        private final String label;

        ReceiverSubscriber(String label) {
            this.label = label;
        }

        @SubscribeEvent
        public void on(CallbackEvent event) {
            event.record(label);
        }
    }

    public static final class OwnerEvent extends Event implements IContextSetter {
        private final List<String> trace;
        private ModContainer owner;

        public OwnerEvent() {
            this(new ArrayList<>());
        }

        public OwnerEvent(List<String> trace) {
            this.trace = trace;
        }

        @Override
        public void setModContainer(ModContainer container) {
            owner = container;
            trace.add("context set=" + (container == Loader.instance().activeModContainer()));
        }
    }

    public static final class ContextSubscriber {
        @SubscribeEvent
        public void on(OwnerEvent event) {
            event.trace.add("listener owner=" + (event.owner == Loader.instance().activeModContainer()));
        }
    }
}

final class PackageSubscriber {
    @SubscribeEvent(priority = EventPriority.HIGH)
    void packaged(LegacySubscriberScenarios.CallbackEvent event) {
        event.record("package:" + event.getPhase());
    }

    @SubscribeEvent
    protected void protectedMethod(LegacySubscriberScenarios.CallbackEvent event) {
        event.record("protected:" + event.getPhase());
    }
}

/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.eventbus;

import com.cleanroommc.cleanroom.event.CancellableEventChannel;
import com.cleanroommc.cleanroom.event.EventChannel;
import com.cleanroommc.cleanroom.event.Subscription;
import com.cleanroommc.cleanroom.event.SubscriptionGroup;

import org.junit.jupiter.api.Test;

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.InheritableEvent;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.EventListener;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventChannelTest {

    private static final CancellableEventChannel<Pulse> PULSES = EventChannel.cancellableRecord(Pulse.class);
    private static final EventChannel<Change> CHANGES = EventChannel.mutable(Change.class);

    private static ModContainer owner(String id) {
        var metadata = new ModMetadata();
        metadata.modId = id;
        metadata.name = id;
        return new DummyModContainer(metadata);
    }

    private static void assertRejectedWithoutInstalling(
        EventChannel<?> channel, SubscriptionGroup group, MethodHandles.Lookup lookup, Object target, String reason) {
        assertThatThrownBy(() -> channel.subscribe(group, lookup, target))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining(reason);
        assertThat(channel.raw().hasListeners()).isFalse();
    }

    private static Subscription installRemoval(SubscriptionGroup group, String source, Runnable remove)
        throws ReflectiveOperationException {
        return install(group, source, () -> remove);
    }

    private static Subscription install(SubscriptionGroup group, String source, Supplier<Runnable> registration)
        throws ReflectiveOperationException {
        var install = SubscriptionGroup.class.getDeclaredMethod("install", String.class, Supplier.class);
        install.setAccessible(true);
        return (Subscription) install.invoke(group, source, registration);
    }

    @Test
    void hasListenersGuardsDispatchAndFireReturnsTheMutatedInstance() {
        var change = new Change();
        assertThat(CHANGES.hasListeners()).isFalse();
        assertThat(CHANGES.fire(change)).isSameAs(change);
        assertThat(change.value).isZero();

        try (var group = new SubscriptionGroup(owner("channel-fire"))) {
            CHANGES.subscribe(group, event -> event.value++);
            CHANGES.subscribe(group, Priority.HIGH, event -> event.value = 7);
            CHANGES.subscribe(group, "double", event -> event.value *= 2);
            assertThat(CHANGES.hasListeners()).isTrue();
            assertThat(CHANGES.fire(change)).isSameAs(change);
            assertThat(change.value).isEqualTo(16);
        }
        assertThat(CHANGES.hasListeners()).isFalse();
    }

    @Test
    void rawBusAcceptsBareForgeTokensOnTheDefaultScope() {
        var channel = EventChannel.of(Change.class);
        EventBus<Change> raw = EventBus.create(channel.scope(), channel.eventType());
        assertThat(CHANGES.eventType()).isSameAs(Change.class);
        assertThat(CHANGES.scope()).isSameAs(BusGroup.DEFAULT);
        assertThat(channel.raw()).isSameAs(raw);

        EventListener token = raw.addListener(event -> event.value++);
        try {
            assertThat(CHANGES.hasListeners()).isTrue();
            assertThat(CHANGES.fire(new Change()).value).isOne();
        } finally {
            raw.removeListener(token);
        }
        assertThat(CHANGES.hasListeners()).isFalse();
    }

    @Test
    void generalFactoriesKeepTheChosenScopeAndCompileTimeCancellation() {
        var defaultChannel = EventChannel.cancellable(Pulse.class);
        var scope = BusGroup.create("channel-general-cancellable");
        try (var group = new SubscriptionGroup(owner("channel-general"))) {
            var privateChannel = EventChannel.cancellable(scope, Pulse.class);
            assertThat(defaultChannel.raw()).isSameAs(PULSES.raw());
            assertThat(privateChannel.raw()).isSameAs(EventChannel.of(scope, Pulse.class).raw());
            privateChannel.subscribeCancelling(group, event -> event.value() > 0);
            assertThat(privateChannel.post(new Pulse(1))).isTrue();
            assertThat(defaultChannel.post(new Pulse(1))).isFalse();
            assertThatThrownBy(() -> EventChannel.of(String.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not a Forge 7 event type");
        } finally {
            scope.dispose();
        }
    }

    @Test
    void managedListenerRestoresRawPublishersOwnerBeforeTheNextListener() {
        var subscriber = owner("channel-raw-subscriber");
        var changedOwner = owner("channel-raw-changed");
        var publisher = owner("channel-raw-publisher");
        var previous = ModEventContext.swapOwner(null);
        var raw = EventBus.create(CHANGES.scope(), CHANGES.eventType());
        var observed = new ArrayList<ModContainer>();
        var token = raw.addListener(Priority.LOW, event -> observed.add(ModEventContext.owner()));
        try (var group = new SubscriptionGroup(subscriber)) {
            CHANGES.subscribe(group, event -> {
                assertThat(Loader.instance().activeModContainer()).isSameAs(subscriber);
                Loader.instance().setActiveModContainer(changedOwner);
                assertThat(Loader.instance().activeModContainer()).isSameAs(changedOwner);
            });

            raw.post(new Change());
            assertThat(ModEventContext.owner()).isNull();
            ModEventContext.setOwner(publisher);
            raw.post(new Change());
            assertThat(ModEventContext.owner()).isSameAs(publisher);
            assertThat(observed).containsExactly(null, publisher);
        } finally {
            raw.removeListener(token);
            ModEventContext.setOwner(previous);
        }
    }

    @Test
    void cancellingOverloadsUseNormalPriorityAndAllowMonitoring() {
        var trace = new ArrayList<String>();
        try (var group = new SubscriptionGroup(owner("channel-cancelling-overloads"))) {
            PULSES.monitor(group, (event, cancelled) -> trace.add("monitor=" + cancelled));
            PULSES.subscribe(group, event -> trace.add("normal"));
            PULSES.subscribeCancelling(group, Priority.HIGH, event -> {
                trace.add("high");
                return false;
            });
            var cancel = PULSES.subscribeCancelling(group, event -> {
                trace.add("cancel");
                return true;
            });
            assertThat(PULSES.post(new Pulse(1))).isTrue();
            cancel.close();

            var namedCancel = PULSES.subscribeCancelling(group, "named cancellation", event -> {
                trace.add("named cancel");
                return true;
            });
            assertThat(PULSES.post(new Pulse(2))).isTrue();
            namedCancel.close();

            var always = PULSES.subscribeAlwaysCancelling(group, event -> trace.add("always"));
            assertThat(PULSES.post(new Pulse(3))).isTrue();
            always.close();

            var namedAlways = PULSES.subscribeAlwaysCancelling(
                group, "named always", event -> trace.add("named always")
            );
            assertThat(PULSES.post(new Pulse(4))).isTrue();
            namedAlways.close();

            PULSES.subscribeAlwaysCancelling(group, Priority.HIGH, event -> trace.add("high always"));
            assertThat(PULSES.post(new Pulse(5))).isTrue();
        }
        assertThat(trace).containsExactly(
            "high", "normal", "cancel", "monitor=true", "high", "normal", "named cancel", "monitor=true", "high",
            "normal", "always", "monitor=true", "high", "normal", "named always", "monitor=true", "high", "high always",
            "monitor=true"
        );
    }

    @Test
    void cancellableMutableAndInheritableFactoriesDispatchThroughForge() {
        var mutable = EventChannel.cancellableMutable(MutablePulse.class);
        var family = EventChannel.cancellableInheritable(CancellablePulseFamily.class);
        try (var group = new SubscriptionGroup(owner("channel-cancellable-kinds"))) {
            mutable.subscribeCancelling(group, event -> true);
            family.subscribeCancelling(group, event -> true);

            var event = new MutablePulse();
            assertThat(mutable.post(event)).isTrue();
            assertThat(
                EventChannel.cancellableRecord(CancellableInheritedPulse.class).post(new CancellableInheritedPulse())
            ).isTrue();
        }
    }

    @Test
    void closingOneRegistrationKeepsAnotherRegistrationOfTheSameCallback() {
        var firstOwner = owner("channel-first");
        var secondOwner = owner("channel-second");
        var trace = new ArrayList<ModContainer>();
        Consumer<Pulse> callback = event -> trace.add(Loader.instance().activeModContainer());
        try (var first = new SubscriptionGroup(firstOwner); var second = new SubscriptionGroup(secondOwner)) {
            var firstToken = PULSES.subscribe(first, Priority.NORMAL, "first callback", callback);
            var secondToken = PULSES.subscribe(second, Priority.NORMAL, "second callback", callback);
            PULSES.post(new Pulse(0));
            firstToken.close();
            firstToken.close();
            first.close();
            assertThat(firstToken.isClosed()).isTrue();
            assertThat(secondToken.isClosed()).isFalse();
            PULSES.post(new Pulse(0));
            assertThat(trace).containsExactly(firstOwner, secondOwner, secondOwner);
            assertThatThrownBy(() -> PULSES.subscribe(first, Priority.NORMAL, "after close", callback)).isInstanceOf(
                IllegalStateException.class
            );
        }
        PULSES.post(new Pulse(0));
        assertThat(trace).containsExactly(firstOwner, secondOwner, secondOwner);
    }

    @Test
    void cancellationAndMonitoringUseUpstreamSemantics() {
        var trace = new ArrayList<String>();
        try (var group = new SubscriptionGroup(owner("channel-cancellation"))) {
            var cancel = PULSES.subscribeCancelling(group, Priority.HIGH, "conditional", event -> event.value() == 15);
            var always = PULSES.subscribeAlwaysCancelling(group, Priority.HIGH, "always", event -> trace.add("always"));
            PULSES.subscribe(group, Priority.NORMAL, "ordinary", event -> trace.add("ordinary"));
            PULSES.monitor(group, "monitor", (event, cancelled) -> trace.add("monitor=" + cancelled));

            assertThat(PULSES.post(new Pulse(15))).isTrue();
            assertThat(trace).containsExactly("monitor=true");
            assertThat(PULSES.post(new Pulse(14))).isTrue();
            assertThat(trace).containsExactly("monitor=true", "always", "monitor=true");
            cancel.close();
            always.close();
            assertThat(PULSES.post(new Pulse(15))).isFalse();
            assertThat(trace).containsExactly("monitor=true", "always", "monitor=true", "ordinary", "monitor=false");
        }
    }

    @Test
    void annotatedBatchUsesExplicitLookupForPrivateStaticAndInstanceCallbacks() {
        var owner = owner("channel-bulk");
        var actual = new AnnotatedListeners();
        try (var group = new SubscriptionGroup(owner)) {
            var lookup = MethodHandles.lookup();
            var first = new Change();

            var changeTokens = CHANGES.subscribe(group, lookup, actual);
            var pulseTokens = PULSES.subscribe(group, lookup, actual);
            assertThat(changeTokens).hasSize(1);
            assertThat(pulseTokens).hasSize(3);
            var other = new AnnotatedListeners();
            assertThatThrownBy(() -> CHANGES.subscribe(
                group, lookup.dropLookupMode(MethodHandles.Lookup.PRIVATE),
                other
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Lookup cannot access");
            CHANGES.post(first);
            assertThat(first.value).isEqualTo(13);
            assertThat(actual.seenOwner).isSameAs(owner);
            assertThat(actual.directLambda).isTrue();
            assertThat(PULSES.post(new Pulse(2))).isTrue();
            assertThat(actual.alwaysCalls).isZero();
            assertThat(PULSES.post(new Pulse(-2))).isTrue();
            assertThat(actual.alwaysCalls).isOne();
            assertThat(actual.monitorResults).containsExactly(true, true);
            changeTokens.forEach(Subscription::close);
            pulseTokens.forEach(Subscription::close);
            assertThat(CHANGES.fire(new Change()).value).isZero();
            assertThat(PULSES.post(new Pulse(2))).isFalse();
            actual.seenOwner = null;
            CHANGES.subscribe(group, lookup, other);
            assertThat(CHANGES.fire(new Change()).value).isEqualTo(13);
            assertThat(other.seenOwner).isSameAs(owner);
            assertThat(actual.seenOwner).isNull();
        }
    }

    @Test
    void invalidAnnotatedBatchesLeaveUpstreamBusesUntouched() {
        var lookup = MethodHandles.lookup();
        var scope = BusGroup.create("cleanroom-channel-invalid");
        try (var group = new SubscriptionGroup(owner("channel-invalid"))) {
            var changes = EventChannel.mutable(scope, Change.class);
            var pulses = EventChannel.cancellableRecord(scope, Pulse.class);
            assertRejectedWithoutInstalling(
                changes, group, lookup, new InvalidBatch(), "require a CancellableEventChannel"
            );
            assertRejectedWithoutInstalling(pulses, group, lookup, new InvalidPriorityBatch(), "MONITOR cannot cancel");
            assertRejectedWithoutInstalling(
                changes, group, lookup, new NoArgumentBatch(), "expected a Forge 7 event argument"
            );
            var ordinaryPulses = EventChannel.record(scope, Pulse.class);
            assertRejectedWithoutInstalling(
                ordinaryPulses, group, lookup, new InvalidMonitorBatch(), "require a CancellableEventChannel"
            );
            assertRejectedWithoutInstalling(
                changes, group, lookup.dropLookupMode(MethodHandles.Lookup.PRIVATE), new InaccessibleBatch(),
                "Lookup cannot access"
            );
        } finally {
            scope.dispose();
        }
    }

    @Test
    void wholeClassRegistrationRoutesMultipleForge7EventsAndSkipsLegacy() {
        var scope = BusGroup.create("cleanroom-channel-whole-class");
        var defaultOwner = owner("channel-whole-default");
        var previousOwner = ModEventContext.swapOwner(defaultOwner);
        MultiEventAnnotatedListeners.staticCalls = 0;
        var handler = new MultiEventAnnotatedListeners();
        try {
            var changes = EventChannel.mutable(scope, Change.class);
            var pulses = EventChannel.cancellableRecord(scope, Pulse.class);

            var subscriptions = EventChannel.register(scope, MethodHandles.lookup(), handler);
            assertThat(subscriptions).hasSize(4);

            var change = new Change();
            changes.post(change);
            assertThat(change.value).isEqualTo(21);
            assertThat(MultiEventAnnotatedListeners.staticCalls).isZero();
            assertThat(pulses.post(new Pulse(5))).isTrue();
            assertThat(MultiEventAnnotatedListeners.staticCalls).isOne();
            assertThat(pulses.post(new Pulse(-1))).isFalse();
            assertThat(MultiEventAnnotatedListeners.staticCalls).isEqualTo(2);
            assertThat(handler.monitorResults).containsExactly(true, false);

            subscriptions.forEach(Subscription::close);
            assertThat(changes.hasListeners()).isFalse();
            assertThat(pulses.hasListeners()).isFalse();

            var mixedScope = BusGroup.create("cleanroom-channel-whole-invalid");
            try (var group = new SubscriptionGroup(owner("channel-whole-invalid"))) {
                assertThatThrownBy(() -> EventChannel.register(
                    group, mixedScope, MethodHandles.lookup(), new InvalidWholeClassBatch()
                ))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("CancellableEventChannel");
                assertThat(EventChannel.mutable(mixedScope, Change.class).hasListeners()).isFalse();
                assertThat(EventChannel.cancellableRecord(mixedScope, Pulse.class).hasListeners()).isFalse();
            } finally {
                mixedScope.dispose();
            }
        } finally {
            scope.dispose();
            SubscriptionGroup.closeDefaultGroup(defaultOwner);
            ModEventContext.setOwner(previousOwner);
        }
    }

    @Test
    void factoryRejectsAnEventOutsideTheScopeBaseType() {
        BusGroup scope = BusGroup.create("cleanroom-channel-restricted", Change.class);
        try {
            var changes = EventChannel.mutable(scope, Change.class);
            assertThatThrownBy(() -> EventChannel.cancellableRecord(scope, Pulse.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires all events");
            assertThat(changes.hasListeners()).isFalse();
        } finally {
            scope.dispose();
        }
    }

    @Test
    void annotatedCallbacksFallBackWithAnAccessCapableLookupWithoutFullPrivilege() {
        var lookup = MethodHandles.lookup().dropLookupMode(MethodHandles.Lookup.PRIVATE);
        assertThat(lookup.hasFullPrivilegeAccess()).isFalse();
        var listeners = new FallbackListeners();
        try (var group = new SubscriptionGroup(owner("channel-fallback"))) {
            CHANGES.subscribe(group, lookup, listeners);
            PULSES.subscribe(group, lookup, listeners);
            var change = new Change();
            CHANGES.post(change);
            assertThat(change.value).isEqualTo(17);
            assertThat(PULSES.post(new Pulse(3))).isTrue();
            assertThat(PULSES.post(new Pulse(4))).isFalse();
            assertThat(listeners.monitorResults).containsExactly(true, false);
        }
    }

    @Test
    void privateStaticSubscriberFromAnotherClassLoaderUsesTheGrantedLookup() throws Exception {
        Class<?> originalClass = IsolatedListener.class;
        String classPath = "/" + originalClass.getName().replace('.', '/') + ".class";
        byte[] classBytes;
        try (var resource = Objects.requireNonNull(originalClass.getResourceAsStream(classPath))) {
            classBytes = resource.readAllBytes();
        }
        Class<?> isolatedClass = new ClassLoader(originalClass.getClassLoader()) {

            Class<?> define() {
                return defineClass(originalClass.getName(), classBytes, 0, classBytes.length);
            }

        }.define();
        var lookup = MethodHandles.privateLookupIn(isolatedClass, MethodHandles.lookup());
        assertThat(lookup.hasFullPrivilegeAccess()).isFalse();
        try (var group = new SubscriptionGroup(owner("channel-isolated-lookup"))) {
            var subscriptions = PULSES.subscribe(group, lookup, isolatedClass);
            assertThat(PULSES.post(new Pulse(4))).isTrue();
            subscriptions.getFirst().close();
            assertThat(PULSES.post(new Pulse(4))).isFalse();
        }
    }

    @Test
    void exceptionalCallbacksPreserveTheCheckedExceptionAndCleanUpTheOwnerFrame() {
        var listenerOwner = owner("channel-exception");
        var changedOwner = owner("channel-mutated-exception");
        var fullLookup = MethodHandles.lookup();
        var caller = ModEventContext.swapOwner(null);
        try {
            for (var lookup : List.of(fullLookup, fullLookup.dropLookupMode(MethodHandles.Lookup.PRIVATE))) {
                var listener = new ThrowingListener(listenerOwner, changedOwner);
                try (var group = new SubscriptionGroup(listenerOwner)) {
                    CHANGES.subscribe(group, lookup, listener);
                    assertThatThrownBy(() -> CHANGES.post(new Change())).isSameAs(listener.failure);
                    assertThat(ModEventContext.owner()).isNull();
                    assertThat(ModEventContext.setDispatchOwner(changedOwner)).isFalse();
                }
            }
        } finally {
            ModEventContext.setOwner(caller);
        }
    }

    @Test
    void nestedPostRestoresItsOwnerAndSameOwnerCallbacksCanMutateContext() {
        var outerOwner = owner("channel-outer");
        var innerOwner = owner("channel-inner");
        var changedOwner = owner("channel-mutated");
        var caller = ModEventContext.swapOwner(outerOwner);
        try (var outer = new SubscriptionGroup(outerOwner); var inner = new SubscriptionGroup(innerOwner)) {
            PULSES.subscribe(inner, Priority.NORMAL, "inner", pulse -> {
                assertThat(ModEventContext.owner()).isSameAs(innerOwner);
                ModEventContext.setOwner(null);
            });
            CHANGES.subscribe(outer, Priority.NORMAL, "outer", change -> {
                assertThat(ModEventContext.owner()).isSameAs(outerOwner);
                PULSES.post(new Pulse(0));
                assertThat(ModEventContext.owner()).isSameAs(outerOwner);
                ModEventContext.setOwner(changedOwner);
            });
            CHANGES.subscribe(outer, Priority.LOW, "after mutation", change -> {
                assertThat(ModEventContext.owner()).isSameAs(outerOwner);
                change.value++;
            });

            var change = new Change();
            CHANGES.post(change);
            assertThat(change.value).isOne();
            assertThat(ModEventContext.owner()).isSameAs(outerOwner);
        } finally {
            ModEventContext.setOwner(caller);
        }
    }

    @Test
    void groupCloseContinuesAfterErrorsAndRetriesOnlyFailedRemovals() throws ReflectiveOperationException {
        var trace = new ArrayList<String>();
        var firstFailure = new AssertionError("first removal");
        var secondFailure = new IllegalStateException("second removal");
        try (var group = new SubscriptionGroup(owner("channel-cleanup"))) {
            var last = installRemoval(group, "successful removal", () -> trace.add("success"));
            var second = installRemoval(group, "runtime exception", () -> {
                boolean retry = trace.contains("exception");
                trace.add("exception");
                if (!retry) {
                    throw secondFailure;
                }
            });
            var first = installRemoval(group, "error", () -> {
                boolean retry = trace.contains("error");
                trace.add("error");
                if (!retry) {
                    throw firstFailure;
                }
            });

            assertThatThrownBy(group::close).isSameAs(firstFailure);
            assertThat(firstFailure.getSuppressed()).containsExactly(secondFailure);
            assertThat(trace).containsExactly("error", "exception", "success");
            assertThat(last.isClosed()).isTrue();
            assertThat(second.isClosed()).isFalse();
            assertThat(first.isClosed()).isFalse();

            group.close();
            assertThat(trace).containsExactly("error", "exception", "success", "error", "exception");
            assertThat(second.isClosed()).isTrue();
            assertThat(first.isClosed()).isTrue();
        }
    }

    @Test
    void groupRetriesRemovalWhenAnInstallationFinishesAfterClose() throws ReflectiveOperationException {
        var failure = new AssertionError("racing removal");
        var attempts = new ArrayList<String>();
        try (var group = new SubscriptionGroup(owner("channel-cleanup-race"))) {
            assertThatThrownBy(() -> install(group, "racing registration", () -> {
                group.close();
                return () -> {
                    attempts.add("remove");
                    if (attempts.size() == 1) {
                        throw failure;
                    }
                };
            })).cause().isSameAs(failure);

            group.close();
            assertThat(attempts).containsExactly("remove", "remove");
        }
        assertThat(attempts).containsExactly("remove", "remove");
    }

    @Test
    void sameTypeInDifferentScopesRemainsIsolated() {
        BusGroup isolated = BusGroup.create("cleanroom-test-event-scope");
        try (var group = new SubscriptionGroup(owner("channel-isolation"))) {
            var channel = EventChannel.cancellableRecord(isolated, Pulse.class);
            var trace = new ArrayList<String>();
            PULSES.subscribe(group, Priority.NORMAL, "default", event -> trace.add("default"));
            channel.subscribe(group, Priority.NORMAL, "isolated", event -> trace.add("isolated"));
            channel.post(new Pulse(1));
            assertThat(trace).containsExactly("isolated");
            PULSES.post(new Pulse(2));
            assertThat(trace).containsExactly("isolated", "default");
        } finally {
            isolated.dispose();
        }
    }

    @Test
    void defaultInheritableChannelSharesTheUpstreamDefaultScope() {
        var channel = EventChannel.inheritable(PulseFamily.class);
        var trace = new ArrayList<PulseFamily>();
        try (var group = new SubscriptionGroup(owner("channel-default-inherited"))) {
            channel.subscribe(group, Priority.NORMAL, "inherited", trace::add);
            var event = new InheritedPulse();
            EventBus.create(BusGroup.DEFAULT, InheritedPulse.class).post(event);
            assertThat(trace).containsExactly(event);
        }
    }

    @Test
    void annotatedInheritableSubscribersReceiveFinalRecordEvents() {
        BusGroup scope = BusGroup.create("cleanroom-channel-inherited");
        try (var group = new SubscriptionGroup(owner("channel-inherited"))) {
            var channel = EventChannel.record(scope, InheritedPulse.class);
            var listener = new InheritedListener();
            var lookup = MethodHandles.lookup();
            var family = EventChannel.inheritable(scope, PulseFamily.class);
            assertThat(family.subscribe(group, lookup, listener)).hasSize(1);
            var event = new InheritedPulse();
            channel.post(event);
            assertThat(listener.lastEvent).isSameAs(event);
        } finally {
            scope.dispose();
        }
    }

    private interface PulseFamily extends InheritableEvent { }

    private interface CancellablePulseFamily extends InheritableEvent, Cancellable { }

    public record Pulse(int value) implements RecordEvent, Cancellable { }

    private record InheritedPulse() implements RecordEvent, PulseFamily { }

    private static final class MutablePulse extends MutableEvent implements Cancellable { }

    private record CancellableInheritedPulse() implements RecordEvent, CancellablePulseFamily { }

    private static final class InheritedListener {

        private PulseFamily lastEvent;

        @SubscribeEvent
        private void onPulse(PulseFamily event) {
            lastEvent = event;
        }

    }

    private static final class Change extends MutableEvent {

        private int value;

    }

    private static final class AnnotatedListeners {

        private final List<Boolean> monitorResults = new ArrayList<>();

        private ModContainer seenOwner;
        private boolean directLambda;
        private int alwaysCalls;

        @SubscribeEvent
        private static boolean pulse(Pulse event) {
            return event.value() > 0;
        }

        @SubscribeEvent
        private void change(Change event) {
            event.value = 13;
            seenOwner = Loader.instance().activeModContainer();
            directLambda = StackWalker.getInstance(Set.of(
                StackWalker.Option.RETAIN_CLASS_REFERENCE,
                StackWalker.Option.SHOW_HIDDEN_FRAMES
            ))
                .walk(frames -> frames.anyMatch(frame -> {
                    Class<?> declaringClass = frame.getDeclaringClass();
                    return declaringClass.isHidden() && declaringClass.getNestHost() == EventChannelTest.class;
                }));
        }

        @SubscribeEvent(priority = Priority.LOW, alwaysCancelling = true)
        private void always(Pulse event) {
            alwaysCalls++;
        }

        @SubscribeEvent(priority = Priority.MONITOR)
        private void monitor(Pulse event, boolean cancelled) {
            monitorResults.add(cancelled);
        }

    }

    private static final class InvalidBatch {

        @SubscribeEvent
        private void valid(Change event) {
            event.value = 9;
        }

        @SubscribeEvent
        private boolean invalid(Change event) {
            return true;
        }

    }

    private static final class NoArgumentBatch {

        @SubscribeEvent
        private void valid(Change event) {
            event.value = 9;
        }

        @SubscribeEvent
        private void invalid() { }

    }

    private static final class InvalidPriorityBatch {

        @SubscribeEvent
        private void valid(Pulse event) { }

        @SubscribeEvent(priority = Priority.MONITOR)
        private boolean invalid(Pulse event) {
            return true;
        }

    }

    private static final class InvalidMonitorBatch {

        @SubscribeEvent
        private void valid(Pulse event) { }

        @SubscribeEvent(priority = Priority.MONITOR)
        private void invalid(Pulse event, boolean cancelled) { }

    }

    static final class InaccessibleBatch {

        @SubscribeEvent
        void valid(Change event) {
            event.value = 9;
        }

        @SubscribeEvent
        private void invalid(Change event) { }

    }

    static final class FallbackListeners {

        private final List<Boolean> monitorResults = new ArrayList<>();

        @SubscribeEvent
        static boolean pulse(Pulse event) {
            return event.value() == 3;
        }

        @SubscribeEvent
        void change(Change event) {
            event.value = 17;
        }

        @SubscribeEvent(priority = Priority.MONITOR)
        void monitor(Pulse event, boolean cancelled) {
            monitorResults.add(cancelled);
        }

    }

    public static final class IsolatedListener {

        @SubscribeEvent
        private static boolean pulse(Pulse event) {
            return event.value() == 4;
        }

    }

    record ThrowingListener(ModContainer owner, ModContainer changedOwner, IOException failure) {

        ThrowingListener(ModContainer owner, ModContainer changedOwner) {
            this(owner, changedOwner, new IOException("channel listener failure"));
        }

        @SubscribeEvent
        void fail(Change event) throws IOException {
            assertThat(ModEventContext.owner()).isSameAs(owner);
            ModEventContext.setOwner(changedOwner);
            throw failure;
        }

    }

    private static final class LegacyStubEvent extends net.minecraftforge.fml.common.eventhandler.Event { }

    private static final class MultiEventAnnotatedListeners {

        private static int staticCalls;

        private final List<Boolean> monitorResults = new ArrayList<>();

        @SubscribeEvent(priority = Priority.HIGH)
        private static void staticPulse(Pulse event) {
            staticCalls++;
        }

        @SubscribeEvent
        private void change(Change event) {
            event.value = 21;
        }

        @SubscribeEvent
        private boolean cancelPulse(Pulse event) {
            return event.value() > 0;
        }

        @SubscribeEvent(priority = Priority.MONITOR)
        private void monitor(Pulse event, boolean cancelled) {
            monitorResults.add(cancelled);
        }

        @SubscribeEvent
        private void ignoredLegacy(LegacyStubEvent event) { }

    }

    private static final class InvalidWholeClassBatch {

        @SubscribeEvent
        private void valid(Change event) {
            event.value = 1;
        }

        @SubscribeEvent
        private boolean invalid(Change event) {
            return true;
        }

    }

}

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

import com.cleanroommc.cleanroom.event.LegacyEventChannel;
import com.cleanroommc.cleanroom.event.Subscription;
import com.cleanroommc.cleanroom.event.SubscriptionGroup;
import com.cleanroommc.common.CleanroomContainer;

import org.junit.jupiter.api.Test;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacyEventChannelTest {

    private static SubscriptionGroup subscriptions() {
        return new SubscriptionGroup(CleanroomContainer.instance());
    }

    private static void closeMethod(List<Subscription> subscriptions, String method) {
        subscriptions.stream()
            .filter(subscription -> subscription.source().contains('.' + method + '('))
            .findFirst()
            .orElseThrow()
            .close();
    }

    @Test
    void defaultChannelSeesRawAndManagedListenersAndFiresTheSameEvent() {
        var channel = CounterEvent.BUS;
        var rawListener = new RawCounterListener();
        assertThat(channel.raw()).isSameAs(MinecraftForge.EVENT_BUS);
        assertThat(channel.hasListeners()).isFalse();

        channel.raw().register(rawListener);
        try {
            assertThat(channel.hasListeners()).isTrue();
            try (var subscriptions = subscriptions()) {
                channel.subscribe(subscriptions, event -> event.value *= 2);
                channel.subscribe(subscriptions, Priority.HIGH, event -> event.value += 2);
                var event = new CounterEvent(1);
                assertThat(channel.fire(event)).isSameAs(event);
                assertThat(event.value).isEqualTo(16);
            }
            assertThat(channel.fire(new CounterEvent(1)).value).isEqualTo(6);
        } finally {
            channel.raw().unregister(rawListener);
        }
        assertThat(channel.hasListeners()).isFalse();
    }

    @Test
    void forgePrioritiesMapOntoLegacyBucketsAndMonitorsAreRejected() {
        var channel = LegacyEventChannel.of(new EventBus(), CounterEvent.class);
        var trace = new ArrayList<String>();
        try (var subscriptions = subscriptions()) {
            channel.subscribe(subscriptions, Priority.LOWEST, event -> trace.add("lowest"));
            channel.subscribe(subscriptions, Priority.LOW, event -> trace.add("low"));
            channel.subscribe(subscriptions, Priority.NORMAL, event -> trace.add("normal"));
            channel.subscribe(subscriptions, Priority.HIGH, event -> trace.add("high"));
            channel.subscribe(subscriptions, Priority.HIGHEST, event -> trace.add("highest"));

            assertThatThrownBy(() -> channel.subscribe(subscriptions, Priority.MONITOR, event -> { }))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monitor");

            channel.fire(new CounterEvent(0));
            assertThat(trace).containsExactly("highest", "high", "normal", "low", "lowest");
        }
    }

    @Test
    void channelAnnotationRegistrationSelectsItsEventAndReturnsIndependentHandles() {
        var bus = new EventBus();
        var channel = LegacyEventChannel.of(bus, CounterEvent.class);
        var root = LegacyEventChannel.of(bus, Event.class);
        var lookup = MethodHandles.lookup();
        var listener = new CounterListeners();
        try (var subscriptions = subscriptions()) {
            var tokens = channel.subscribe(subscriptions, lookup, listener);
            assertThat(tokens).hasSize(2);
            assertThat(root.hasListeners()).isFalse();
            assertThat(root.hasListeners(new CounterEvent(0))).isTrue();

            root.fire(new Event());
            root.fire(new CounterEvent(0));
            assertThat(listener.trace).containsExactly("first", "last");

            closeMethod(tokens, "first");
            channel.fire(new CounterEvent(0));
            assertThat(listener.trace).containsExactly("first", "last", "last");
        }
        assertThat(channel.hasListeners()).isFalse();
        try (var subscriptions = subscriptions()) {
            assertThatThrownBy(() -> channel.subscribe(subscriptions, lookup, new InvalidCounterListeners()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("requires 2 arguments");
            assertThat(channel.hasListeners()).isFalse();
        }
    }

    @Test
    void annotatedLegacyChannelPreservesItsGenericSelector() {
        var channel = LegacyEventChannel.of(new EventBus(), Payload.class);
        var stringPayloads = channel.withGenericType(String.class);
        var listener = new PayloadListeners();
        var lookup = MethodHandles.lookup();
        try (var subscriptions = subscriptions()) {
            stringPayloads.subscribe(subscriptions, lookup, listener);
            channel.post(new Payload<>(String.class));
            channel.post(new Payload<>(Integer.class));
            assertThat(listener.typedCalls).isOne();
            assertThat(listener.rawCalls).isOne();

            var integers = channel.withGenericType(Integer.class);
            assertThatThrownBy(() -> integers.subscribe(subscriptions, lookup, listener))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("channel selector");
            channel.post(new Payload<>(Integer.class));
            assertThat(listener.rawCalls).isOne();
        }
    }

    @Test
    void subscribeCancellingAndDefaultReceiveCanceledMapPrioritiesAndRejectInvalidEvents() {
        var bus = new EventBus();
        var cancelable = LegacyEventChannel.of(bus, CancelableLegacyEvent.class);
        var plain = LegacyEventChannel.of(bus, CounterEvent.class);
        var defaultOwner = CleanroomContainer.instance();
        ModContainer previous = ModEventContext.swapOwner(defaultOwner);
        var trace = new ArrayList<String>();
        try {
            var cancelSub = cancelable.subscribeCancelling(Priority.HIGH, event -> {
                trace.add("cancel");
                return event.shouldCancel;
            });
            var skippedSub = cancelable.subscribe(event -> trace.add("normal"));
            var uncancelSub = cancelable.subscribe(Priority.LOW, true, event -> {
                trace.add("receive=" + event.isCanceled());
                if (event.clearCancel) {
                    event.setCanceled(false);
                }
            });
            var lowestSub = cancelable.subscribe(Priority.LOWEST, "after-uncancel", event -> trace.add("lowest"));
            try {
                assertThat(cancelable.post(new CancelableLegacyEvent(true, false))).isTrue();
                assertThat(trace).containsExactly("cancel", "receive=true");
                trace.clear();

                assertThat(cancelable.post(new CancelableLegacyEvent(true, true))).isFalse();
                assertThat(trace).containsExactly("cancel", "receive=true", "lowest");
                trace.clear();

                assertThatThrownBy(() -> cancelable.subscribeCancelling(Priority.MONITOR, event -> true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("monitor");
                assertThatThrownBy(() -> plain.subscribeCancelling(event -> true))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("not cancelable");
            } finally {
                cancelSub.close();
                skippedSub.close();
                uncancelSub.close();
                lowestSub.close();
            }
        } finally {
            SubscriptionGroup.closeDefaultGroup(defaultOwner);
            ModEventContext.setOwner(previous);
        }
    }

    @Test
    void forge7AnnotationsWorkOnLegacyChannelsAndWholeClassLegacyRegister() {
        var bus = new EventBus();
        var lookup = MethodHandles.lookup();
        var handler = new Forge7LegacySubscribers();
        var cancelable = LegacyEventChannel.of(bus, CancelableLegacyEvent.class);
        var counter = LegacyEventChannel.of(bus, CounterEvent.class);
        try (var group = subscriptions()) {
            var singleTokens = cancelable.subscribe(group, lookup, handler);
            assertThat(singleTokens).hasSize(2);
            assertThat(cancelable.post(new CancelableLegacyEvent(true, false))).isTrue();
            assertThat(handler.trace).containsExactly("high-predicate");
            handler.trace.clear();
            assertThat(cancelable.post(new CancelableLegacyEvent(false, false))).isTrue();
            assertThat(handler.trace).containsExactly("high-predicate", "low-always");
            singleTokens.forEach(Subscription::close);
            handler.trace.clear();

            var wholeTokens = LegacyEventChannel.register(group, bus, lookup, handler);
            assertThat(wholeTokens).hasSize(3);
            var c = new CounterEvent(1);
            assertThat(counter.fire(c).value).isEqualTo(11);
            assertThat(cancelable.post(new CancelableLegacyEvent(false, false))).isTrue();
            assertThat(handler.trace).containsExactly("counter", "high-predicate", "low-always");

            var predicateBus = new EventBus();
            var predicateChannel = LegacyEventChannel.of(predicateBus, CancelableLegacyEvent.class);
            predicateChannel.subscribe(group, lookup, new Forge7PredicateOnlyLegacy());
            assertThat(predicateChannel.post(new CancelableLegacyEvent(false, false))).isFalse();
            assertThat(predicateChannel.post(new CancelableLegacyEvent(true, false))).isTrue();

            assertThatThrownBy(() -> counter.subscribe(group, lookup, new MonitorForge7Legacy()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("monitor");
            assertThatThrownBy(() -> counter.subscribe(group, lookup, new NonCancelableBooleanLegacy()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not cancelable");
            assertThatThrownBy(() -> counter.subscribe(group, lookup, new DualAnnotatedLegacy()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("both legacy and Forge 7");
            assertThatThrownBy(() -> LegacyEventChannel.register(group, bus, lookup, new BadArgLegacy()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Forge 7 event");

            var genericBus = new EventBus();
            var genericHandler = new Forge7GenericPayloadListener();
            LegacyEventChannel.of(genericBus, Payload.class)
                .withGenericType(String.class)
                .subscribe(group, lookup, genericHandler);
            genericBus.post(new Payload<>(String.class));
            genericBus.post(new Payload<>(Integer.class));
            assertThat(genericHandler.calls).isOne();
        }
    }

    private static final class CounterEvent extends Event {

        private static final LegacyEventChannel<CounterEvent> BUS = LegacyEventChannel.of(CounterEvent.class);

        private int value;

        private CounterEvent(int value) {
            this.value = value;
        }

    }

    private static final class RawCounterListener {

        @SubscribeEvent
        public void receive(CounterEvent event) {
            event.value += 5;
        }

    }

    private static class ParentCounterListeners {

        final List<String> trace = new ArrayList<>();

        @SubscribeEvent(priority = EventPriority.HIGH)
        protected void first(CounterEvent event) {
            trace.add("first");
        }

    }

    private static final class CounterListeners extends ParentCounterListeners {

        @SubscribeEvent(priority = EventPriority.LOW)
        private void last(CounterEvent event) {
            trace.add("last");
        }

        @SubscribeEvent
        private void unrelated(Event event) {
            trace.add("unrelated");
        }

    }

    private static final class InvalidCounterListeners {

        @SubscribeEvent
        private void valid(CounterEvent event) {
            event.value++;
        }

        @SubscribeEvent
        private void invalid(CounterEvent event, boolean extra) {
            event.value++;
        }

    }

    private static final class Payload<T> extends GenericEvent<T> {

        private Payload(Class<T> type) {
            super(type);
        }

    }

    private static final class PayloadListeners {

        private int typedCalls;
        private int rawCalls;

        @SubscribeEvent
        private void typed(Payload<String> event) {
            typedCalls++;
        }

        @SubscribeEvent
        @SuppressWarnings("rawtypes")
        private void raw(Payload event) {
            rawCalls++;
        }

    }

    @Cancelable
    private static final class CancelableLegacyEvent extends Event {

        private final boolean shouldCancel;
        private final boolean clearCancel;

        private CancelableLegacyEvent(boolean shouldCancel, boolean clearCancel) {
            this.shouldCancel = shouldCancel;
            this.clearCancel = clearCancel;
        }

    }

    private static final class Forge7LegacySubscribers {

        private final List<String> trace = new ArrayList<>();

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent(priority = Priority.HIGH)
        private boolean highPredicate(CancelableLegacyEvent event) {
            trace.add("high-predicate");
            return event.shouldCancel;
        }

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent(priority = Priority.LOW, alwaysCancelling = true)
        private void lowAlways(CancelableLegacyEvent event) {
            trace.add("low-always");
        }

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent(priority = Priority.NORMAL)
        private void counter(CounterEvent event) {
            trace.add("counter");
            event.value += 10;
        }

    }

    private static final class MonitorForge7Legacy {

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent(priority = Priority.MONITOR)
        private void bad(CounterEvent event, boolean canceled) { }

    }

    private static final class NonCancelableBooleanLegacy {

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private boolean bad(CounterEvent event) {
            return true;
        }

    }

    private static final class DualAnnotatedLegacy {

        @SubscribeEvent
        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private void bad(CounterEvent event) { }

    }

    private static final class BadArgLegacy {

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private void bad(String notAnEvent) { }

    }

    private static final class Forge7GenericPayloadListener {

        private int calls;

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private void stringOnly(Payload<String> event) {
            calls++;
        }

    }

    private static final class Forge7PredicateOnlyLegacy {

        @net.minecraftforge.eventbus.api.listener.SubscribeEvent
        private boolean check(CancelableLegacyEvent event) {
            return event.shouldCancel;
        }

    }

}

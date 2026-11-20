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

import com.cleanroommc.cleanroom.event.NativeEventChannel;
import com.cleanroommc.cleanroom.event.SubscriptionGroup;

import org.junit.jupiter.api.Test;

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ManagedNativeEventTest {

    private static final NativeEventChannel<Pulse> PULSES = NativeEventChannel.record(BusGroup.DEFAULT, Pulse.class);
    private static final NativeEventChannel<Change> CHANGES = NativeEventChannel.mutable(
        BusGroup.DEFAULT, Change.class
    );

    private static ModContainer owner(String id) {
        var metadata = new ModMetadata();
        metadata.modId = id;
        metadata.name = id;
        return new DummyModContainer(metadata);
    }

    @Test
    void closingOneRegistrationKeepsAnotherRegistrationOfTheSameCallback() {
        var firstOwner = owner("native-first");
        var secondOwner = owner("native-second");
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
    void nativeCancellationAndMonitoringUseUpstreamSemantics() {
        var trace = new ArrayList<String>();
        try (var group = new SubscriptionGroup(owner("native-cancellation"))) {
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
    void annotatedBatchUsesExplicitLookupAndValidatesBeforeInstalling() {
        var owner = owner("native-bulk");
        var actual = new NativeListeners();
        try (var group = new SubscriptionGroup(owner)) {
            var lookup = MethodHandles.lookup();
            assertThatThrownBy(() -> group.registerNative(BusGroup.DEFAULT, lookup, new InvalidBatch())).isInstanceOf(
                IllegalArgumentException.class
            );
            var first = new Change();
            CHANGES.post(first);
            assertThat(first.value).isZero();

            var tokens = group.registerNative(BusGroup.DEFAULT, lookup, actual);
            CHANGES.post(first);
            assertThat(first.value).isEqualTo(13);
            assertThat(actual.seenOwner).isSameAs(owner);
            assertThat(PULSES.post(new Pulse(2))).isTrue();
            tokens.forEach(token -> token.close());
            CHANGES.post(first);
            assertThat(first.value).isEqualTo(13);
            assertThat(PULSES.post(new Pulse(2))).isFalse();
        }
    }

    @Test
    void sameTypeInDifferentNativeScopesRemainsIsolated() {
        BusGroup isolated = BusGroup.create("cleanroom-test-event-scope");
        try (var group = new SubscriptionGroup(owner("native-isolation"))) {
            var channel = NativeEventChannel.record(isolated, Pulse.class);
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

    private record Pulse(int value) implements RecordEvent, Cancellable { }

    private static final class Change extends MutableEvent {

        private int value;

    }

    private static final class NativeListeners {

        private ModContainer seenOwner;

        @SubscribeEvent
        private static boolean pulse(Pulse event) {
            return event.value() > 0;
        }

        @SubscribeEvent
        private void change(Change event) {
            event.value = 13;
            seenOwner = Loader.instance().activeModContainer();
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

}

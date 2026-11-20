/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.event.benchmark;

import com.cleanroommc.cleanroom.event.EventChannel;
import com.cleanroommc.cleanroom.event.SubscriptionGroup;
import com.cleanroommc.common.CleanroomContainer;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class EventChannelBenchmark {

    @Benchmark
    public int post(Posts state) {
        return state.postFresh.getAsInt();
    }

    @Benchmark
    public int postReused(Posts state) {
        return state.postReused.getAsInt();
    }

    public record Signal(int value) implements RecordEvent { }

    private record AnnotatedListener(int[] sum) {

        @SubscribeEvent
        private void record(Signal event) {
            sum[0] += event.value();
        }

    }

    @State(Scope.Thread)
    public static class Posts {

        private final int[] sum = { 0 };
        private final List<SubscriptionGroup> subscriptions = new ArrayList<>();

        @Param({ "0", "1", "4", "16", "64" }) public int listeners;

        @Param(
            {
                "upstream",
                "ownerWrapper",
                "ownerWrapperSameOwner",
                "ownerWrapperDistinctOwners",
                "annotatedOwnerWrapper"
            }
        ) public String backend;
        private BusGroup group;
        private ModContainer previousOwner;
        private boolean restoreOwner;
        private IntSupplier postFresh;
        private IntSupplier postReused;

        @Setup
        public void setup() {
            group = BusGroup.create("eventbus-benchmark");
            var channel = EventChannel.record(group, Signal.class);
            Predicate<Signal> post;
            switch (backend) {
                case "upstream" -> {
                    var bus = EventBus.create(channel.scope(), channel.eventType());
                    for (int i = 0; i < listeners; i++) {
                        bus.addListener(Priority.NORMAL, this::record);
                    }
                    post = bus::post;
                }
                case "ownerWrapper", "ownerWrapperSameOwner", "ownerWrapperDistinctOwners", "annotatedOwnerWrapper" -> {
                    var owner = CleanroomContainer.instance();
                    boolean distinct = backend.equals("ownerWrapperDistinctOwners");
                    if (distinct || backend.equals("ownerWrapperSameOwner")) {
                        previousOwner = ModEventContext.swapOwner(owner);
                        restoreOwner = true;
                    }
                    if (distinct) {
                        for (int i = 0; i < listeners; i++) {
                            var metadata = new ModMetadata();
                            metadata.modId = "benchmark_listener_" + i;
                            metadata.name = metadata.modId;
                            var subscription = new SubscriptionGroup(new DummyModContainer(metadata));
                            subscriptions.add(subscription);
                            channel.subscribe(subscription, Priority.NORMAL, "benchmark", this::record);
                        }
                    } else {
                        var subscription = new SubscriptionGroup(owner);
                        subscriptions.add(subscription);
                        if (backend.equals("annotatedOwnerWrapper")) {
                            var lookup = MethodHandles.lookup();
                            var listener = new AnnotatedListener(sum);
                            for (int i = 0; i < listeners; i++) {
                                channel.subscribe(subscription, lookup, listener);
                            }
                        } else {
                            for (int i = 0; i < listeners; i++) {
                                channel.subscribe(subscription, Priority.NORMAL, "benchmark", this::record);
                            }
                        }
                    }
                    post = channel::post;
                }
                default -> throw new IllegalArgumentException(backend);
            }
            Signal reused = new Signal(1);
            postFresh = () -> post.test(new Signal(1)) ? ~sum[0] : sum[0];
            postReused = () -> post.test(reused) ? ~sum[0] : sum[0];
            if (postFresh.getAsInt() != listeners || postReused.getAsInt() != 2 * listeners) {
                throw new IllegalStateException("Unexpected callback result for " + backend + " at count " + listeners);
            }
        }

        @TearDown
        public void cleanup() {
            try {
                subscriptions.forEach(SubscriptionGroup::close);
            } finally {
                try {
                    group.dispose();
                } finally {
                    if (restoreOwner) {
                        ModEventContext.setOwner(previousOwner);
                    }
                }
            }
        }

        private void record(Signal event) {
            sum[0] += event.value();
        }

    }

}

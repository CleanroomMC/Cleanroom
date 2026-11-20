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

import com.cleanroommc.cleanroom.event.NativeEventChannel;
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

import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class NativeEventBusBenchmark {

    @Benchmark
    public int post(Posts state) {
        return state.postFresh.getAsInt();
    }

    @Benchmark
    public int postReused(Posts state) {
        return state.postReused.getAsInt();
    }

    public record Signal(int value) implements RecordEvent { }

    @State(Scope.Thread)
    public static class Posts {

        private final int[] sum = { 0 };

        @Param({ "0", "1", "4", "16", "64" }) public int listeners;

        @Param({ "upstream", "ownerWrapper" }) public String backend;
        private BusGroup group;
        private SubscriptionGroup subscriptions;
        private IntSupplier postFresh;
        private IntSupplier postReused;

        @Setup
        public void setup() {
            group = BusGroup.create("eventbus-benchmark");
            Predicate<Signal> post;
            switch (backend) {
                case "upstream" -> {
                    EventBus<Signal> bus = EventBus.create(group, Signal.class);
                    for (int i = 0; i < listeners; i++) {
                        bus.addListener(Priority.NORMAL, this::record);
                    }
                    post = bus::post;
                }
                case "ownerWrapper" -> {
                    subscriptions = new SubscriptionGroup(CleanroomContainer.instance());
                    NativeEventChannel<Signal> channel = NativeEventChannel.record(group, Signal.class);
                    for (int i = 0; i < listeners; i++) {
                        channel.subscribe(subscriptions, Priority.NORMAL, "benchmark", this::record);
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
                if (subscriptions != null) {
                    subscriptions.close();
                }
            } finally {
                group.dispose();
            }
        }

        private void record(Signal event) {
            sum[0] += event.value();
        }

    }

}

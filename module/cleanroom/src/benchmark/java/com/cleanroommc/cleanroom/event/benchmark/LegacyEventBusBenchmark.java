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

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.util.concurrent.TimeUnit;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
public class LegacyEventBusBenchmark {

    private static final String WORKLOAD = "com.cleanroommc.cleanroom.event.benchmark.LegacyEventBusWorkload";
    private static final String TRANSFORMER = "net.minecraftforge.fml.common.asm.transformers.EventSubscriptionTransformer";
    private static final int REGISTRATION_BATCH = 16;

    private static <T> T workload(Class<T> result, String factory, String scenario, int count) throws Exception {
        Class<?> type = Class.forName(WORKLOAD, true, RuntimeLoader.LOADER);
        if (type.getClassLoader() != RuntimeLoader.LOADER) {
            throw new IllegalStateException("Legacy workload must be defined by the Foundation launch loader");
        }
        return result.cast(type.getMethod(factory, String.class, int.class).invoke(null, scenario, count));
    }

    @Benchmark
    public int post(Posts state) {
        return state.post.getAsInt();
    }

    @Benchmark
    public int postScenario(Scenarios state) {
        return state.post.getAsInt();
    }

    @Benchmark
    public int postEmptyReused(Empty state) {
        return state.post.getAsInt();
    }

    @Benchmark
    public Object constructEvent(Construction state) {
        return state.construct.get();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @OperationsPerInvocation(REGISTRATION_BATCH)
    public void registration(Registration state) {
        state.measure.run();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public void coldRegistration(ColdRegistration state) {
        state.register.run();
    }

    @Benchmark
    @BenchmarkMode(Mode.SingleShotTime)
    @Warmup(iterations = 0)
    @Measurement(iterations = 1)
    public Object coldLoadAndConstruct(ColdEventType state) throws Exception {
        return Class.forName(WORKLOAD + "$ColdEvent", true, state.loader).getConstructor().newInstance();
    }

    // JMH and the runtime workload share only JDK interfaces across the loader boundary.
    private static final class RuntimeLoader {

        private static final LaunchClassLoader LOADER = create();

        private static LaunchClassLoader create() {
            BenchmarkLaunchLoader loader = new BenchmarkLaunchLoader();
            boolean baseline = LegacyEventBusBenchmark.class
                    .getClassLoader()
                    .getResource(TRANSFORMER.replace('.', '/') + ".class") !=
                null;
            String expected = System.getProperty("cleanroom.eventbusBenchmark.baseline");
            if (expected != null && Boolean.parseBoolean(expected) != baseline) {
                throw new IllegalStateException("Unexpected legacy transformer availability in this worktree");
            }
            try {
                if (baseline) {
                    loader.transformer = (IClassTransformer) loader.loadClass(TRANSFORMER)
                        .getConstructor()
                        .newInstance();
                }
                Class<?> fixture = loader.loadClass(WORKLOAD + "$PlainEvent");
                if (fixture.getSuperclass().getClassLoader() != loader) {
                    throw new IllegalStateException("Legacy Event is not defined by the benchmark launch loader");
                }
                if ((fixture.getMethod("getListenerList").getDeclaringClass() == fixture) != baseline) {
                    throw new IllegalStateException("Unexpected event transformation for " + fixture.getName());
                }
                return loader;
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Could not initialize the benchmark launch loader", e);
            }
        }

    }

    private static final class BenchmarkLaunchLoader extends LaunchClassLoader {

        private IClassTransformer transformer;

        private BenchmarkLaunchLoader() {
            super(LegacyEventBusBenchmark.class.getClassLoader());
        }

        @Override
        public byte[] runTransformers(String name, String transformedName, byte[] bytes) {
            if (transformer == null || !name.startsWith(WORKLOAD)) {
                return bytes;
            }
            return transformer.transform(name, transformedName, bytes);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals(IClassTransformer.class.getName())) {
                return IClassTransformer.class;
            }
            if (name.startsWith("net.minecraftforge.")) {
                return findClass(name);
            }
            return super.loadClass(name, resolve);
        }

    }

    @State(Scope.Thread)
    public static class Posts {

        @Param({ "0", "1", "4", "16", "64" }) public int listeners;
        private IntSupplier post;

        @Setup
        public void setup() throws Exception {
            post = workload(IntSupplier.class, "posts", "direct", listeners);
        }

    }

    @State(Scope.Thread)
    public static class Scenarios {

        @Param({ "inherited", "cancelAndRestore", "generic", "owner" }) public String scenario;
        private IntSupplier post;

        @Setup
        public void setup() throws Exception {
            post = workload(IntSupplier.class, "posts", scenario, 4);
        }

    }

    @State(Scope.Thread)
    public static class Empty {

        private IntSupplier post;

        @Setup
        public void setup() throws Exception {
            post = workload(IntSupplier.class, "posts", "reuseEmpty", 0);
        }

    }

    @State(Scope.Thread)
    public static class Construction {

        @Param({ "plain", "mixed" }) public String eventTypes;
        private Supplier<?> construct;

        @Setup
        public void setup() throws Exception {
            construct = workload(Supplier.class, "construction", eventTypes, 0);
        }

    }

    @State(Scope.Thread)
    public static class Registration {

        @Param({ "register", "unregister", "roundTrip" }) public String operation;
        private Runnable measure;
        private Runnable cleanup;
        private Runnable prepare;

        @Setup
        public void setup() throws Exception {
            Runnable[] callbacks = workload(Runnable[].class, "registration", operation, REGISTRATION_BATCH);
            prepare = callbacks[0];
            measure = callbacks[1];
            cleanup = callbacks[2];
        }

        @Setup(Level.Iteration)
        public void prepareBatch() {
            prepare.run();
        }

        // For the isolated timings, GC profiling includes the corresponding untimed setup or teardown.
        // The round trip measures both operations in a single timed interval.

        @TearDown(Level.Iteration)
        public void cleanupBatch() {
            cleanup.run();
        }

    }

    @State(Scope.Thread)
    public static class ColdRegistration {

        private Runnable register;

        @Setup
        public void setup() throws Exception {
            register = workload(Runnable.class, "coldRegistration", "", 0);
        }

    }

    @State(Scope.Thread)
    public static class ColdEventType {

        private ClassLoader loader;

        @Setup
        public void setup() throws Exception {
            loader = RuntimeLoader.LOADER;
            // Establish the shared legacy infrastructure before timing a distinct, previously unseen type.
            workload(Supplier.class, "construction", "", 0).get();
            if (RuntimeLoader.LOADER.isClassLoaded(WORKLOAD + "$ColdEvent")) {
                throw new IllegalStateException("Cold event fixture was already loaded");
            }
        }

    }

}

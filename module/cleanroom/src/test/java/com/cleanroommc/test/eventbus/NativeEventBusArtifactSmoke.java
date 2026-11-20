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

import org.jspecify.annotations.NullMarked;

import net.minecraft.launchwrapper.Launch;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.EventListener;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public final class NativeEventBusArtifactSmoke {

    private NativeEventBusArtifactSmoke() { }

    public static void main(String[] args) throws Throwable {
        verify();
        System.out.println("Forge EventBus 7.0.6 Foundation smoke passed");
    }

    static void verify() throws Throwable {
        var previousLaunchLoader = Launch.classLoader;
        var thread = Thread.currentThread();
        var previousContextLoader = thread.getContextClassLoader();
        try (var loader = new LaunchClassLoader(ClassLoader.getSystemClassLoader())) {
            // Gradle's test worker loads the test classpath separately from java.class.path.
            loader.addURL(location(NativeEventBusArtifactSmoke.class));
            loader.addURL(location(EventBus.class));
            loader.addURL(location(NullMarked.class));
            thread.setContextClassLoader(loader);
            var scenario = Class.forName(Scenario.class.getName(), true, loader);
            require(scenario.getClassLoader() == loader, "Foundation must define the smoke fixture");
            scenario.getMethod("run").invoke(null);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            thread.setContextClassLoader(previousContextLoader);
            Launch.classLoader = previousLaunchLoader;
        }
    }

    private static URL location(Class<?> type) {
        return type.getProtectionDomain().getCodeSource().getLocation();
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static final class Scenario {

        private static final BusGroup GROUP = BusGroup.create("cleanroom-artifact-smoke");
        private static final CancellableEventBus<SampleRecord> RECORD_BUS = CancellableEventBus.create(
            GROUP, SampleRecord.class
        );
        private static final EventBus<SampleMutable> MUTABLE_BUS = EventBus.create(GROUP, SampleMutable.class);

        private Scenario() { }

        public static void run() {
            var loader = Scenario.class.getClassLoader();
            require(loader == Launch.classLoader, "The fixture must run under the Launch classloader");
            require(EventBus.class.getClassLoader() == loader, "Foundation must define the upstream EventBus API");
            require(
                SampleRecord.class.getClassLoader() == loader && SampleMutable.class.getClassLoader() == loader,
                "Foundation must define both native event types"
            );
            require(
                "7.0.6".equals(EventBus.class.getPackage().getImplementationVersion()),
                "The upstream EventBus JAR must report implementation version 7.0.6"
            );
            try {
                verifyRecord();
                verifyMutableAndBulk();
            } finally {
                GROUP.dispose();
            }
        }

        private static void verifyRecord() {
            var seen = new AtomicInteger();
            var canceller = RECORD_BUS.addListener(Priority.HIGH, event -> event.value() > 0);
            Consumer<SampleRecord> recordValue = event -> seen.set(event.value());
            var observer = RECORD_BUS.addListener(Priority.NORMAL, recordValue);
            var event = new SampleRecord(7);
            require(
                RECORD_BUS.post(event) && seen.get() == 0,
                "Record cancellation must skip the subsequent ordinary listener"
            );
            RECORD_BUS.removeListener(canceller);
            RECORD_BUS.removeListener(canceller);
            require(
                !RECORD_BUS.post(event) && seen.get() == 7,
                "Removing the cancellation token must leave the other record listener active"
            );
            RECORD_BUS.removeListener(observer);
            require(!RECORD_BUS.hasListeners(), "Removing the second token must empty the record bus");
        }

        private static void verifyMutableAndBulk() {
            var mutation = MUTABLE_BUS.addListener(event -> event.value += 2);
            var bulk = new BulkListeners();
            var tokens = bulk.register();
            require(tokens.size() == 2, "Bulk registration must return both listener tokens");

            var mutable = new SampleMutable();
            require(
                MUTABLE_BUS.fire(mutable) == mutable && mutable.value == 5,
                "Mutable fire must return the original event with both listeners' mutations"
            );
            require(
                !RECORD_BUS.post(new SampleRecord(3)) && bulk.recordValue == 3,
                "Bulk registration must also post to the record subscriber"
            );

            GROUP.unregister(tokens);
            GROUP.unregister(tokens);
            var afterBulkRemoval = new SampleMutable();
            require(
                !MUTABLE_BUS.post(afterBulkRemoval) && afterBulkRemoval.value == 2,
                "Removing the bulk tokens must leave the separately registered mutable listener active"
            );
            RECORD_BUS.post(new SampleRecord(9));
            require(bulk.recordValue == 3, "Removing the bulk tokens must remove the record subscriber");

            MUTABLE_BUS.removeListener(mutation);
            var afterRemoval = new SampleMutable();
            require(
                MUTABLE_BUS.fire(afterRemoval) == afterRemoval && afterRemoval.value == 0,
                "Removing the mutable token must stop its mutation"
            );
        }

        private record SampleRecord(int value) implements RecordEvent, Cancellable { }

        private static final class SampleMutable extends MutableEvent {

            private int value;

        }

        private static final class BulkListeners {

            private int recordValue;

            Collection<EventListener> register() {
                return GROUP.register(MethodHandles.lookup(), this);
            }

            @SubscribeEvent
            private void onRecord(SampleRecord event) {
                recordValue = event.value();
            }

            @SubscribeEvent
            private void onMutable(SampleMutable event) {
                event.value += 3;
            }

        }

    }

}

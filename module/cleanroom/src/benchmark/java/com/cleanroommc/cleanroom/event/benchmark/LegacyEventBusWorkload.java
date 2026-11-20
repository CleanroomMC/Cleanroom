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

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.IContextSetter;
import net.minecraftforge.fml.common.eventhandler.IGenericEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Type;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/** The actual legacy bus, events and subscribers are defined by the same launch loader. */
public final class LegacyEventBusWorkload {

    private LegacyEventBusWorkload() { }

    public static IntSupplier posts(String scenario, int count) throws ReflectiveOperationException {
        EventBus bus = new EventBus();
        // One shared, observable accumulation per invocation. The holder stays local to the JMH thread.
        int[] sum = { 0 };
        IntSupplier post;
        int expected = count;
        switch (scenario) {
            case "direct", "reuseEmpty" -> {
                for (int i = 0; i < count; i++) {
                    bus.register(new PlainListener(sum, i));
                }
                if (scenario.equals("reuseEmpty")) {
                    PlainEvent event = new PlainEvent();
                    post = () -> post(bus, event, sum);
                } else {
                    post = () -> post(bus, new PlainEvent(), sum);
                }
            }
            case "inherited" -> {
                for (int i = 0; i < count / 2; i++) {
                    bus.register(new PlainListener(sum, i));
                    bus.register(new InheritedListener(sum, i));
                }
                post = () -> post(bus, new InheritedEvent(), sum);
            }
            case "cancelAndRestore" -> {
                bus.register(new CancellationListeners(sum));
                expected = 3;
                post = () -> post(bus, new CancelEvent(), sum);
            }
            case "generic" -> {
                for (int i = 0; i < count / 2; i++) {
                    bus.register(new GenericListeners(sum, i));
                }
                expected = count / 2;
                post = () -> post(bus, new TypedEvent<String>(), sum);
            }
            case "owner" -> {
                Loader loader = Loader.instance();
                var controller = Loader.class.getDeclaredField("modController");
                controller.setAccessible(true);
                if (controller.get(loader) == null) {
                    controller.set(loader, new LoadController(loader));
                }
                ModContainer owner = owner("benchmark_listener");
                ModContainer caller = owner("benchmark_poster");
                loader.setActiveModContainer(owner);
                for (int i = 0; i < count; i++) {
                    bus.register(new OwnedListener(sum, i));
                }
                loader.setActiveModContainer(caller);
                OwnedEvent probe = new OwnedEvent();
                if (post(bus, probe, sum) != count || probe.owner != owner || loader.activeModContainer() != caller) {
                    throw new IllegalStateException("Legacy owner context did not switch and restore");
                }
                post = () -> post(bus, new OwnedEvent(), sum);
            }
            default -> throw new IllegalArgumentException(scenario);
        }
        int before = sum[0];
        if (post.getAsInt() - before != expected) {
            throw new IllegalStateException("Unexpected callback result for " + scenario + " at count " + count);
        }
        return post;
    }

    public static Supplier<?> construction(String scenario, int count) {
        return PlainEvent::new;
    }

    public static Runnable coldRegistration(String scenario, int count) {
        EventBus bus = new EventBus();
        Loader.instance();
        RegistrationListener listener = new RegistrationListener(new int[1], 0);
        return () -> bus.register(listener);
    }

    public static Runnable[] registration(String operation, int count) {
        EventBus bus = new EventBus();
        int[] sum = { 0 };
        RegistrationListener[] listeners = new RegistrationListener[count];
        for (int i = 0; i < count; i++) {
            listeners[i] = new RegistrationListener(sum, i);
        }
        bus.register(listeners[0]);
        if (post(bus, new RegistrationEvent(), sum) != 1) {
            throw new IllegalStateException("Registration fixture was not installed");
        }
        bus.unregister(listeners[0]);
        Runnable register = () -> {
            for (RegistrationListener listener : listeners) {
                bus.register(listener);
            }
        };
        Runnable unregister = () -> {
            for (RegistrationListener listener : listeners) {
                bus.unregister(listener);
            }
        };
        Runnable noOperation = () -> { };
        return switch (operation) {
            case "register" -> new Runnable[] { noOperation, register, unregister };
            case "unregister" -> new Runnable[] { register, unregister, noOperation };
            case "roundTrip" ->
                new Runnable[] {
                    noOperation,
                    () -> {
                        register.run();
                        unregister.run();
                    },
                    noOperation
                };
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private static int post(EventBus bus, Event event, int[] sum) {
        // Preserve the cancel return contract as well as the callback work.
        return bus.post(event) ? ~sum[0] : sum[0];
    }

    private static ModContainer owner(String id) {
        ModMetadata metadata = new ModMetadata();
        metadata.modId = id;
        metadata.name = id;
        return new DummyModContainer(metadata);
    }

    public static class PlainEvent extends Event {

        final int value;

        public PlainEvent() {
            value = 1;
        }

    }

    public static final class InheritedEvent extends PlainEvent { }

    @Cancelable
    public static final class CancelEvent extends PlainEvent { }

    public static final class ColdEvent extends Event { }

    public static final class RegistrationEvent extends PlainEvent { }

    public static final class OwnedEvent extends PlainEvent implements IContextSetter {

        private ModContainer owner;

        @Override
        public void setModContainer(ModContainer owner) {
            this.owner = owner;
        }

    }

    public static final class TypedEvent<T> extends PlainEvent implements IGenericEvent<T> {

        @Override
        public Type getGenericType() {
            return String.class;
        }

    }

    public record PlainListener(int[] sum, int listenerIndex) {

        @SubscribeEvent
        public void onEvent(PlainEvent event) {
            sum[0] += event.value;
        }

    }

    public record InheritedListener(int[] sum, int listenerIndex) {

        @SubscribeEvent
        public void onEvent(InheritedEvent event) {
            sum[0] += event.value;
        }

    }

    public record CancellationListeners(int[] sum) {

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void cancel(CancelEvent event) {
            sum[0] += event.value;
            event.setCanceled(true);
        }

        @SubscribeEvent(priority = EventPriority.HIGH)
        public void skipped(CancelEvent event) {
            sum[0] += event.value;
        }

        @SubscribeEvent(receiveCanceled = true)
        public void restore(CancelEvent event) {
            sum[0] += event.value;
            event.setCanceled(false);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void recovered(CancelEvent event) {
            sum[0] += event.value;
        }

    }

    public record GenericListeners(int[] sum, int listenerIndex) {

        @SubscribeEvent
        public void matches(TypedEvent<String> event) {
            sum[0] += event.value;
        }

        @SubscribeEvent
        public void filtered(TypedEvent<Integer> event) {
            sum[0] += event.value;
        }

    }

    public record OwnedListener(int[] sum, int listenerIndex) {

        @SubscribeEvent
        public void onEvent(OwnedEvent event) {
            sum[0] += event.value;
        }

    }

    public record RegistrationListener(int[] sum, int listenerIndex) {

        @SubscribeEvent
        protected void onEvent(RegistrationEvent event) {
            sum[0] += event.value;
        }

    }

}

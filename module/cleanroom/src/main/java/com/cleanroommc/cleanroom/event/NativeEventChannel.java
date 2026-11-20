/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.event;

import org.jspecify.annotations.NullMarked;

import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.CancellableEventBus;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.InheritableEvent;
import net.minecraftforge.eventbus.api.event.MutableEvent;
import net.minecraftforge.eventbus.api.event.RecordEvent;
import net.minecraftforge.eventbus.api.listener.EventListener;
import net.minecraftforge.eventbus.api.listener.ObjBooleanBiConsumer;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** Synchronous subscriptions for one event type in a Forge EventBus 7 group. Keep the channel in a stable field. */
@NullMarked
public final class NativeEventChannel<E> {

    private final Class<E> eventType;
    private final BusGroup scope;
    private final EventBus<? super E> bus;

    private NativeEventChannel(Class<E> eventType, BusGroup scope, EventBus<? super E> bus) {
        this.eventType = eventType;
        this.scope = scope;
        this.bus = bus;
    }

    public static <E extends RecordEvent> NativeEventChannel<E> record(BusGroup scope, Class<E> eventType) {
        requireNative(eventType);
        return new NativeEventChannel<>(eventType, scope, EventBus.create(scope, eventType));
    }

    public static <E extends MutableEvent> NativeEventChannel<E> mutable(BusGroup scope, Class<E> eventType) {
        return new NativeEventChannel<>(eventType, scope, EventBus.create(scope, eventType));
    }

    public static <E extends InheritableEvent> NativeEventChannel<E> inheritable(BusGroup scope, Class<E> eventType) {
        requireNative(eventType);
        return new NativeEventChannel<>(eventType, scope, EventBus.create(scope, eventType));
    }

    private static void requireNative(Class<?> eventType) {
        if (Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException(eventType.getName() + " is a legacy event");
        }
    }

    private static <E> boolean invoke(E event, boolean cancelled, ModContainer owner, Callback<? super E> callback) {
        var previous = ModEventContext.swapOwner(owner);
        try {
            if (!ModEventContext.LOG_LISTENER_CONTEXT) {
                return callback.invoke(event, cancelled);
            }
            String previousLog = ModEventContext.swapLogMod(owner.getName());
            try {
                return callback.invoke(event, cancelled);
            } finally {
                ModEventContext.restoreLogMod(previousLog);
            }
        } catch (Throwable failure) {
            throw NativeEventChannel.<RuntimeException>propagate(failure);
        } finally {
            ModEventContext.setOwner(previous);
        }
    }

    @SuppressWarnings("unchecked")
    private static <X extends Throwable> X propagate(Throwable failure) throws X {
        throw (X) failure;
    }

    public Class<E> eventType() {
        return eventType;
    }

    public BusGroup scope() {
        return scope;
    }

    public Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, String source, Consumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscribe(subscriptions, priority, source, Kind.CONSUMER, (event, cancelled) -> {
            listener.accept(event);
            return false;
        });
    }

    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, byte priority, String source, Predicate<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscribe(subscriptions, priority, source, Kind.PREDICATE, (event, cancelled) -> listener.test(event));
    }

    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, byte priority, String source, Consumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscribe(subscriptions, priority, source, Kind.ALWAYS_CANCEL, (event, cancelled) -> {
            listener.accept(event);
            return true;
        });
    }

    public Subscription monitor(
        SubscriptionGroup subscriptions, String source, ObjBooleanBiConsumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscribe(subscriptions, Priority.MONITOR, source, Kind.MONITOR, (event, cancelled) -> {
            listener.accept(event, cancelled);
            return false;
        });
    }

    public boolean post(E event) {
        if (event instanceof Event) {
            throw new IllegalArgumentException("Legacy events require LegacyEventChannel");
        }
        return bus.post(event);
    }

    Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, String source, Kind kind, Callback<? super E> callback) {
        if (kind != Kind.CONSUMER && !(bus instanceof CancellableEventBus<?>)) {
            throw new IllegalArgumentException(eventType.getName() + " is not a cancellable Forge 7 event");
        }
        if (priority == Priority.MONITOR && (kind == Kind.ALWAYS_CANCEL || kind == Kind.PREDICATE)) {
            throw new IllegalArgumentException("A Forge 7 monitor cannot cancel " + eventType.getName());
        }
        String description = eventType.getName() + " <- " + Objects.requireNonNull(source, "source");
        return subscriptions.install(description, () -> install(priority, kind, callback, subscriptions.owner()));
    }

    private Runnable install(byte priority, Kind kind, Callback<? super E> callback, ModContainer owner) {
        EventListener token = switch (kind) {
            case CONSUMER -> bus.addListener(priority, event -> invoke(eventType.cast(event), false, owner, callback));
            case ALWAYS_CANCEL ->
                cancellingBus().addListener(
                    priority, true, event -> invoke(eventType.cast(event), false, owner, callback)
                );
            case PREDICATE ->
                cancellingBus().addListener(priority, event -> {
                    E typedEvent = eventType.cast(event);
                    return invoke(typedEvent, false, owner, callback);
                });
            case MONITOR ->
                cancellingBus().addListener((event, cancelled) -> invoke(
                    eventType.cast(event), cancelled, owner, callback
                ));
        };
        return () -> bus.removeListener(token);
    }

    @SuppressWarnings("unchecked")
    private CancellableEventBus<? super E> cancellingBus() {
        return (CancellableEventBus<? super E>) bus;
    }

    enum Kind {

        CONSUMER,
        ALWAYS_CANCEL,
        PREDICATE,
        MONITOR

    }

    @FunctionalInterface
    interface Callback<E> {

        boolean invoke(E event, boolean cancelled) throws Throwable;

    }

}

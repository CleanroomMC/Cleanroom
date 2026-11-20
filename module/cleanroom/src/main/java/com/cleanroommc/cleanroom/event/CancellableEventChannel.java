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
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.EventListener;
import net.minecraftforge.eventbus.api.listener.ObjBooleanBiConsumer;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.internal.Event;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/** An event channel with Forge 7 cancellation and cancellation-aware monitoring. */
@NullMarked
public final class CancellableEventChannel<E extends Cancellable> extends EventChannel<E> {

    CancellableEventChannel(Class<E> eventType, BusGroup scope, CancellableEventBus<?> bus, EventKind kind) {
        super(eventType, scope, bus, kind);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Event & Cancellable> EventListener addCancelling(
        CancellableEventBus<T> bus, byte priority, Predicate<?> listener) {
        return bus.addListener(priority, (Predicate<T>) listener);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Event & Cancellable> EventListener addAlwaysCancelling(
        CancellableEventBus<T> bus, byte priority, Consumer<?> listener) {
        return bus.addListener(priority, true, (Consumer<T>) listener);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Event & Cancellable> EventListener addMonitor(
        CancellableEventBus<T> bus, ObjBooleanBiConsumer<?> listener) {
        return bus.addListener((ObjBooleanBiConsumer<T>) listener);
    }

    private static <E> Predicate<E> ownPredicate(ModContainer owner, Predicate<? super E> listener) {
        return event -> {
            var frame = ModEventContext.currentFrame();
            var previous = ModEventContext.pushDispatchOwner(frame, owner);
            try {
                String previousLog = ModEventContext.LOG_LISTENER_CONTEXT
                    ? ModEventContext.swapLogMod(owner.getName())
                    : null;
                try {
                    return listener.test(event);
                } finally {
                    if (ModEventContext.LOG_LISTENER_CONTEXT) {
                        ModEventContext.restoreLogMod(previousLog);
                    }
                }
            } finally {
                ModEventContext.restoreDispatchOwner(frame, previous);
            }
        };
    }

    private static <E> ObjBooleanBiConsumer<E> ownMonitor(
        ModContainer owner, ObjBooleanBiConsumer<? super E> listener) {
        return (event, cancelled) -> {
            var frame = ModEventContext.currentFrame();
            var previous = ModEventContext.pushDispatchOwner(frame, owner);
            try {
                String previousLog = ModEventContext.LOG_LISTENER_CONTEXT
                    ? ModEventContext.swapLogMod(owner.getName())
                    : null;
                try {
                    listener.accept(event, cancelled);
                } finally {
                    if (ModEventContext.LOG_LISTENER_CONTEXT) {
                        ModEventContext.restoreLogMod(previousLog);
                    }
                }
            } finally {
                ModEventContext.restoreDispatchOwner(frame, previous);
            }
        };
    }

    /** Returns the Forge bus. Use {@link CancellableEventBus#create(BusGroup, Class)} for a typed view. */
    @Override
    public CancellableEventBus<?> raw() {
        return (CancellableEventBus<?>) super.raw();
    }

    public Subscription subscribeCancelling(SubscriptionGroup subscriptions, Predicate<? super E> listener) {
        return subscribeCancelling(subscriptions, Priority.NORMAL, listener);
    }

    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, byte priority, Predicate<? super E> listener) {
        return subscribeCancelling(subscriptions, priority, SubscriptionGroup.callbackSource(listener), listener);
    }

    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, String source, Predicate<? super E> listener) {
        return subscribeCancelling(subscriptions, Priority.NORMAL, source, listener);
    }

    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, byte priority, String source, Predicate<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        requireCancellingPriority(priority);
        return subscriptions.install(
            describe(source),
            () -> removal(addCancelling(raw(), priority, ownPredicate(subscriptions.owner(), listener)))
        );
    }

    public Subscription subscribeAlwaysCancelling(SubscriptionGroup subscriptions, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(subscriptions, Priority.NORMAL, listener);
    }

    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, byte priority, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(subscriptions, priority, SubscriptionGroup.callbackSource(listener), listener);
    }

    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, String source, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(subscriptions, Priority.NORMAL, source, listener);
    }

    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, byte priority, String source, Consumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        requireCancellingPriority(priority);
        return subscriptions.install(
            describe(source),
            () -> removal(addAlwaysCancelling(raw(), priority, ownConsumer(subscriptions.owner(), listener)))
        );
    }

    public Subscription monitor(SubscriptionGroup subscriptions, ObjBooleanBiConsumer<? super E> listener) {
        return monitor(subscriptions, SubscriptionGroup.callbackSource(listener), listener);
    }

    public Subscription monitor(
        SubscriptionGroup subscriptions, String source, ObjBooleanBiConsumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscriptions.install(
            describe(source), () -> removal(addMonitor(raw(), ownMonitor(subscriptions.owner(), listener)))
        );
    }

    private void requireCancellingPriority(byte priority) {
        if (priority == Priority.MONITOR) {
            throw new IllegalArgumentException("A Forge 7 monitor cannot cancel " + eventType().getName());
        }
    }

}

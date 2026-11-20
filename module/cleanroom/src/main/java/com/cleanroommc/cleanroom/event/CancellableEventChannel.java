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
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * An event channel with Forge 7 short-circuit cancellation and cancellation-aware monitoring.
 * <p>
 * In Forge EventBus 7, once a cancelling listener cancels an event, lower-priority non-monitor listeners are skipped
 * and cancellation cannot be undone. Monitor listeners registered via {@link #monitor} run at {@link Priority#MONITOR}
 * regardless of cancellation and receive the final cancellation flag.
 * <p>
 * Create instances via {@link EventChannel#cancellable(Class)}, {@link EventChannel#cancellableRecord(Class)},
 * {@link EventChannel#cancellableMutable(Class)}, or {@link EventChannel#cancellableInheritable(Class)}.
 *
 * @param <E> the cancellable Forge 7 event type
 */
@NullMarked
public final class CancellableEventChannel<E extends Cancellable> extends EventChannel<E> {

    CancellableEventChannel(Class<E> eventType, BusGroup scope, CancellableEventBus<?> bus, EventKind kind) {
        super(eventType, scope, bus, kind);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EventListener addCancelling(CancellableEventBus<?> bus, byte priority, Predicate<?> listener) {
        return ((CancellableEventBus) bus).addListener(priority, listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EventListener addAlwaysCancelling(CancellableEventBus<?> bus, byte priority, Consumer<?> listener) {
        return ((CancellableEventBus) bus).addListener(priority, true, listener);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EventListener addMonitor(CancellableEventBus<?> bus, ObjBooleanBiConsumer<?> listener) {
        return ((CancellableEventBus) bus).addListener(listener);
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

    /**
     * Returns the underlying cancellable Forge 7 bus. Direct registrations use upstream tokens and the publisher's context.
     * Obtain the same bus with its exact event type using {@link CancellableEventBus#create(BusGroup, Class)}.
     *
     * @return the underlying {@link CancellableEventBus}
     */
    @Override
    public CancellableEventBus<?> raw() {
        return (CancellableEventBus<?>) super.raw();
    }

    /**
     * Subscribes a predicate listener at {@link Priority#NORMAL} priority that cancels the event when it returns
     * {@code true}.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeCancelling(SubscriptionGroup subscriptions, Predicate<? super E> listener) {
        return subscribeCancelling(subscriptions, Priority.NORMAL, listener);
    }

    /**
     * Subscribes a predicate listener at an explicit {@link Priority} that cancels the event when it returns
     * {@code true}.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, byte priority, Predicate<? super E> listener) {
        return subscribeCancelling(subscriptions, priority, SubscriptionGroup.callbackSource(listener), listener);
    }

    /**
     * Subscribes a predicate listener at {@link Priority#NORMAL} priority with a custom diagnostic source label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param source a human-readable label identifying the registration site
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, String source, Predicate<? super E> listener) {
        return subscribeCancelling(subscriptions, Priority.NORMAL, source, listener);
    }

    /**
     * Subscribes a predicate listener with an explicit {@link Priority} and diagnostic source label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param source a human-readable label identifying the registration site
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeCancelling(
        SubscriptionGroup subscriptions, byte priority, String source, Predicate<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        requireCancellingPriority(priority);
        return subscriptions.install(
            describe(source),
            () -> removal(addCancelling(raw(), priority, ownPredicate(subscriptions.owner(), listener)))
        );
    }

    /**
     * Subscribes a cancelling predicate listener at {@link Priority#NORMAL} in the owning mod's default group.
     *
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     */
    public Subscription subscribeCancelling(Predicate<? super E> listener) {
        return subscribeCancelling(SubscriptionGroup.current(), listener);
    }

    /**
     * Subscribes a cancelling predicate listener at an explicit {@link Priority} in the owning mod's default group.
     *
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribeCancelling(byte priority, Predicate<? super E> listener) {
        return subscribeCancelling(SubscriptionGroup.current(), priority, listener);
    }

    /**
     * Subscribes a cancelling predicate listener at {@link Priority#NORMAL} with a custom diagnostic source label in
     * the owning mod's default group.
     *
     * @param source a human-readable label identifying the registration site
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     */
    public Subscription subscribeCancelling(String source, Predicate<? super E> listener) {
        return subscribeCancelling(SubscriptionGroup.current(), source, listener);
    }

    /**
     * Subscribes a cancelling predicate listener with an explicit {@link Priority} and diagnostic source label in the
     * owning mod's default group.
     *
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param source a human-readable label identifying the registration site
     * @param listener the predicate invoked on dispatch; returning {@code true} cancels further non-monitor listeners
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribeCancelling(byte priority, String source, Predicate<? super E> listener) {
        return subscribeCancelling(SubscriptionGroup.current(), priority, source, listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} priority that unconditionally cancels the event after running.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeAlwaysCancelling(SubscriptionGroup subscriptions, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(subscriptions, Priority.NORMAL, listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} that unconditionally cancels the event after running.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, byte priority, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(subscriptions, priority, SubscriptionGroup.callbackSource(listener), listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} priority with a custom diagnostic source label that
     * unconditionally cancels the event.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, String source, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(subscriptions, Priority.NORMAL, source, listener);
    }

    /**
     * Subscribes a listener with an explicit {@link Priority} and diagnostic source label that unconditionally cancels
     * the event.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribeAlwaysCancelling(
        SubscriptionGroup subscriptions, byte priority, String source, Consumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        requireCancellingPriority(priority);
        return subscriptions.install(
            describe(source),
            () -> removal(addAlwaysCancelling(raw(), priority, ownConsumer(subscriptions.owner(), listener)))
        );
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} that unconditionally cancels the event after running, in the
     * owning mod's default group.
     *
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     */
    public Subscription subscribeAlwaysCancelling(Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(SubscriptionGroup.current(), listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} that unconditionally cancels the event after running, in
     * the owning mod's default group.
     *
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribeAlwaysCancelling(byte priority, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(SubscriptionGroup.current(), priority, listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} with a custom diagnostic source label that unconditionally
     * cancels the event, in the owning mod's default group.
     *
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     */
    public Subscription subscribeAlwaysCancelling(String source, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(SubscriptionGroup.current(), source, listener);
    }

    /**
     * Subscribes a listener with an explicit {@link Priority} and diagnostic source label that unconditionally cancels
     * the event, in the owning mod's default group.
     *
     * @param priority the dispatch priority constant from {@link Priority}; must not be {@link Priority#MONITOR}
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke before cancelling further non-monitor dispatch
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribeAlwaysCancelling(byte priority, String source, Consumer<? super E> listener) {
        return subscribeAlwaysCancelling(SubscriptionGroup.current(), priority, source, listener);
    }

    /**
     * Subscribes a {@link Priority#MONITOR} listener that observes every posted event along with its cancellation state.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param listener the callback invoked at {@link Priority#MONITOR} with {@code (event, cancelled)}
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription monitor(SubscriptionGroup subscriptions, ObjBooleanBiConsumer<? super E> listener) {
        return monitor(subscriptions, SubscriptionGroup.callbackSource(listener), listener);
    }

    /**
     * Subscribes a {@link Priority#MONITOR} listener with a custom diagnostic source label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param source a human-readable label identifying the registration site
     * @param listener the callback invoked at {@link Priority#MONITOR} with {@code (event, cancelled)}
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription monitor(
        SubscriptionGroup subscriptions, String source, ObjBooleanBiConsumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscriptions.install(
            describe(source), () -> removal(addMonitor(raw(), ownMonitor(subscriptions.owner(), listener)))
        );
    }

    /**
     * Subscribes a {@link Priority#MONITOR} listener in the owning mod's default group.
     *
     * @param listener the callback invoked at {@link Priority#MONITOR} with {@code (event, cancelled)}
     * @return the subscription handle used to unregister the listener
     */
    public Subscription monitor(ObjBooleanBiConsumer<? super E> listener) {
        return monitor(SubscriptionGroup.current(), listener);
    }

    /**
     * Subscribes a {@link Priority#MONITOR} listener with a custom diagnostic source label in the owning mod's default
     * group.
     *
     * @param source a human-readable label identifying the registration site
     * @param listener the callback invoked at {@link Priority#MONITOR} with {@code (event, cancelled)}
     * @return the subscription handle used to unregister the listener
     */
    public Subscription monitor(String source, ObjBooleanBiConsumer<? super E> listener) {
        return monitor(SubscriptionGroup.current(), source, listener);
    }

    private void requireCancellingPriority(byte priority) {
        if (priority == Priority.MONITOR) {
            throw new IllegalArgumentException("A Forge 7 monitor cannot cancel " + eventType().getName());
        }
    }

}

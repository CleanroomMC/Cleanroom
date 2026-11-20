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
import net.minecraftforge.eventbus.api.event.characteristic.Cancellable;
import net.minecraftforge.eventbus.api.listener.EventListener;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.eventbus.internal.Event;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Consumer;

/** Synchronous Forge 7 dispatch for one event type and scope. Store it on the event as a static final field. */
@NullMarked
public sealed class EventChannel<E> permits CancellableEventChannel {

    private final Class<E> eventType;
    private final BusGroup scope;
    private final EventBus<?> bus;
    private final EventKind kind;

    EventChannel(Class<E> eventType, BusGroup scope, EventBus<?> bus, EventKind kind) {
        this.eventType = eventType;
        this.scope = scope;
        this.bus = bus;
        this.kind = kind;
    }

    public static <E> EventChannel<E> of(Class<E> eventType) {
        return of(BusGroup.DEFAULT, eventType);
    }

    public static <E> EventChannel<E> of(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.of(eventType));
    }

    public static <E extends RecordEvent> EventChannel<E> record(Class<E> eventType) {
        return record(BusGroup.DEFAULT, eventType);
    }

    public static <E extends RecordEvent> EventChannel<E> record(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.RECORD);
    }

    public static <E extends MutableEvent> EventChannel<E> mutable(Class<E> eventType) {
        return mutable(BusGroup.DEFAULT, eventType);
    }

    public static <E extends MutableEvent> EventChannel<E> mutable(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.MUTABLE);
    }

    public static <E extends InheritableEvent> EventChannel<E> inheritable(Class<E> eventType) {
        return inheritable(BusGroup.DEFAULT, eventType);
    }

    public static <E extends InheritableEvent> EventChannel<E> inheritable(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.INHERITABLE);
    }

    /** Creates a cancellable channel on {@link BusGroup#DEFAULT}. */
    public static <E extends Cancellable> CancellableEventChannel<E> cancellable(Class<E> eventType) {
        return cancellable(BusGroup.DEFAULT, eventType);
    }

    /** Creates a cancellable channel for a Forge 7 event in the given scope. */
    public static <E extends Cancellable> CancellableEventChannel<E> cancellable(BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.of(eventType));
    }

    public static <E extends RecordEvent & Cancellable> CancellableEventChannel<E> cancellableRecord(
        Class<E> eventType) {
        return cancellableRecord(BusGroup.DEFAULT, eventType);
    }

    public static <E extends RecordEvent & Cancellable> CancellableEventChannel<E> cancellableRecord(
        BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.RECORD);
    }

    public static <E extends MutableEvent & Cancellable> CancellableEventChannel<E> cancellableMutable(
        Class<E> eventType) {
        return cancellableMutable(BusGroup.DEFAULT, eventType);
    }

    public static <E extends MutableEvent & Cancellable> CancellableEventChannel<E> cancellableMutable(
        BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.MUTABLE);
    }

    public static <E extends InheritableEvent & Cancellable> CancellableEventChannel<E> cancellableInheritable(
        Class<E> eventType) {
        return cancellableInheritable(BusGroup.DEFAULT, eventType);
    }

    public static <E extends InheritableEvent & Cancellable> CancellableEventChannel<E> cancellableInheritable(
        BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.INHERITABLE);
    }

    static <E> Consumer<E> ownConsumer(ModContainer owner, Consumer<? super E> listener) {
        return event -> {
            var frame = ModEventContext.currentFrame();
            var previous = ModEventContext.pushDispatchOwner(frame, owner);
            try {
                String previousLog = ModEventContext.LOG_LISTENER_CONTEXT
                    ? ModEventContext.swapLogMod(owner.getName())
                    : null;
                try {
                    listener.accept(event);
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

    private static <E> EventChannel<E> create(BusGroup scope, Class<E> eventType, EventKind kind) {
        kind.requireType(eventType);
        return new EventChannel<>(eventType, scope, createBus(scope, eventType), kind);
    }

    private static <E extends Cancellable> CancellableEventChannel<E> createCancellable(
        BusGroup scope, Class<E> eventType, EventKind kind) {
        kind.requireType(eventType);
        if (!Cancellable.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException(eventType.getName() + " does not implement Cancellable");
        }
        return new CancellableEventChannel<>(
            eventType, scope, (CancellableEventBus<?>) createBus(scope, eventType), kind
        );
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EventBus<?> createBus(BusGroup scope, Class<?> eventType) {
        // EventKind has already checked that the class implements an exported Forge event base type.
        return EventBus.create(scope, (Class) eventType);
    }

    private static void requireNonLegacy(Class<?> eventType) {
        if (net.minecraftforge.fml.common.eventhandler.Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException(eventType.getName() + " is a legacy event");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Event> EventListener addListener(EventBus<T> bus, byte priority, Consumer<?> listener) {
        // The channel and callback share the exact event class of this bus.
        return bus.addListener(priority, (Consumer<T>) listener);
    }

    /**
     * Returns the event class bound to this channel.
     *
     * @return the event class
     */
    public final Class<E> eventType() {
        return eventType;
    }

    /**
     * Returns the {@link BusGroup} scope that owns the underlying bus.
     *
     * @return the bus group scope
     */
    public final BusGroup scope() {
        return scope;
    }

    /**
     * Returns the underlying Forge 7 bus. Direct registrations use upstream tokens and the publisher's context.
     * Obtain the same bus with its exact event type using {@link EventBus#create(BusGroup, Class)} and the channel's
     * {@link #scope()} and {@link #eventType()}.
     *
     * @return the underlying Forge 7 {@link EventBus}
     */
    public EventBus<?> raw() {
        return bus;
    }

    /**
     * Returns whether any listeners are currently registered on this channel's bus.
     *
     * @return {@code true} if at least one listener is registered
     */
    public final boolean hasListeners() {
        return bus.hasListeners();
    }

    /**
     * Synchronously dispatches the event to registered listeners and returns its cancellation state.
     *
     * @param event the event instance to dispatch
     * @return {@code true} if the event was cancelled during dispatch, {@code false} otherwise
     * @throws IllegalArgumentException if {@code event} is a legacy Forge event
     */
    public final boolean post(E event) {
        if (event instanceof net.minecraftforge.fml.common.eventhandler.Event) {
            throw new IllegalArgumentException("Legacy events require LegacyEventChannel");
        }
        if (!bus.hasListeners()) {
            return false;
        }
        var frame = ModEventContext.currentFrame();
        boolean opened = ModEventContext.enterDispatch(frame);
        try {
            return kind.dispatch.test(bus, event);
        } finally {
            ModEventContext.exitDispatch(frame, opened);
        }
    }

    /**
     * Synchronously posts the event and returns the same event instance for caller convenience.
     *
     * @param event the event instance to dispatch
     * @return the dispatched {@code event} instance
     * @throws IllegalArgumentException if {@code event} is a legacy Forge event
     */
    public final E fire(E event) {
        post(event);
        return event;
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} priority under the given subscription group.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public final Subscription subscribe(SubscriptionGroup subscriptions, Consumer<? super E> listener) {
        return subscribe(subscriptions, Priority.NORMAL, listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} under the given subscription group.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority constant from {@link Priority}
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public final Subscription subscribe(SubscriptionGroup subscriptions, byte priority, Consumer<? super E> listener) {
        return subscribe(subscriptions, priority, SubscriptionGroup.callbackSource(listener), listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} priority with a custom diagnostic source label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public final Subscription subscribe(SubscriptionGroup subscriptions, String source, Consumer<? super E> listener) {
        return subscribe(subscriptions, Priority.NORMAL, source, listener);
    }

    /**
     * Subscribes a listener with an explicit {@link Priority} and diagnostic source label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority constant from {@link Priority}
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public final Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, String source, Consumer<? super E> listener) {
        Objects.requireNonNull(listener, "listener");
        return subscriptions.install(
            describe(source), () -> removal(addListener(bus, priority, ownConsumer(subscriptions.owner(), listener)))
        );
    }

    /**
     * Registers the target's declared Forge 7 subscribers for this channel's exact {@link #eventType()}.
     * <p>
     * Passing a {@link Class} registers only static methods annotated with
     * {@link net.minecraftforge.eventbus.api.listener.SubscribeEvent}; passing an instance registers both static and
     * instance methods declared on that instance's class. The supplied {@code lookup} acts as the access capability
     * for unreflecting subscribers, including private methods accessible to it. Installation is atomic per batch: if
     * installing any subscriber fails, earlier subscriptions from the batch are rolled back.
     *
     * @param subscriptions the subscription group that owns the registered listeners
     * @param lookup the lookup capability granted for accessing subscriber methods on {@code target}
     * @param target a subscriber instance or a {@link Class} declaring static subscriber methods
     * @return an immutable list containing one {@link Subscription} handle per registered method
     * @throws IllegalArgumentException if no matching subscribers exist, a method signature is invalid, or
     * {@code lookup} cannot access a subscriber method
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public final List<Subscription> subscribe(
        SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        return ChannelSubscribers.register(this, subscriptions, lookup, target);
    }

    final String describe(String source) {
        return eventType.getName() + " <- " + Objects.requireNonNull(source, "source");
    }

    final Runnable removal(EventListener token) {
        return () -> bus.removeListener(token);
    }

    // Dispatch through exported parameter types so an explicit Forge module can keep its Event marker private.
    @SuppressWarnings("unchecked")
    private static boolean postRecord(EventBus<?> bus, Object event) {
        return ((EventBus<RecordEvent>) bus).post((RecordEvent) event);
    }

    @SuppressWarnings("unchecked")
    private static boolean postMutable(EventBus<?> bus, Object event) {
        return ((EventBus<MutableEvent>) bus).post((MutableEvent) event);
    }

    @SuppressWarnings("unchecked")
    private static boolean postInheritable(EventBus<?> bus, Object event) {
        return ((EventBus<InheritableEvent>) bus).post((InheritableEvent) event);
    }

    enum EventKind {

        RECORD(RecordEvent.class, EventChannel::postRecord),
        MUTABLE(MutableEvent.class, EventChannel::postMutable),
        INHERITABLE(InheritableEvent.class, EventChannel::postInheritable);

        private final Class<?> baseType;
        private final BiPredicate<EventBus<?>, Object> dispatch;

        EventKind(Class<?> baseType, BiPredicate<EventBus<?>, Object> dispatch) {
            this.baseType = baseType;
            this.dispatch = dispatch;
        }

        static EventKind of(Class<?> eventType) {
            if (RecordEvent.class.isAssignableFrom(eventType)) {
                return RECORD;
            }
            if (MutableEvent.class.isAssignableFrom(eventType)) {
                return MUTABLE;
            }
            if (InheritableEvent.class.isAssignableFrom(eventType)) {
                return INHERITABLE;
            }
            requireNonLegacy(eventType);
            throw new IllegalArgumentException(eventType.getName() + " is not a Forge 7 event type");
        }

        void requireType(Class<?> eventType) {
            requireNonLegacy(eventType);
            if (!baseType.isAssignableFrom(eventType)) {
                throw new IllegalArgumentException(eventType.getName() + " is not a " + baseType.getSimpleName());
            }
        }

    }

}

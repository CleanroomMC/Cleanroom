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
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Synchronous Forge EventBus 7 dispatch for one event type and {@link BusGroup} scope.
 * <p>
 * Channels are typically declared as {@code public static final} fields on the event class itself:
 * <pre>{@code
 * public record MyEvent(int value) implements RecordEvent {
 *     public static final EventChannel<MyEvent> BUS = EventChannel.record(MyEvent.class);
 * }
 * }</pre>
 * <p>
 * Channel factories default to {@link BusGroup#DEFAULT} unless an explicit {@link BusGroup} scope is supplied.
 * Cancellable Forge 7 events should use {@link CancellableEventChannel}, which adds cancellation and monitor support.
 * Managed callbacks registered through {@link SubscriptionGroup} run with that group's owning {@link ModContainer}
 * active on the dispatching thread; {@link #raw()} exposes the underlying {@link EventBus} for direct upstream use.
 *
 * @param <E> the Forge 7 event type
 */
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

    /**
     * Creates a channel on {@link BusGroup#DEFAULT} for an event class.
     *
     * @param <E> the Forge 7 event type
     * @param eventType the event class
     * @return a new event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or invalid Forge 7 event type
     */
    public static <E> EventChannel<E> of(Class<E> eventType) {
        return of(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a channel in the given {@link BusGroup} scope for an event class.
     *
     * @param <E> the Forge 7 event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class
     * @return a new event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or invalid Forge 7 event type
     */
    public static <E> EventChannel<E> of(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.of(eventType));
    }

    /**
     * Creates a channel on {@link BusGroup#DEFAULT} for a {@link RecordEvent} class.
     *
     * @param <E> the record event type
     * @param eventType the event class implementing {@link RecordEvent}
     * @return a new event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or does not implement {@link RecordEvent}
     */
    public static <E extends RecordEvent> EventChannel<E> record(Class<E> eventType) {
        return record(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a channel in the given {@link BusGroup} scope for a {@link RecordEvent} class.
     *
     * @param <E> the record event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing {@link RecordEvent}
     * @return a new event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or does not implement {@link RecordEvent}
     */
    public static <E extends RecordEvent> EventChannel<E> record(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.RECORD);
    }

    /**
     * Creates a channel on {@link BusGroup#DEFAULT} for a {@link MutableEvent} class.
     *
     * @param <E> the mutable event type
     * @param eventType the event class implementing {@link MutableEvent}
     * @return a new event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or does not implement {@link MutableEvent}
     */
    public static <E extends MutableEvent> EventChannel<E> mutable(Class<E> eventType) {
        return mutable(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a channel in the given {@link BusGroup} scope for a {@link MutableEvent} class.
     *
     * @param <E> the mutable event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing {@link MutableEvent}
     * @return a new event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or does not implement {@link MutableEvent}
     */
    public static <E extends MutableEvent> EventChannel<E> mutable(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.MUTABLE);
    }

    /**
     * Creates a channel on {@link BusGroup#DEFAULT} for an {@link InheritableEvent} class.
     *
     * @param <E> the inheritable event type
     * @param eventType the event class implementing {@link InheritableEvent}
     * @return a new event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or does not implement
     * {@link InheritableEvent}
     */
    public static <E extends InheritableEvent> EventChannel<E> inheritable(Class<E> eventType) {
        return inheritable(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a channel in the given {@link BusGroup} scope for an {@link InheritableEvent} class.
     *
     * @param <E> the inheritable event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing {@link InheritableEvent}
     * @return a new event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or does not implement
     * {@link InheritableEvent}
     */
    public static <E extends InheritableEvent> EventChannel<E> inheritable(BusGroup scope, Class<E> eventType) {
        return create(scope, eventType, EventKind.INHERITABLE);
    }

    /**
     * Creates a cancellable channel on {@link BusGroup#DEFAULT} for an event class.
     *
     * @param <E> the cancellable Forge 7 event type
     * @param eventType the event class implementing a Forge 7 base type and {@link Cancellable}
     * @return a new cancellable event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event, lacks a valid Forge 7 base type, or
     * does not implement {@link Cancellable}
     */
    public static <E extends Cancellable> CancellableEventChannel<E> cancellable(Class<E> eventType) {
        return cancellable(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a cancellable channel in the given {@link BusGroup} scope for an event class.
     *
     * @param <E> the cancellable Forge 7 event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing a Forge 7 base type and {@link Cancellable}
     * @return a new cancellable event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event, lacks a valid Forge 7 base type, or
     * does not implement {@link Cancellable}
     */
    public static <E extends Cancellable> CancellableEventChannel<E> cancellable(BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.of(eventType));
    }

    /**
     * Creates a cancellable channel on {@link BusGroup#DEFAULT} for a cancellable {@link RecordEvent} class.
     *
     * @param <E> the cancellable record event type
     * @param eventType the event class implementing {@link RecordEvent} and {@link Cancellable}
     * @return a new cancellable event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or lacks {@link RecordEvent} or
     * {@link Cancellable}
     */
    public static <E extends RecordEvent & Cancellable> CancellableEventChannel<E> cancellableRecord(
        Class<E> eventType) {
        return cancellableRecord(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a cancellable channel in the given {@link BusGroup} scope for a cancellable {@link RecordEvent} class.
     *
     * @param <E> the cancellable record event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing {@link RecordEvent} and {@link Cancellable}
     * @return a new cancellable event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or lacks {@link RecordEvent} or
     * {@link Cancellable}
     */
    public static <E extends RecordEvent & Cancellable> CancellableEventChannel<E> cancellableRecord(
        BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.RECORD);
    }

    /**
     * Creates a cancellable channel on {@link BusGroup#DEFAULT} for a cancellable {@link MutableEvent} class.
     *
     * @param <E> the cancellable mutable event type
     * @param eventType the event class implementing {@link MutableEvent} and {@link Cancellable}
     * @return a new cancellable event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or lacks {@link MutableEvent} or
     * {@link Cancellable}
     */
    public static <E extends MutableEvent & Cancellable> CancellableEventChannel<E> cancellableMutable(
        Class<E> eventType) {
        return cancellableMutable(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a cancellable channel in the given {@link BusGroup} scope for a cancellable {@link MutableEvent} class.
     *
     * @param <E> the cancellable mutable event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing {@link MutableEvent} and {@link Cancellable}
     * @return a new cancellable event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or lacks {@link MutableEvent} or
     * {@link Cancellable}
     */
    public static <E extends MutableEvent & Cancellable> CancellableEventChannel<E> cancellableMutable(
        BusGroup scope, Class<E> eventType) {
        return createCancellable(scope, eventType, EventKind.MUTABLE);
    }

    /**
     * Creates a cancellable channel on {@link BusGroup#DEFAULT} for a cancellable {@link InheritableEvent} class.
     *
     * @param <E> the cancellable inheritable event type
     * @param eventType the event class implementing {@link InheritableEvent} and {@link Cancellable}
     * @return a new cancellable event channel bound to {@link BusGroup#DEFAULT}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or lacks {@link InheritableEvent} or
     * {@link Cancellable}
     */
    public static <E extends InheritableEvent & Cancellable> CancellableEventChannel<E> cancellableInheritable(
        Class<E> eventType) {
        return cancellableInheritable(BusGroup.DEFAULT, eventType);
    }

    /**
     * Creates a cancellable channel in the given {@link BusGroup} scope for a cancellable {@link InheritableEvent} class.
     *
     * @param <E> the cancellable inheritable event type
     * @param scope the bus group scope owning the underlying bus
     * @param eventType the event class implementing {@link InheritableEvent} and {@link Cancellable}
     * @return a new cancellable event channel bound to {@code scope}
     * @throws IllegalArgumentException if {@code eventType} is a legacy event or lacks {@link InheritableEvent} or
     * {@link Cancellable}
     */
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static EventListener addListener(EventBus<?> bus, byte priority, Consumer<?> listener) {
        // The channel and callback share the exact event class of this bus.
        return ((EventBus) bus).addListener(priority, listener);
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
            return dispatch(event);
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
     * Subscribes a listener at {@link Priority#NORMAL} in the owning mod's
     * {@linkplain SubscriptionGroup#current() default group}.
     *
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     */
    public final Subscription subscribe(Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} in the owning mod's default group.
     *
     * @param priority the dispatch priority constant from {@link Priority}
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     */
    public final Subscription subscribe(byte priority, Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), priority, listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} with a custom diagnostic source label in the owning mod's
     * default group.
     *
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     */
    public final Subscription subscribe(String source, Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), source, listener);
    }

    /**
     * Subscribes a listener with an explicit {@link Priority} and diagnostic source label in the owning mod's default
     * group.
     *
     * @param priority the dispatch priority constant from {@link Priority}
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when an event is posted
     * @return the subscription handle used to unregister the listener
     */
    public final Subscription subscribe(byte priority, String source, Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), priority, source, listener);
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

    /**
     * Registers the target's declared subscribers in the owning mod's
     * {@linkplain SubscriptionGroup#current() default group}.
     *
     * @param lookup the lookup capability granted for accessing subscriber methods on {@code target}
     * @param target a subscriber instance or a {@link Class} declaring static subscriber methods
     * @return an immutable list containing one {@link Subscription} handle per registered method
     * @throws IllegalArgumentException if no matching subscribers exist, a method signature is invalid, or
     * {@code lookup} cannot access a subscriber method
     */
    public final List<Subscription> subscribe(MethodHandles.Lookup lookup, Object target) {
        return subscribe(SubscriptionGroup.current(), lookup, target);
    }

    final String describe(String source) {
        return eventType.getName() + " <- " + Objects.requireNonNull(source, "source");
    }

    final Runnable removal(EventListener token) {
        return () -> bus.removeListener(token);
    }

    // Dispatch through exported parameter types so an explicit Forge module can keep its Event marker private.
    @SuppressWarnings("unchecked")
    private boolean dispatch(E event) {
        return switch (kind) {
            case RECORD -> ((EventBus<RecordEvent>) bus).post((RecordEvent) event);
            case MUTABLE -> ((EventBus<MutableEvent>) bus).post((MutableEvent) event);
            case INHERITABLE -> ((EventBus<InheritableEvent>) bus).post((InheritableEvent) event);
        };
    }

    enum EventKind {

        RECORD(RecordEvent.class),
        MUTABLE(MutableEvent.class),
        INHERITABLE(InheritableEvent.class);

        private final Class<?> baseType;

        EventKind(Class<?> baseType) {
            this.baseType = baseType;
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

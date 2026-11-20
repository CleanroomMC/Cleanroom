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
import org.jspecify.annotations.Nullable;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.listener.Priority;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A synchronous legacy event type and its Forge {@link EventBus} scope.
 * <p>
 * Channels are typically declared as {@code public static final} fields on the event class itself:
 * <pre>{@code
 * public final class MyLegacyEvent extends Event {
 *     public static final LegacyEventChannel<MyLegacyEvent> BUS =
 *         LegacyEventChannel.of(MyLegacyEvent.class);
 * }
 * }</pre>
 * <p>
 * Dispatch posts the original event instance without wrapping and preserves legacy Forge semantics, including
 * {@link Event#setCanceled(boolean)}, uncancellation by {@code receiveCanceled} listeners, legacy priority buckets,
 * generic type filtering via {@link #withGenericType(Type)}, and custom {@link ListenerList} tables via
 * {@link #withListenerList(ListenerList)}.
 * <p>
 * Subscription priorities use the Forge 7 {@link Priority} byte scale, the same parameter as {@link EventChannel}.
 * Values are placed in the nearest legacy bucket, so the standard constants keep their relative order;
 * {@link Priority#MONITOR} is rejected because legacy dispatch has no monitor phase.
 *
 * @param <E> the legacy Forge event type
 */
@NullMarked
public final class LegacyEventChannel<E extends Event> {

    private final Class<E> eventType;
    private final @Nullable EventBus bus;
    private final @Nullable Type genericType;
    private final @Nullable ListenerList registrationList;

    private LegacyEventChannel(
        Class<E> eventType, @Nullable EventBus bus, @Nullable Type genericType,
        @Nullable ListenerList registrationList) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.bus = bus;
        this.genericType = genericType;
        this.registrationList = registrationList;
    }

    /**
     * Creates a channel on {@link MinecraftForge#EVENT_BUS} for the given legacy event class.
     * The default bus is resolved on first use of the scope.
     *
     * @param <E> the legacy Forge event type
     * @param eventType the legacy event class
     * @return a new legacy event channel bound to {@link MinecraftForge#EVENT_BUS}
     */
    public static <E extends Event> LegacyEventChannel<E> of(Class<E> eventType) {
        return new LegacyEventChannel<>(eventType, null, null, null);
    }

    /**
     * Creates a channel on an explicit legacy {@link EventBus} scope for the given event class.
     *
     * @param <E> the legacy Forge event type
     * @param bus the legacy Forge bus owning the scope
     * @param eventType the legacy event class
     * @return a new legacy event channel bound to {@code bus}
     */
    public static <E extends Event> LegacyEventChannel<E> of(EventBus bus, Class<E> eventType) {
        return new LegacyEventChannel<>(eventType, Objects.requireNonNull(bus, "bus"), null, null);
    }

    /**
     * Returns the legacy event class bound to this channel.
     *
     * @return the legacy event class
     */
    public Class<E> eventType() {
        return eventType;
    }

    /**
     * Returns the legacy {@link EventBus} scope used by this channel.
     *
     * @return the legacy {@link EventBus}
     */
    public EventBus scope() {
        return bus != null ? bus : MinecraftForge.EVENT_BUS;
    }

    /**
     * Returns the Forge bus that owns the legacy scope.
     *
     * @return the underlying legacy {@link EventBus}
     */
    public EventBus raw() {
        return scope();
    }

    /**
     * Checks whether the declared event type or explicit {@link ListenerList} has listeners in this scope.
     * An unresolved hand-written list returns {@code true} conservatively.
     *
     * @return {@code true} if listeners may exist for this channel's declared event type
     */
    public boolean hasListeners() {
        return scope().hasManagedListeners(eventType, registrationList);
    }

    /**
     * Checks whether the {@link ListenerList} selected by the given event instance has listeners in this scope.
     *
     * @param event the event instance whose listener list should be checked
     * @return {@code true} if the bus is active and the instance's listener list is non-empty
     */
    public boolean hasListeners(E event) {
        return scope().hasManagedListeners(eventType.cast(event));
    }

    /**
     * Returns a channel view whose registrations filter legacy generic events by this exact {@link Type} identity.
     *
     * @param type the selector returned by the event's generic getter
     * @return a channel view applying generic type filtering on registration
     */
    public LegacyEventChannel<E> withGenericType(Type type) {
        return new LegacyEventChannel<>(eventType, bus, Objects.requireNonNull(type, "type"), registrationList);
    }

    /**
     * Returns a channel view whose registrations target a known hand-written {@link ListenerList}, including for an
     * abstract event class.
     *
     * @param list the listener list an event instance uses when posted
     * @return a channel view targeting {@code list} on registration and listener probing
     */
    public LegacyEventChannel<E> withListenerList(ListenerList list) {
        return new LegacyEventChannel<>(eventType, bus, genericType, Objects.requireNonNull(list, "list"));
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} without receiving already-cancelled events.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribe(SubscriptionGroup subscriptions, Consumer<? super E> listener) {
        return subscribe(subscriptions, Priority.NORMAL, listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} without receiving already-cancelled events.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority, on the same scale as {@link EventChannel}
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, Consumer<? super E> listener) {
        return subscribe(subscriptions, priority, false, listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} with a custom diagnostic source label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public Subscription subscribe(SubscriptionGroup subscriptions, String source, Consumer<? super E> listener) {
        return subscribe(subscriptions, Priority.NORMAL, false, source, listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} and diagnostic source label without receiving
     * already-cancelled events.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority, on the same scale as {@link EventChannel}
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, String source, Consumer<? super E> listener) {
        return subscribe(subscriptions, priority, false, source, listener);
    }

    /**
     * Subscribes a listener with explicit {@link Priority} and {@code receiveCanceled} handling.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority, on the same scale as {@link EventChannel}
     * @param receiveCanceled whether the listener still receives cancelled events
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, boolean receiveCanceled,
        Consumer<? super E> listener) {
        return subscribe(
            subscriptions, priority, receiveCanceled, SubscriptionGroup.callbackSource(listener), listener
        );
    }

    /**
     * Subscribes a listener with explicit {@link Priority}, {@code receiveCanceled} handling, and diagnostic source
     * label.
     *
     * @param subscriptions the subscription group that owns the registration
     * @param priority the dispatch priority, on the same scale as {@link EventChannel}
     * @param receiveCanceled whether the listener still receives cancelled events
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalStateException if {@code subscriptions} is already closed
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribe(
        SubscriptionGroup subscriptions, byte priority, boolean receiveCanceled, String source,
        Consumer<? super E> listener) {
        String description = describe(source);
        EventPriority bucket = legacyPriority(priority);
        return subscriptions.install(
            description,
            () -> scope().addManagedListener(
                eventType, registrationList, bucket, receiveCanceled, genericType, listener, subscriptions.owner(),
                description
            )
        );
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} in the owning mod's
     * {@linkplain SubscriptionGroup#current() default group}.
     *
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     */
    public Subscription subscribe(Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), listener);
    }

    /**
     * Subscribes a listener at an explicit {@link Priority} in the owning mod's default group.
     *
     * @param priority the dispatch priority, on the same scale as {@link EventChannel}
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribe(byte priority, Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), priority, listener);
    }

    /**
     * Subscribes a listener at {@link Priority#NORMAL} with a custom diagnostic source label in the owning mod's
     * default group.
     *
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     */
    public Subscription subscribe(String source, Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), source, listener);
    }

    /**
     * Subscribes a listener with an explicit {@link Priority} and diagnostic source label in the owning mod's default
     * group.
     *
     * @param priority the dispatch priority, on the same scale as {@link EventChannel}
     * @param source a human-readable label identifying the registration site
     * @param listener the callback to invoke when a matching event is posted
     * @return the subscription handle used to unregister the listener
     * @throws IllegalArgumentException if {@code priority} is {@link Priority#MONITOR}
     */
    public Subscription subscribe(byte priority, String source, Consumer<? super E> listener) {
        return subscribe(SubscriptionGroup.current(), priority, source, listener);
    }

    /**
     * Registers legacy subscribers on {@code target} for this channel's exact {@link #eventType()}.
     * <p>
     * Passing a {@link Class} discovers only static {@link net.minecraftforge.fml.common.eventhandler.SubscribeEvent}
     * methods declared on that class; passing an instance discovers declared and inherited instance subscribers (plus
     * declared static methods), using {@code lookup} for declared private methods and skipping private ancestor methods.
     * Inherited annotations preserve virtual dispatch to overriding methods. Configured {@link #withGenericType(Type)}
     * and {@link #withListenerList(ListenerList)} settings apply to all subscribers in the batch.
     *
     * @param subscriptions the subscription group that owns the registered listeners
     * @param lookup the lookup capability granted for accessing subscriber methods on {@code target}
     * @param target a subscriber instance or a {@link Class} declaring static subscriber methods
     * @return an immutable list containing one {@link Subscription} handle per registered method
     * @throws IllegalArgumentException if no matching subscribers exist, a method signature or generic target is
     * invalid, or {@code lookup} cannot access a declared subscriber method
     * @throws IllegalStateException if {@code subscriptions} is already closed
     */
    public List<Subscription> subscribe(SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        var registrations = scope().prepareManagedListeners(
            eventType, registrationList, genericType, lookup, target, subscriptions.owner()
        );
        return subscriptions.installAll(
            registrations, listener -> subscriptions.install(describe(listener.source()), listener.install())
        );
    }

    /**
     * Registers legacy subscribers on {@code target} in the owning mod's
     * {@linkplain SubscriptionGroup#current() default group}.
     *
     * @param lookup the lookup capability granted for accessing subscriber methods on {@code target}
     * @param target a subscriber instance or a {@link Class} declaring static subscriber methods
     * @return an immutable list containing one {@link Subscription} handle per registered method
     * @throws IllegalArgumentException if no matching subscribers exist, a method signature or generic target is
     * invalid, or {@code lookup} cannot access a declared subscriber method
     */
    public List<Subscription> subscribe(MethodHandles.Lookup lookup, Object target) {
        return subscribe(SubscriptionGroup.current(), lookup, target);
    }

    /**
     * Synchronously dispatches the event on the underlying legacy {@link EventBus} and returns its final cancellation
     * state.
     *
     * @param event the legacy event instance to dispatch
     * @return {@code true} if the event is cancelable and ended canceled, {@code false} otherwise
     */
    public boolean post(E event) {
        return scope().post(eventType.cast(event));
    }

    /**
     * Synchronously posts the event and returns the same event instance for caller convenience.
     *
     * @param event the legacy event instance to dispatch
     * @return the dispatched {@code event} instance
     */
    public E fire(E event) {
        post(event);
        return event;
    }

    /** Places a Forge 7 priority in the nearest legacy bucket; the standard constants keep their relative order. */
    private static EventPriority legacyPriority(byte priority) {
        if (priority == Priority.MONITOR) {
            throw new IllegalArgumentException(
                "Legacy dispatch has no monitor priority; use Priority.LOWEST and receiveCanceled instead"
            );
        }
        if (priority >= 96) {
            return EventPriority.HIGHEST;
        }
        if (priority >= 32) {
            return EventPriority.HIGH;
        }
        if (priority >= -32) {
            return EventPriority.NORMAL;
        }
        if (priority >= -96) {
            return EventPriority.LOW;
        }
        return EventPriority.LOWEST;
    }

    private String describe(String source) {
        return eventType.getName() + " <- " + Objects.requireNonNull(source, "source");
    }

}

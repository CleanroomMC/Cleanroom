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
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.ListenerList;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/** A synchronous legacy event type and its Forge bus scope. The original event instance is posted. */
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

    /** Creates a channel that resolves {@link MinecraftForge#EVENT_BUS} when first used. */
    public static <E extends Event> LegacyEventChannel<E> of(Class<E> eventType) {
        return new LegacyEventChannel<>(eventType, null, null, null);
    }

    public static <E extends Event> LegacyEventChannel<E> of(EventBus bus, Class<E> eventType) {
        return new LegacyEventChannel<>(eventType, Objects.requireNonNull(bus, "bus"), null, null);
    }

    public Class<E> eventType() {
        return eventType;
    }

    public EventBus scope() {
        return bus != null ? bus : MinecraftForge.EVENT_BUS;
    }

    /** Returns the Forge bus that owns the legacy scope. */
    public EventBus raw() {
        return scope();
    }

    /** Checks the declared event type. An unresolved hand-written list may have listeners. */
    public boolean hasListeners() {
        return scope().hasManagedListeners(eventType, registrationList);
    }

    /** Checks the listener list selected by the event instance. */
    public boolean hasListeners(E event) {
        return scope().hasManagedListeners(eventType.cast(event));
    }

    /** Limits registration to legacy generic events returning this exact Type object. */
    public LegacyEventChannel<E> withGenericType(Type type) {
        return new LegacyEventChannel<>(eventType, bus, Objects.requireNonNull(type, "type"), registrationList);
    }

    /** Points registration at a known hand-written table, including for an abstract event class. */
    public LegacyEventChannel<E> withListenerList(ListenerList list) {
        return new LegacyEventChannel<>(eventType, bus, genericType, Objects.requireNonNull(list, "list"));
    }

    public Subscription subscribe(SubscriptionGroup subscriptions, Consumer<? super E> listener) {
        return subscribe(subscriptions, EventPriority.NORMAL, listener);
    }

    public Subscription subscribe(
        SubscriptionGroup subscriptions, EventPriority priority, Consumer<? super E> listener) {
        return subscribe(subscriptions, priority, false, listener);
    }

    public Subscription subscribe(SubscriptionGroup subscriptions, String source, Consumer<? super E> listener) {
        return subscribe(subscriptions, EventPriority.NORMAL, false, source, listener);
    }

    public Subscription subscribe(
        SubscriptionGroup subscriptions, EventPriority priority, String source, Consumer<? super E> listener) {
        return subscribe(subscriptions, priority, false, source, listener);
    }

    public Subscription subscribe(
        SubscriptionGroup subscriptions, EventPriority priority, boolean receiveCanceled,
        Consumer<? super E> listener) {
        return subscribe(
            subscriptions, priority, receiveCanceled, SubscriptionGroup.callbackSource(listener), listener
        );
    }

    public Subscription subscribe(
        SubscriptionGroup subscriptions, EventPriority priority, boolean receiveCanceled, String source,
        Consumer<? super E> listener) {
        String description = describe(source);
        return subscriptions.install(
            description,
            () -> scope().addManagedListener(
                eventType, registrationList, priority, receiveCanceled, genericType, listener, subscriptions.owner(),
                description
            )
        );
    }

    public List<Subscription> subscribe(SubscriptionGroup subscriptions, MethodHandles.Lookup lookup, Object target) {
        var registrations = scope().prepareManagedListeners(
            eventType, registrationList, genericType, lookup, target, subscriptions.owner()
        );
        return subscriptions.installAll(
            registrations, listener -> subscriptions.install(describe(listener.source()), listener.install())
        );
    }

    public boolean post(E event) {
        return scope().post(eventType.cast(event));
    }

    public E fire(E event) {
        post(event);
        return event;
    }

    private String describe(String source) {
        return eventType.getName() + " <- " + Objects.requireNonNull(source, "source");
    }

}

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

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.function.Consumer;

/** A synchronous legacy event type and its Forge bus scope. The original event instance is posted. */
@NullMarked
public final class LegacyEventChannel<E extends Event> {

    private final Class<E> eventType;
    private final EventBus bus;
    private final @Nullable Type genericType;
    private final @Nullable ListenerList registrationList;

    private LegacyEventChannel(
        Class<E> eventType, EventBus bus, @Nullable Type genericType, @Nullable ListenerList registrationList) {
        this.eventType = Objects.requireNonNull(eventType, "eventType");
        this.bus = Objects.requireNonNull(bus, "bus");
        this.genericType = genericType;
        this.registrationList = registrationList;
    }

    public static <E extends Event> LegacyEventChannel<E> of(Class<E> eventType) {
        return of(MinecraftForge.EVENT_BUS, eventType);
    }

    public static <E extends Event> LegacyEventChannel<E> of(EventBus bus, Class<E> eventType) {
        return new LegacyEventChannel<>(eventType, bus, null, null);
    }

    public Class<E> eventType() {
        return eventType;
    }

    public EventBus scope() {
        return bus;
    }

    /**
     * Limits registration to legacy generic events returning this exact Type object.
     *
     * @param type the selector returned by the event's generic getter
     */
    public LegacyEventChannel<E> withGenericType(Type type) {
        return new LegacyEventChannel<>(eventType, bus, Objects.requireNonNull(type, "type"), registrationList);
    }

    /**
     * Points registration at a known hand-written table, including for an abstract event class.
     *
     * @param list the table an instance uses when posted
     */
    public LegacyEventChannel<E> withListenerList(ListenerList list) {
        return new LegacyEventChannel<>(eventType, bus, genericType, Objects.requireNonNull(list, "list"));
    }

    public Subscription subscribe(SubscriptionGroup subscriptions, String source, Consumer<? super E> listener) {
        return subscribe(subscriptions, EventPriority.NORMAL, false, source, listener);
    }

    public Subscription subscribe(
        SubscriptionGroup subscriptions, EventPriority priority, boolean receiveCanceled, String source,
        Consumer<? super E> listener) {
        String description = eventType.getName() + " <- " + Objects.requireNonNull(source, "source");
        return subscriptions.install(
            description,
            () -> bus.addManagedListener(
                eventType, registrationList, priority, receiveCanceled, genericType, listener, subscriptions.owner(),
                description
            )
        );
    }

    public boolean post(E event) {
        return bus.post(eventType.cast(event));
    }

}

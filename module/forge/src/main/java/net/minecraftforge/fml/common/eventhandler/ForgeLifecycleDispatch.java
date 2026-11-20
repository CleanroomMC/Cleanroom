/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 */

package net.minecraftforge.fml.common.eventhandler;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.FMLSubscriberRegistry;
import com.google.common.eventbus.FMLSubscriberRegistry.Snapshot;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.bus.EventBus;
import net.minecraftforge.eventbus.api.event.RecordEvent;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/** Dispatches FML's Guava subscribers through Forge 7 in their original snapshot order. */
final class ForgeLifecycleDispatch {
    private static final String GROUP_PREFIX = "cleanroom:fml-lifecycle-" + UUID.randomUUID() + "-";
    private static final AtomicLong NEXT_GROUP = new AtomicLong();

    private final FMLThrowingEventBus facade;
    private final FMLSubscriberRegistry registry;
    private final ThreadLocal<ArrayDeque<Delivery>> pending = new ThreadLocal<>();
    private Route cached;

    ForgeLifecycleDispatch(FMLThrowingEventBus facade) {
        this.facade = facade;
        this.registry = new FMLSubscriberRegistry(facade);
    }

    synchronized void register(Object target) {
        registry.register(target);
    }

    synchronized void unregister(Object target) {
        try {
            registry.unregister(target);
        } finally {
            retireCached();
        }
    }

    void post(Object event) {
        Route route = acquire(event);
        if (route == null) {
            if (!(event instanceof DeadEvent)) {
                facade.post(new DeadEvent(facade, event));
            }
            return;
        }

        Delivery delivery = new Delivery(event, route);
        var queue = pending.get();
        if (queue != null) {
            queue.addLast(delivery);
            return;
        }

        queue = new ArrayDeque<>();
        pending.set(queue);
        try {
            do {
                try {
                    delivery.route.bus.post(delivery);
                } finally {
                    release(delivery.route);
                }
            } while ((delivery = queue.pollFirst()) != null);
        } finally {
            pending.remove();
            while ((delivery = queue.pollFirst()) != null) {
                release(delivery.route);
            }
        }
    }

    @Nullable
    private synchronized Route acquire(Object event) {
        Route route = cached;
        Snapshot previous = route == null ? Snapshot.EMPTY : route.subscribers;
        Snapshot selected = registry.snapshot(event, previous);
        if (selected.isEmpty()) {
            return null;
        }
        if (route == null || selected != previous) {
            route = new Route(selected);
            retireCached();
            cached = route;
        }
        // The lease starts at post(), including for deliveries still waiting in a reentrant queue.
        route.posts++;
        return route;
    }

    private void retireCached() {
        Route route = cached;
        if (route != null) {
            cached = null;
            route.retired = true;
            if (route.posts == 0) {
                route.scope.dispose();
            }
        }
    }

    private synchronized void release(Route route) {
        if (--route.posts == 0 && route.retired) {
            route.scope.dispose();
        }
    }

    private record Delivery(Object event, Route route) implements RecordEvent {}

    private static final class Route {
        private final Snapshot subscribers;
        private final BusGroup scope;
        private final EventBus<Delivery> bus;
        private int posts;
        private boolean retired;

        private Route(Snapshot subscribers) {
            this.subscribers = subscribers;
            scope = BusGroup.create(GROUP_PREFIX + NEXT_GROUP.getAndIncrement(), Delivery.class);
            try {
                bus = EventBus.create(scope, Delivery.class);
                subscribers.bind(Delivery::event, bus::addListener);
            } catch (RuntimeException | Error failure) {
                scope.dispose();
                throw failure;
            }
        }
    }
}

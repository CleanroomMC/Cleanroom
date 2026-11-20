/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.google.common.eventbus;

import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Exposes Guava's subscriber selection and invocation to FML's lifecycle dispatcher. */
@NullMarked
public final class FMLSubscriberRegistry {

    // Foundation delegates this package to the loader that defines Guava.
    private final SubscriberRegistry registry;

    public FMLSubscriberRegistry(EventBus bus) {
        registry = new SubscriberRegistry(bus);
    }

    public void register(Object target) {
        registry.register(target);
    }

    public void unregister(Object target) {
        registry.unregister(target);
    }

    public Snapshot snapshot(Object event, Snapshot previous) {
        var selected = registry.getSubscribers(event);
        var old = previous.subscribers;
        int matching = 0;
        while (selected.hasNext()) {
            Subscriber subscriber = selected.next();
            // Re-registration can produce an equal Subscriber with a different synchronization lock.
            if (matching == old.size() || old.get(matching) != subscriber) {
                var subscribers = new ArrayList<Subscriber>();
                for (int index = 0; index < matching; index++) {
                    subscribers.add(old.get(index));
                }
                subscribers.add(subscriber);
                selected.forEachRemaining(subscribers::add);
                return new Snapshot(subscribers);
            }
            matching++;
        }
        if (matching == old.size()) {
            return previous;
        }
        return matching == 0 ? Snapshot.EMPTY : new Snapshot(List.copyOf(old.subList(0, matching)));
    }

    public static final class Snapshot {

        public static final Snapshot EMPTY = new Snapshot(List.of());

        private final List<Subscriber> subscribers;

        private Snapshot(List<Subscriber> subscribers) {
            this.subscribers = subscribers;
        }

        public boolean isEmpty() {
            return subscribers.isEmpty();
        }

        public <E> void bind(Function<E, Object> unwrap, Consumer<Consumer<E>> register) {
            for (Subscriber subscriber : subscribers) {
                register.accept(event -> subscriber.dispatchEvent(unwrap.apply(event)));
            }
        }

    }

}

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
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.lang.invoke.MethodHandles;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/** Owns managed subscriptions for one mod. Closing the group revokes its registrations across scopes. */
@NullMarked
public final class SubscriptionGroup implements AutoCloseable {

    private final ModContainer owner;
    private final String ownerId;
    private final LinkedHashSet<Subscription> active = new LinkedHashSet<>();

    private long nextId;
    private boolean closed;

    /** Captures the owner from the current FML context. */
    public SubscriptionGroup() {
        this(ModEventContext.registrationOwner());
    }

    public SubscriptionGroup(ModContainer owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.ownerId = owner.getModId();
    }

    public ModContainer owner() {
        return owner;
    }

    /**
     * Registers legacy @SubscribeEvent callbacks as one batch in the specified Forge bus.
     *
     * @param bus the Forge scope receiving this registration
     * @param lookup the access granted for invoking subscriber methods
     * @param target an instance or a class declaring static subscribers
     */
    public Subscription registerLegacy(EventBus bus, MethodHandles.Lookup lookup, Object target) {
        Class<?> type = target instanceof Class<?> utility ? utility : target.getClass();
        return install(type.getName(), () -> bus.registerManaged(lookup, target, owner));
    }

    /**
     * Registers declared Forge 7 @SubscribeEvent callbacks with the access granted by the lookup.
     *
     * @param scope the Forge 7 bus group receiving the subscriptions
     * @param lookup the access granted for invoking subscriber methods
     * @param target an instance or a class declaring static subscribers
     */
    public List<Subscription> registerNative(BusGroup scope, MethodHandles.Lookup lookup, Object target) {
        return NativeSubscribers.register(this, scope, lookup, target);
    }

    /** Removes each owned registration and can be repeated to retry a failed removal. */
    @Override
    public void close() {
        List<Subscription> subscriptions;
        synchronized (active) {
            closed = true;
            subscriptions = List.copyOf(active);
        }
        RuntimeException failure = null;
        for (Subscription subscription : subscriptions.reversed()) {
            try {
                subscription.close();
            } catch (RuntimeException error) {
                if (failure == null) {
                    failure = error;
                } else if (failure != error) {
                    failure.addSuppressed(error);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    String ownerId() {
        return ownerId;
    }

    Subscription install(String source, Supplier<Runnable> registration) {
        Objects.requireNonNull(source, "source");
        synchronized (active) {
            requireOpen();
        }
        Runnable remove = registration.get();
        Subscription subscription;
        synchronized (active) {
            if (!closed) {
                subscription = new Subscription(++nextId, this, source, remove);
                active.add(subscription);
                FMLLog.log.debug(
                    "Event subscription {} for {} from {}", subscription.id(), subscription.ownerId(), source
                );
                return subscription;
            }
        }
        // An installation that raced with close must retire its token before returning.
        remove.run();
        throw new IllegalStateException("Event subscriptions for " + ownerId + " are closed");
    }

    void forget(Subscription subscription) {
        synchronized (active) {
            active.remove(subscription);
        }
    }

    private void requireOpen() {
        if (closed) {
            throw new IllegalStateException("Event subscriptions for " + ownerId + " are closed");
        }
    }

}

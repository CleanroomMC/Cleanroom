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

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/** Owns managed subscriptions for one mod. Closing the group revokes its registrations across scopes. */
@NullMarked
public final class SubscriptionGroup implements AutoCloseable {

    private final ModContainer owner;
    private final String ownerId;
    private final LinkedHashSet<Subscription> active = new LinkedHashSet<>();

    private long nextId;
    private boolean closed;

    /** Captures the active FML registration owner as the group's owner. */
    public SubscriptionGroup() {
        this(ModEventContext.registrationOwner());
    }

    public SubscriptionGroup(ModContainer owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.ownerId = owner.getModId();
    }

    static String callbackSource(Object listener) {
        return Objects.requireNonNull(listener, "listener").getClass().getName();
    }

    public ModContainer owner() {
        return owner;
    }

    /** Registers legacy @SubscribeEvent callbacks as one batch in the specified Forge bus. */
    public Subscription registerLegacy(EventBus bus, MethodHandles.Lookup lookup, Object target) {
        Class<?> type = target instanceof Class<?> utility ? utility : target.getClass();
        return install(type.getName(), () -> bus.registerManaged(lookup, target, owner));
    }

    /** Removes each owned registration and can be repeated to retry a failed removal. */
    @Override
    public void close() {
        List<Subscription> subscriptions;
        synchronized (active) {
            closed = true;
            subscriptions = List.copyOf(active);
        }
        Throwable failure = null;
        for (Subscription subscription : subscriptions.reversed()) {
            try {
                subscription.close();
            } catch (RuntimeException | Error error) {
                if (failure == null) {
                    failure = error;
                } else if (failure != error) {
                    failure.addSuppressed(error);
                }
            }
        }
        if (failure instanceof RuntimeException runtimeFailure) {
            throw runtimeFailure;
        }
        if (failure instanceof Error error) {
            throw error;
        }
    }

    String ownerId() {
        return ownerId;
    }

    <T> List<Subscription> installAll(List<T> registrations, Function<T, Subscription> install) {
        requireOpen();
        var installed = new ArrayList<Subscription>(registrations.size());
        try {
            for (T registration : registrations) {
                installed.add(install.apply(registration));
            }
            return List.copyOf(installed);
        } catch (RuntimeException | Error failure) {
            for (Subscription subscription : installed.reversed()) {
                try {
                    subscription.close();
                } catch (RuntimeException | Error cleanup) {
                    if (failure != cleanup) {
                        failure.addSuppressed(cleanup);
                    }
                }
            }
            throw failure;
        }
    }

    Subscription install(String source, Supplier<Runnable> registration) {
        Objects.requireNonNull(source, "source");
        requireOpen();
        Runnable remove = registration.get();
        Subscription subscription;
        synchronized (active) {
            subscription = new Subscription(++nextId, this, source, remove);
            active.add(subscription);
            if (!closed) {
                FMLLog.log.debug(
                    "Event subscription {} for {} from {}", subscription.id(), subscription.ownerId(), source
                );
                return subscription;
            }
        }
        // Track the token until removal succeeds, including when installation races with close.
        subscription.close();
        throw new IllegalStateException("Event subscriptions for " + ownerId + " are closed");
    }

    void forget(Subscription subscription) {
        synchronized (active) {
            active.remove(subscription);
        }
    }

    private void requireOpen() {
        synchronized (active) {
            if (closed) {
                throw new IllegalStateException("Event subscriptions for " + ownerId + " are closed");
            }
        }
    }

}

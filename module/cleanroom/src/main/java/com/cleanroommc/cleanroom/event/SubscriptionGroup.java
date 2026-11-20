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

import com.cleanroommc.hidefromuserdev.HideFromUserdev;

import org.jspecify.annotations.NullMarked;

import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.eventhandler.ModEventContext;

import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Owns managed event subscriptions for a single {@link ModContainer} across Forge 7 and legacy scopes.
 * <p>
 * The owning mod container is captured when the group is created. Closing the group revokes every active
 * {@link Subscription} it owns in reverse registration order without disposing external
 * {@link net.minecraftforge.eventbus.api.bus.BusGroup} scopes.
 */
@NullMarked
public final class SubscriptionGroup implements AutoCloseable {

    // Framework-managed groups, one per mod container. Identity, because containers do not define equality.
    private static final Map<ModContainer, SubscriptionGroup> DEFAULTS = new IdentityHashMap<>();

    private final ModContainer owner;
    private final String ownerId;
    private final boolean managed;
    private final LinkedHashSet<Subscription> active = new LinkedHashSet<>();

    private long nextId;
    private boolean closed;

    /** Captures the active FML registration owner as the group's owner. */
    public SubscriptionGroup() {
        this(ModEventContext.registrationOwner(), false);
    }

    /**
     * Creates a subscription group owned by the given {@link ModContainer}.
     *
     * @param owner the mod container credited with registrations in this group
     */
    public SubscriptionGroup(ModContainer owner) {
        this(owner, false);
    }

    private SubscriptionGroup(ModContainer owner, boolean managed) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.ownerId = owner.getModId();
        this.managed = managed;
    }

    /**
     * Returns the framework-managed group of the current registration owner, creating it on first use.
     * <p>
     * This is the group used by the channel methods that omit a {@link SubscriptionGroup}. FML closes it when the
     * owning mod is disabled, so listeners registered this way live as long as the mod. Mods may close individual
     * {@link Subscription} handles earlier; calling {@link #close()} directly on a framework-managed default group is
     * rejected.
     *
     * @return the default group of the active FML registration owner
     */
    public static SubscriptionGroup current() {
        return defaultGroup(ModEventContext.registrationOwner());
    }

    /**
     * Returns the framework-managed group of {@code owner}, creating it on first use.
     *
     * @param owner the mod container that owns the group
     * @return the default group of {@code owner}
     */
    public static SubscriptionGroup defaultGroup(ModContainer owner) {
        Objects.requireNonNull(owner, "owner");
        synchronized (DEFAULTS) {
            return DEFAULTS.computeIfAbsent(owner, container -> new SubscriptionGroup(container, true));
        }
    }

    /**
     * Closes and forgets the framework-managed group of {@code owner}. Called by FML when a mod is disabled, and
     * harmless when that mod never used the default group.
     *
     * @param owner the mod container whose default group is retired
     */
    @HideFromUserdev
    public static void closeDefaultGroup(ModContainer owner) {
        Objects.requireNonNull(owner, "owner");
        SubscriptionGroup group;
        synchronized (DEFAULTS) {
            group = DEFAULTS.remove(owner);
        }
        if (group != null) {
            group.closeInternal();
        }
    }

    /**
     * Registers static Forge 7 {@code @SubscribeEvent} handlers (targeting either Forge 7 or legacy events) on a
     * mod-discovered class into {@code owner}'s default group.
     *
     * @param owner the owning mod container
     * @param lookup the access capability for {@code target}
     * @param target the discovered class
     */
    @HideFromUserdev
    public static void registerDiscovered(ModContainer owner, MethodHandles.Lookup lookup, Class<?> target) {
        ChannelSubscribers.registerDiscovered(defaultGroup(owner), lookup, target);
    }

    static String callbackSource(Object listener) {
        return Objects.requireNonNull(listener, "listener").getClass().getName();
    }

    /**
     * Returns the mod container that owns all subscriptions in this group.
     *
     * @return the owning {@link ModContainer}
     */
    public ModContainer owner() {
        return owner;
    }

    /**
     * Registers all discovered subscribers on {@code target} across both Forge 7 ({@link
     * net.minecraftforge.eventbus.api.bus.BusGroup#DEFAULT}) and legacy
     * ({@link net.minecraftforge.common.MinecraftForge#EVENT_BUS}) into this group as one atomic batch.
     * <p>
     * Declared Forge 7 {@link net.minecraftforge.eventbus.api.listener.SubscribeEvent} methods match both Forge 7
     * events and legacy {@link net.minecraftforge.fml.common.eventhandler.Event} subclasses, and legacy
     * {@link net.minecraftforge.fml.common.eventhandler.SubscribeEvent} methods match legacy events as in
     * {@link LegacyEventChannel}.
     *
     * @param lookup the access granted for invoking subscriber methods on {@code target}
     * @param target an instance or a {@link Class} declaring static subscribers
     * @return an immutable list containing one {@link Subscription} handle per registered method
     * @throws IllegalArgumentException if no valid subscribers exist, a method signature is invalid, or {@code lookup}
     * cannot access a subscriber method
     * @throws IllegalStateException if this group is already closed
     */
    public List<Subscription> register(MethodHandles.Lookup lookup, Object target) {
        return ChannelSubscribers.registerUnified(this, lookup, target);
    }

    /**
     * Registers legacy {@link net.minecraftforge.fml.common.eventhandler.SubscribeEvent} callbacks as one batch in the
     * specified Forge bus.
     * <p>
     * Declared private subscriber methods are unreflected using {@code lookup}, whereas private methods declared by
     * superclasses are skipped.
     *
     * @param bus the Forge scope receiving this registration
     * @param lookup the access granted for invoking subscriber methods
     * @param target an instance or a class declaring static subscribers
     * @return a single {@link Subscription} handle that removes the entire registered batch on close
     * @throws IllegalArgumentException if no valid legacy subscribers exist or a subscriber cannot be prepared
     * @throws IllegalStateException if this group is already closed
     */
    public Subscription registerLegacy(EventBus bus, MethodHandles.Lookup lookup, Object target) {
        Class<?> type = target instanceof Class<?> utility ? utility : target.getClass();
        return install(type.getName(), () -> bus.registerManaged(lookup, target, owner));
    }

    /**
     * Marks this group closed and removes each remaining owned registration in reverse installation order.
     * Calling this method repeatedly retries any registrations whose earlier removal failed; multiple failures are
     * preserved as suppressed exceptions on the first thrown error.
     *
     * @throws UnsupportedOperationException if called directly on a framework-managed default group from
     * {@link #current()} or {@link #defaultGroup(ModContainer)}
     */
    @Override
    public void close() {
        if (managed) {
            throw new UnsupportedOperationException(
                "Cannot directly close a framework-managed default SubscriptionGroup; close individual Subscription handles instead"
            );
        }
        closeInternal();
    }

    /**
     * Tracks a removal prepared elsewhere in this group, so closing the group also revokes it.
     *
     * @param source a human-readable label identifying the registration site
     * @param removal the removal action for the already installed listeners
     * @return the subscription handle used to unregister the listeners
     * @throws IllegalStateException if this group is already closed
     */
    public Subscription track(String source, Runnable removal) {
        Objects.requireNonNull(removal, "removal");
        return install(source, () -> removal);
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

    private void closeInternal() {
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

    private void requireOpen() {
        synchronized (active) {
            if (closed) {
                throw new IllegalStateException("Event subscriptions for " + ownerId + " are closed");
            }
        }
    }

}

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

/**
 * An idempotent handle to one managed listener registration or one legacy annotation batch from
 * {@link SubscriptionGroup#registerLegacy}.
 */
@NullMarked
public final class Subscription implements AutoCloseable {

    private final long id;
    private final String ownerId;
    private final String source;

    private @Nullable SubscriptionGroup group;
    private @Nullable Runnable remove;

    Subscription(long id, SubscriptionGroup group, String source, Runnable remove) {
        this.id = id;
        this.ownerId = group.ownerId();
        this.source = source;
        this.group = group;
        this.remove = remove;
    }

    /**
     * Returns the per-group sequential identifier assigned when this subscription was installed.
     *
     * @return the subscription identifier
     */
    public long id() {
        return id;
    }

    /**
     * Returns the mod identifier of the owning {@link SubscriptionGroup}.
     *
     * @return the owning mod identifier
     */
    public String ownerId() {
        return ownerId;
    }

    /**
     * Returns the diagnostic label describing the registered event and listener source.
     *
     * @return the source description
     */
    public String source() {
        return source;
    }

    /**
     * Returns whether this subscription has been retired.
     *
     * @return {@code true} after {@link #close()} completes
     */
    public synchronized boolean isClosed() {
        return remove == null;
    }

    /**
     * Retires this registration and detaches it from its owning {@link SubscriptionGroup}. Calling {@code close()}
     * more than once has no effect; a dispatch already in progress may still include this listener.
     */
    @Override
    public synchronized void close() {
        if (remove != null) {
            remove.run();
            remove = null;
            if (group != null) {
                group.forget(this);
                group = null;
            }
        }
    }

}

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

/** A handle to one registration or one legacy annotation batch. */
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

    public long id() {
        return id;
    }

    public String ownerId() {
        return ownerId;
    }

    public String source() {
        return source;
    }

    public synchronized boolean isClosed() {
        return remove == null;
    }

    /** Retires this registration. A dispatch already in progress may still include it. */
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

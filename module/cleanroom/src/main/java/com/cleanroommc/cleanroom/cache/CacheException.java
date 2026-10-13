/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.cache;

import org.jspecify.annotations.Nullable;

import java.io.IOException;

/**
 * A cache failure that callers can tell apart by {@link #kind()}.
 * Other I/O failures surface as plain {@link IOException}s.
 */
public final class CacheException extends IOException {

    private final Kind kind;

    CacheException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    CacheException(Kind kind, String message, @Nullable Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

    public enum Kind {

        /**
         * The source could not be reached and no usable entry is cached.
         */
        OFFLINE,
        /**
         * The content does not match the expected or pinned SHA-256.
         */
        HASH_MISMATCH,
        /**
         * The server answered with a status other than 200 or 304.
         */
        HTTP,
        /**
         * A symbolic link sits on the cache path.
         */
        UNSAFE_PATH

    }

}

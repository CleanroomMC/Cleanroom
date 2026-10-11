package com.cleanroommc.cleanroom.cache;

import java.io.IOException;

public final class CacheException extends IOException {

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

    private final Kind kind;

    CacheException(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    CacheException(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }

}

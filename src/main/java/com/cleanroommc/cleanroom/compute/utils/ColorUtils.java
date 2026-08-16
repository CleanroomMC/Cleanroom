package com.cleanroommc.cleanroom.compute.utils;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

import java.nio.IntBuffer;

public record ColorUtils() {
    public static void convertUColor(byte r, byte g, byte b, byte a, @NonNull IntBuffer buffer) {
        Preconditions.checkNotNull(buffer);
        Preconditions.checkArgument(buffer.remaining() >= 4);

        buffer.put(mapByteToInt(r));
        buffer.put(mapByteToInt(g));
        buffer.put(mapByteToInt(b));
        buffer.put(mapByteToInt(a));
    }

    private static int mapByteToInt(byte x) {
        return (x << 24) + (x << 16) + (x << 8) + x;
    }
}

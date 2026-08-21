package com.cleanroommc.util.patch;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Decodes COPY/INSERT binary deltas produced by CleanroomGradle's {@code GenerateBinPatches} task.
 */
final class BinDelta {

    private static final int COPY = 0;
    private static final int INSERT = 1;

    static byte[] decode(byte[] original, byte[] delta) {
        ByteArrayInputStream input = new ByteArrayInputStream(delta);
        ByteArrayOutputStream output = new ByteArrayOutputStream(original.length + 64);
        int tag;
        while ((tag = input.read()) != -1) {
            switch (tag) {
                case COPY -> {
                    int offset = readVarInt(input);
                    int length = readVarInt(input);
                    if (offset > original.length || length > original.length - offset) {
                        throw corrupt("COPY range is outside the original buffer");
                    }
                    output.write(original, offset, length);
                }
                case INSERT -> {
                    int length = readVarInt(input);
                    if (length > input.available()) {
                        throw corrupt("INSERT length exceeds the remaining delta");
                    }
                    output.write(delta, delta.length - input.available(), length);
                    input.skip(length);
                }
                default -> throw corrupt("Unknown operation tag " + tag);
            }
        }
        return output.toByteArray();
    }

    private static int readVarInt(ByteArrayInputStream input) {
        int value = 0;
        for (int byteIndex = 0; byteIndex < 5; byteIndex++) {
            int next = input.read();
            if (next == -1) {
                throw corrupt("Truncated VarInt");
            }
            if (byteIndex == 4 && (next & 0xF8) != 0) {
                throw corrupt("VarInt exceeds signed int range");
            }
            value |= (next & 0x7F) << (byteIndex * 7);
            if ((next & 0x80) == 0) {
                return value;
            }
        }
        throw corrupt("VarInt is too long");
    }

    private static IllegalArgumentException corrupt(String detail) {
        return new IllegalArgumentException("Corrupt delta: " + detail);
    }

    private BinDelta() { }

}

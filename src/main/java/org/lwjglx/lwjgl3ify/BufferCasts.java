package org.lwjglx.lwjgl3ify;

import java.nio.*;

import org.lwjgl.system.MemoryUtil;

public class BufferCasts {

    public static ByteBuffer toByteBuffer(CharBuffer buffer) {
        return MemoryUtil.memByteBuffer(buffer);
    }

    public static ByteBuffer toByteBuffer(ShortBuffer buffer) {
        return MemoryUtil.memByteBuffer(buffer);
    }

    public static ByteBuffer toByteBuffer(IntBuffer buffer) {
        return MemoryUtil.memByteBuffer(buffer);
    }

    public static ByteBuffer toByteBuffer(LongBuffer buffer) {
        return MemoryUtil.memByteBuffer(buffer);
    }

    public static ByteBuffer toByteBuffer(FloatBuffer buffer) {
        return MemoryUtil.memByteBuffer(buffer);
    }

    public static ByteBuffer toByteBuffer(DoubleBuffer buffer) {
        return MemoryUtil.memByteBuffer(buffer);
    }

    public static void updateBuffer(CharBuffer destination, ByteBuffer source) {}

    public static void updateBuffer(ShortBuffer destination, ByteBuffer source) {}

    public static void updateBuffer(IntBuffer destination, ByteBuffer source) {}

    public static void updateBuffer(LongBuffer destination, ByteBuffer source) {}

    public static void updateBuffer(FloatBuffer destination, ByteBuffer source) {}

    public static void updateBuffer(DoubleBuffer destination, ByteBuffer source) {}

    public static CharSequence bufferToCharSeq(ByteBuffer buffer) {
        return MemoryUtil.memUTF8(buffer);
    }
}

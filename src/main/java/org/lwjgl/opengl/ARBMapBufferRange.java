package org.lwjgl.opengl;

public class ARBMapBufferRange {

    public static final int GL_MAP_FLUSH_EXPLICIT_BIT = (int) 16;
    public static final int GL_MAP_INVALIDATE_BUFFER_BIT = (int) 8;
    public static final int GL_MAP_INVALIDATE_RANGE_BIT = (int) 4;
    public static final int GL_MAP_READ_BIT = (int) 1;
    public static final int GL_MAP_UNSYNCHRONIZED_BIT = (int) 32;
    public static final int GL_MAP_WRITE_BIT = (int) 2;

    public static void glFlushMappedBufferRange(int target, long offset, long length) {
        org.lwjgl3.opengl.ARBMapBufferRange.glFlushMappedBufferRange(target, offset, length);
    }

    public static java.nio.ByteBuffer glMapBufferRange(int target, long offset, long length, int access,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl3.opengl.ARBMapBufferRange.glMapBufferRange(target, offset, length, access, old_buffer);
    }
}

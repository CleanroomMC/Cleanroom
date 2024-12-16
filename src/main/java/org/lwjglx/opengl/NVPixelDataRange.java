package org.lwjglx.opengl;

import org.lwjglx.lwjgl3ify.BufferCasts;

public class NVPixelDataRange {

    public static final int GL_READ_PIXEL_DATA_RANGE_LENGTH_NV = (int) 34939;
    public static final int GL_READ_PIXEL_DATA_RANGE_NV = (int) 34937;
    public static final int GL_READ_PIXEL_DATA_RANGE_POINTER_NV = (int) 34941;
    public static final int GL_WRITE_PIXEL_DATA_RANGE_LENGTH_NV = (int) 34938;
    public static final int GL_WRITE_PIXEL_DATA_RANGE_NV = (int) 34936;
    public static final int GL_WRITE_PIXEL_DATA_RANGE_POINTER_NV = (int) 34940;

    public static void glFlushPixelDataRangeNV(int target) {
        org.lwjgl.opengl.NVPixelDataRange.glFlushPixelDataRangeNV(target);
    }

    public static void glPixelDataRangeNV(int target, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.NVPixelDataRange.glPixelDataRangeNV(target, data);
    }

    public static void glPixelDataRangeNV(int target, java.nio.DoubleBuffer data) {
        final java.nio.ByteBuffer wrappedArg1 = BufferCasts.toByteBuffer(data);

        org.lwjgl.opengl.NVPixelDataRange.glPixelDataRangeNV(target, wrappedArg1);
        BufferCasts.updateBuffer(data, wrappedArg1);
    }

    public static void glPixelDataRangeNV(int target, java.nio.FloatBuffer data) {
        final java.nio.ByteBuffer wrappedArg1 = BufferCasts.toByteBuffer(data);

        org.lwjgl.opengl.NVPixelDataRange.glPixelDataRangeNV(target, wrappedArg1);
        BufferCasts.updateBuffer(data, wrappedArg1);
    }

    public static void glPixelDataRangeNV(int target, java.nio.IntBuffer data) {
        final java.nio.ByteBuffer wrappedArg1 = BufferCasts.toByteBuffer(data);

        org.lwjgl.opengl.NVPixelDataRange.glPixelDataRangeNV(target, wrappedArg1);
        BufferCasts.updateBuffer(data, wrappedArg1);
    }

    public static void glPixelDataRangeNV(int target, java.nio.ShortBuffer data) {
        final java.nio.ByteBuffer wrappedArg1 = BufferCasts.toByteBuffer(data);

        org.lwjgl.opengl.NVPixelDataRange.glPixelDataRangeNV(target, wrappedArg1);
        BufferCasts.updateBuffer(data, wrappedArg1);
    }
}

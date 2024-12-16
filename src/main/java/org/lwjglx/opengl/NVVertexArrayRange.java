package org.lwjglx.opengl;

import org.lwjglx.lwjgl3ify.BufferCasts;

public class NVVertexArrayRange {

    public static final int GL_MAX_VERTEX_ARRAY_RANGE_ELEMENT_NV = (int) 34080;
    public static final int GL_VERTEX_ARRAY_RANGE_LENGTH_NV = (int) 34078;
    public static final int GL_VERTEX_ARRAY_RANGE_NV = (int) 34077;
    public static final int GL_VERTEX_ARRAY_RANGE_POINTER_NV = (int) 34081;
    public static final int GL_VERTEX_ARRAY_RANGE_VALID_NV = (int) 34079;

    public static void glFlushVertexArrayRangeNV() {
        org.lwjgl.opengl.NVVertexArrayRange.glFlushVertexArrayRangeNV();
    }

    public static void glVertexArrayRangeNV(java.nio.ByteBuffer pPointer) {
        org.lwjgl.opengl.NVVertexArrayRange.glVertexArrayRangeNV(pPointer);
    }

    public static void glVertexArrayRangeNV(java.nio.DoubleBuffer pPointer) {
        final java.nio.ByteBuffer wrappedArg0 = BufferCasts.toByteBuffer(pPointer);

        org.lwjgl.opengl.NVVertexArrayRange.glVertexArrayRangeNV(wrappedArg0);
        BufferCasts.updateBuffer(pPointer, wrappedArg0);
    }

    public static void glVertexArrayRangeNV(java.nio.FloatBuffer pPointer) {
        final java.nio.ByteBuffer wrappedArg0 = BufferCasts.toByteBuffer(pPointer);

        org.lwjgl.opengl.NVVertexArrayRange.glVertexArrayRangeNV(wrappedArg0);
        BufferCasts.updateBuffer(pPointer, wrappedArg0);
    }

    public static void glVertexArrayRangeNV(java.nio.IntBuffer pPointer) {
        final java.nio.ByteBuffer wrappedArg0 = BufferCasts.toByteBuffer(pPointer);

        org.lwjgl.opengl.NVVertexArrayRange.glVertexArrayRangeNV(wrappedArg0);
        BufferCasts.updateBuffer(pPointer, wrappedArg0);
    }

    public static void glVertexArrayRangeNV(java.nio.ShortBuffer pPointer) {
        final java.nio.ByteBuffer wrappedArg0 = BufferCasts.toByteBuffer(pPointer);

        org.lwjgl.opengl.NVVertexArrayRange.glVertexArrayRangeNV(wrappedArg0);
        BufferCasts.updateBuffer(pPointer, wrappedArg0);
    }
}

package org.lwjglx.opengl;

public class NVVertexAttribInteger64bit {

    public static final int GL_INT64_NV = (int) 5134;
    public static final int GL_UNSIGNED_INT64_NV = (int) 5135;

    public static void glGetVertexAttribLNV(int index, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glGetVertexAttribLi64vNV(index, pname, params);
    }

    public static void glGetVertexAttribLuNV(int index, int pname, java.nio.LongBuffer params) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glGetVertexAttribLui64vNV(index, pname, params);
    }

    public static void glVertexAttribL1NV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL1i64vNV(index, v);
    }

    public static void glVertexAttribL1i64NV(int index, long x) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL1i64NV(index, x);
    }

    public static void glVertexAttribL1uNV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL1ui64vNV(index, v);
    }

    public static void glVertexAttribL1ui64NV(int index, long x) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL1ui64NV(index, x);
    }

    public static void glVertexAttribL2NV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL2i64vNV(index, v);
    }

    public static void glVertexAttribL2i64NV(int index, long x, long y) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL2i64NV(index, x, y);
    }

    public static void glVertexAttribL2uNV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL2ui64vNV(index, v);
    }

    public static void glVertexAttribL2ui64NV(int index, long x, long y) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL2ui64NV(index, x, y);
    }

    public static void glVertexAttribL3NV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL3i64vNV(index, v);
    }

    public static void glVertexAttribL3i64NV(int index, long x, long y, long z) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL3i64NV(index, x, y, z);
    }

    public static void glVertexAttribL3uNV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL3ui64vNV(index, v);
    }

    public static void glVertexAttribL3ui64NV(int index, long x, long y, long z) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL3ui64NV(index, x, y, z);
    }

    public static void glVertexAttribL4NV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL4i64vNV(index, v);
    }

    public static void glVertexAttribL4i64NV(int index, long x, long y, long z, long w) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL4i64NV(index, x, y, z, w);
    }

    public static void glVertexAttribL4uNV(int index, java.nio.LongBuffer v) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL4ui64vNV(index, v);
    }

    public static void glVertexAttribL4ui64NV(int index, long x, long y, long z, long w) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribL4ui64NV(index, x, y, z, w);
    }

    public static void glVertexAttribLFormatNV(int index, int size, int type, int stride) {
        org.lwjgl.opengl.NVVertexAttribInteger64bit.glVertexAttribLFormatNV(index, size, type, stride);
    }
}

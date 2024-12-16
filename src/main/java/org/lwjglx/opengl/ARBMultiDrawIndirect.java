package org.lwjglx.opengl;

public class ARBMultiDrawIndirect {

    public static void glMultiDrawArraysIndirect(int mode, long indirect_buffer_offset, int primcount, int stride) {
        org.lwjgl.opengl.ARBMultiDrawIndirect
                .glMultiDrawArraysIndirect(mode, indirect_buffer_offset, primcount, stride);
    }

    public static void glMultiDrawArraysIndirect(int mode, java.nio.ByteBuffer indirect, int primcount, int stride) {
        org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawArraysIndirect(mode, indirect, primcount, stride);
    }

    public static void glMultiDrawArraysIndirect(int mode, java.nio.IntBuffer indirect, int primcount, int stride) {
        org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawArraysIndirect(mode, indirect, primcount, stride);
    }

    public static void glMultiDrawElementsIndirect(int mode, int type, long indirect_buffer_offset, int primcount,
            int stride) {
        org.lwjgl.opengl.ARBMultiDrawIndirect
                .glMultiDrawElementsIndirect(mode, type, indirect_buffer_offset, primcount, stride);
    }

    public static void glMultiDrawElementsIndirect(int mode, int type, java.nio.ByteBuffer indirect, int primcount,
            int stride) {
        org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawElementsIndirect(mode, type, indirect, primcount, stride);
    }

    public static void glMultiDrawElementsIndirect(int mode, int type, java.nio.IntBuffer indirect, int primcount,
            int stride) {
        org.lwjgl.opengl.ARBMultiDrawIndirect.glMultiDrawElementsIndirect(mode, type, indirect, primcount, stride);
    }
}

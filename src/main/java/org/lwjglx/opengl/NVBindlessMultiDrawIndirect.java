package org.lwjglx.opengl;

public class NVBindlessMultiDrawIndirect {

    public static void glMultiDrawArraysIndirectBindlessNV(int mode, long indirect_buffer_offset, int drawCount,
            int stride, int vertexBufferCount) {
        org.lwjgl.opengl.NVBindlessMultiDrawIndirect.glMultiDrawArraysIndirectBindlessNV(
                mode,
                indirect_buffer_offset,
                drawCount,
                stride,
                vertexBufferCount);
    }

    public static void glMultiDrawArraysIndirectBindlessNV(int mode, java.nio.ByteBuffer indirect, int drawCount,
            int stride, int vertexBufferCount) {
        org.lwjgl.opengl.NVBindlessMultiDrawIndirect
                .glMultiDrawArraysIndirectBindlessNV(mode, indirect, drawCount, stride, vertexBufferCount);
    }

    public static void glMultiDrawElementsIndirectBindlessNV(int mode, int type, long indirect_buffer_offset,
            int drawCount, int stride, int vertexBufferCount) {
        org.lwjgl.opengl.NVBindlessMultiDrawIndirect.glMultiDrawElementsIndirectBindlessNV(
                mode,
                type,
                indirect_buffer_offset,
                drawCount,
                stride,
                vertexBufferCount);
    }

    public static void glMultiDrawElementsIndirectBindlessNV(int mode, int type, java.nio.ByteBuffer indirect,
            int drawCount, int stride, int vertexBufferCount) {
        org.lwjgl.opengl.NVBindlessMultiDrawIndirect
                .glMultiDrawElementsIndirectBindlessNV(mode, type, indirect, drawCount, stride, vertexBufferCount);
    }
}

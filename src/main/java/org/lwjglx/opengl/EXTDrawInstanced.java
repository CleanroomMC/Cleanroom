package org.lwjglx.opengl;

public class EXTDrawInstanced {

    public static void glDrawArraysInstancedEXT(int mode, int first, int count, int primcount) {
        org.lwjgl.opengl.EXTDrawInstanced.glDrawArraysInstancedEXT(mode, first, count, primcount);
    }

    public static void glDrawElementsInstancedEXT(int mode, int indices_count, int type, long indices_buffer_offset,
            int primcount) {
        org.lwjgl.opengl.EXTDrawInstanced
                .glDrawElementsInstancedEXT(mode, indices_count, type, indices_buffer_offset, primcount);
    }

    public static void glDrawElementsInstancedEXT(int mode, java.nio.ByteBuffer indices, int primcount) {
        org.lwjgl.opengl.EXTDrawInstanced.glDrawElementsInstancedEXT(mode, indices, primcount);
    }

    public static void glDrawElementsInstancedEXT(int mode, java.nio.IntBuffer indices, int primcount) {
        org.lwjgl.opengl.EXTDrawInstanced.glDrawElementsInstancedEXT(mode, indices, primcount);
    }

    public static void glDrawElementsInstancedEXT(int mode, java.nio.ShortBuffer indices, int primcount) {
        org.lwjgl.opengl.EXTDrawInstanced.glDrawElementsInstancedEXT(mode, indices, primcount);
    }
}

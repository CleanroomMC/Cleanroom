package org.lwjglx.opengl;

public class ARBDrawInstanced {

    public static void glDrawArraysInstancedARB(int mode, int first, int count, int primcount) {
        org.lwjgl.opengl.ARBDrawInstanced.glDrawArraysInstancedARB(mode, first, count, primcount);
    }

    public static void glDrawElementsInstancedARB(int mode, int indices_count, int type, long indices_buffer_offset,
            int primcount) {
        org.lwjgl.opengl.ARBDrawInstanced
                .glDrawElementsInstancedARB(mode, indices_count, type, indices_buffer_offset, primcount);
    }

    public static void glDrawElementsInstancedARB(int mode, java.nio.ByteBuffer indices, int primcount) {
        org.lwjgl.opengl.ARBDrawInstanced.glDrawElementsInstancedARB(mode, indices, primcount);
    }

    public static void glDrawElementsInstancedARB(int mode, java.nio.IntBuffer indices, int primcount) {
        org.lwjgl.opengl.ARBDrawInstanced.glDrawElementsInstancedARB(mode, indices, primcount);
    }

    public static void glDrawElementsInstancedARB(int mode, java.nio.ShortBuffer indices, int primcount) {
        org.lwjgl.opengl.ARBDrawInstanced.glDrawElementsInstancedARB(mode, indices, primcount);
    }
}

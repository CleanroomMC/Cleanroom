package org.lwjglx.opengl;

public class ARBIndirectParameters {

    public static final int GL_PARAMETER_BUFFER_ARB = (int) 33006;
    public static final int GL_PARAMETER_BUFFER_BINDING_ARB = (int) 33007;

    public static void glMultiDrawArraysIndirectCountARB(int mode, long indirect_buffer_offset, long drawcount,
            int maxdrawcount, int stride) {
        org.lwjgl.opengl.ARBIndirectParameters
                .glMultiDrawArraysIndirectCountARB(mode, indirect_buffer_offset, drawcount, maxdrawcount, stride);
    }

    public static void glMultiDrawArraysIndirectCountARB(int mode, java.nio.ByteBuffer indirect, long drawcount,
            int maxdrawcount, int stride) {
        org.lwjgl.opengl.ARBIndirectParameters
                .glMultiDrawArraysIndirectCountARB(mode, indirect, drawcount, maxdrawcount, stride);
    }

    public static void glMultiDrawArraysIndirectCountARB(int mode, java.nio.IntBuffer indirect, long drawcount,
            int maxdrawcount, int stride) {
        org.lwjgl.opengl.ARBIndirectParameters
                .glMultiDrawArraysIndirectCountARB(mode, indirect, drawcount, maxdrawcount, stride);
    }

    public static void glMultiDrawElementsIndirectCountARB(int mode, int type, long indirect_buffer_offset,
            long drawcount, int maxdrawcount, int stride) {
        org.lwjgl.opengl.ARBIndirectParameters.glMultiDrawElementsIndirectCountARB(
                mode,
                type,
                indirect_buffer_offset,
                drawcount,
                maxdrawcount,
                stride);
    }

    public static void glMultiDrawElementsIndirectCountARB(int mode, int type, java.nio.ByteBuffer indirect,
            long drawcount, int maxdrawcount, int stride) {
        org.lwjgl.opengl.ARBIndirectParameters
                .glMultiDrawElementsIndirectCountARB(mode, type, indirect, drawcount, maxdrawcount, stride);
    }

    public static void glMultiDrawElementsIndirectCountARB(int mode, int type, java.nio.IntBuffer indirect,
            long drawcount, int maxdrawcount, int stride) {
        org.lwjgl.opengl.ARBIndirectParameters
                .glMultiDrawElementsIndirectCountARB(mode, type, indirect, drawcount, maxdrawcount, stride);
    }
}

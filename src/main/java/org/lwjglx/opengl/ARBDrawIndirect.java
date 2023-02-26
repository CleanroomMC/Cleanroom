package org.lwjglx.opengl;

public class ARBDrawIndirect {

    public static final int GL_DRAW_INDIRECT_BUFFER = (int) 36671;
    public static final int GL_DRAW_INDIRECT_BUFFER_BINDING = (int) 36675;

    public static void glDrawArraysIndirect(int mode, long indirect_buffer_offset) {
        org.lwjgl.opengl.ARBDrawIndirect.glDrawArraysIndirect(mode, indirect_buffer_offset);
    }

    public static void glDrawArraysIndirect(int mode, java.nio.ByteBuffer indirect) {
        org.lwjgl.opengl.ARBDrawIndirect.glDrawArraysIndirect(mode, indirect);
    }

    public static void glDrawArraysIndirect(int mode, java.nio.IntBuffer indirect) {
        org.lwjgl.opengl.ARBDrawIndirect.glDrawArraysIndirect(mode, indirect);
    }

    public static void glDrawElementsIndirect(int mode, int type, long indirect_buffer_offset) {
        org.lwjgl.opengl.ARBDrawIndirect.glDrawElementsIndirect(mode, type, indirect_buffer_offset);
    }

    public static void glDrawElementsIndirect(int mode, int type, java.nio.ByteBuffer indirect) {
        org.lwjgl.opengl.ARBDrawIndirect.glDrawElementsIndirect(mode, type, indirect);
    }

    public static void glDrawElementsIndirect(int mode, int type, java.nio.IntBuffer indirect) {
        org.lwjgl.opengl.ARBDrawIndirect.glDrawElementsIndirect(mode, type, indirect);
    }
}

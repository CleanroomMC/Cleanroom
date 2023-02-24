package org.lwjglx.opengl;

public class ARBDrawBuffers {

    public static final int GL_DRAW_BUFFER0_ARB = (int) 34853;
    public static final int GL_DRAW_BUFFER10_ARB = (int) 34863;
    public static final int GL_DRAW_BUFFER11_ARB = (int) 34864;
    public static final int GL_DRAW_BUFFER12_ARB = (int) 34865;
    public static final int GL_DRAW_BUFFER13_ARB = (int) 34866;
    public static final int GL_DRAW_BUFFER14_ARB = (int) 34867;
    public static final int GL_DRAW_BUFFER15_ARB = (int) 34868;
    public static final int GL_DRAW_BUFFER1_ARB = (int) 34854;
    public static final int GL_DRAW_BUFFER2_ARB = (int) 34855;
    public static final int GL_DRAW_BUFFER3_ARB = (int) 34856;
    public static final int GL_DRAW_BUFFER4_ARB = (int) 34857;
    public static final int GL_DRAW_BUFFER5_ARB = (int) 34858;
    public static final int GL_DRAW_BUFFER6_ARB = (int) 34859;
    public static final int GL_DRAW_BUFFER7_ARB = (int) 34860;
    public static final int GL_DRAW_BUFFER8_ARB = (int) 34861;
    public static final int GL_DRAW_BUFFER9_ARB = (int) 34862;
    public static final int GL_MAX_DRAW_BUFFERS_ARB = (int) 34852;

    public static void glDrawBuffersARB(int buffer) {

        org.lwjgl.opengl.ARBDrawBuffers.glDrawBuffersARB(new int[] { buffer });
    }

    public static void glDrawBuffersARB(java.nio.IntBuffer buffers) {
        org.lwjgl.opengl.ARBDrawBuffers.glDrawBuffersARB(buffers);
    }
}

package org.lwjglx.opengl;

public class ARBColorBufferFloat {

    public static final int GLX_RGBA_FLOAT_BIT = (int) 4;
    public static final int GLX_RGBA_FLOAT_TYPE = (int) 8377;
    public static final int GL_CLAMP_FRAGMENT_COLOR_ARB = (int) 35099;
    public static final int GL_CLAMP_READ_COLOR_ARB = (int) 35100;
    public static final int GL_CLAMP_VERTEX_COLOR_ARB = (int) 35098;
    public static final int GL_FIXED_ONLY_ARB = (int) 35101;
    public static final int GL_RGBA_FLOAT_MODE_ARB = (int) 34848;
    public static final int WGL_TYPE_RGBA_FLOAT_ARB = (int) 8608;

    public static void glClampColorARB(int target, int clamp) {
        org.lwjgl.opengl.ARBColorBufferFloat.glClampColorARB(target, clamp);
    }
}

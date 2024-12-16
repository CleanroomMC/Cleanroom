package org.lwjglx.opengl;

public class EXTBlendMinmax {

    public static final int GL_BLEND_EQUATION_EXT = (int) 32777;
    public static final int GL_FUNC_ADD_EXT = (int) 32774;
    public static final int GL_MAX_EXT = (int) 32776;
    public static final int GL_MIN_EXT = (int) 32775;

    public static void glBlendEquationEXT(int mode) {
        org.lwjgl.opengl.EXTBlendMinmax.glBlendEquationEXT(mode);
    }
}

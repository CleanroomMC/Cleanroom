package org.lwjglx.opengl;

public class EXTBlendEquationSeparate {

    public static final int GL_BLEND_EQUATION_ALPHA_EXT = (int) 34877;
    public static final int GL_BLEND_EQUATION_RGB_EXT = (int) 32777;

    public static void glBlendEquationSeparateEXT(int modeRGB, int modeAlpha) {
        org.lwjgl.opengl.EXTBlendEquationSeparate.glBlendEquationSeparateEXT(modeRGB, modeAlpha);
    }
}

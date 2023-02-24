package org.lwjglx.opengl;

public class EXTBlendColor {

    public static final int GL_BLEND_COLOR_EXT = (int) 32773;
    public static final int GL_CONSTANT_ALPHA_EXT = (int) 32771;
    public static final int GL_CONSTANT_COLOR_EXT = (int) 32769;
    public static final int GL_ONE_MINUS_CONSTANT_ALPHA_EXT = (int) 32772;
    public static final int GL_ONE_MINUS_CONSTANT_COLOR_EXT = (int) 32770;

    public static void glBlendColorEXT(float red, float green, float blue, float alpha) {
        org.lwjgl.opengl.EXTBlendColor.glBlendColorEXT(red, green, blue, alpha);
    }
}

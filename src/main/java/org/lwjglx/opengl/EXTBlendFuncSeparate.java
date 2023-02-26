package org.lwjglx.opengl;

public class EXTBlendFuncSeparate {

    public static final int GL_BLEND_DST_ALPHA_EXT = (int) 32970;
    public static final int GL_BLEND_DST_RGB_EXT = (int) 32968;
    public static final int GL_BLEND_SRC_ALPHA_EXT = (int) 32971;
    public static final int GL_BLEND_SRC_RGB_EXT = (int) 32969;

    public static void glBlendFuncSeparateEXT(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        org.lwjgl.opengl.EXTBlendFuncSeparate
                .glBlendFuncSeparateEXT(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }
}

package org.lwjglx.opengl;

public class EXTTextureArray {

    public static final int GL_COMPARE_REF_DEPTH_TO_TEXTURE_EXT = (int) 34894;
    public static final int GL_FRAMEBUFFER_ATTACHMENT_TEXTURE_LAYER_EXT = (int) 36052;
    public static final int GL_MAX_ARRAY_TEXTURE_LAYERS_EXT = (int) 35071;
    public static final int GL_PROXY_TEXTURE_1D_ARRAY_EXT = (int) 35865;
    public static final int GL_PROXY_TEXTURE_2D_ARRAY_EXT = (int) 35867;
    public static final int GL_SAMPLER_1D_ARRAY_EXT = (int) 36288;
    public static final int GL_SAMPLER_1D_ARRAY_SHADOW_EXT = (int) 36291;
    public static final int GL_SAMPLER_2D_ARRAY_EXT = (int) 36289;
    public static final int GL_SAMPLER_2D_ARRAY_SHADOW_EXT = (int) 36292;
    public static final int GL_TEXTURE_1D_ARRAY_EXT = (int) 35864;
    public static final int GL_TEXTURE_2D_ARRAY_EXT = (int) 35866;
    public static final int GL_TEXTURE_BINDING_1D_ARRAY_EXT = (int) 35868;
    public static final int GL_TEXTURE_BINDING_2D_ARRAY_EXT = (int) 35869;

    public static void glFramebufferTextureLayerEXT(int target, int attachment, int texture, int level, int layer) {
        org.lwjgl.opengl.EXTTextureArray.glFramebufferTextureLayerEXT(target, attachment, texture, level, layer);
    }
}

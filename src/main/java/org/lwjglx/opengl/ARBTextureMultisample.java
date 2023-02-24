package org.lwjglx.opengl;

public class ARBTextureMultisample {

    public static final int GL_INT_SAMPLER_2D_MULTISAMPLE = (int) 37129;
    public static final int GL_INT_SAMPLER_2D_MULTISAMPLE_ARRAY = (int) 37132;
    public static final int GL_MAX_COLOR_TEXTURE_SAMPLES = (int) 37134;
    public static final int GL_MAX_DEPTH_TEXTURE_SAMPLES = (int) 37135;
    public static final int GL_MAX_INTEGER_SAMPLES = (int) 37136;
    public static final int GL_MAX_SAMPLE_MASK_WORDS = (int) 36441;
    public static final int GL_PROXY_TEXTURE_2D_MULTISAMPLE = (int) 37121;
    public static final int GL_PROXY_TEXTURE_2D_MULTISAMPLE_ARRAY = (int) 37123;
    public static final int GL_SAMPLER_2D_MULTISAMPLE = (int) 37128;
    public static final int GL_SAMPLER_2D_MULTISAMPLE_ARRAY = (int) 37131;
    public static final int GL_SAMPLE_MASK = (int) 36433;
    public static final int GL_SAMPLE_MASK_VALUE = (int) 36434;
    public static final int GL_SAMPLE_POSITION = (int) 36432;
    public static final int GL_TEXTURE_2D_MULTISAMPLE = (int) 37120;
    public static final int GL_TEXTURE_2D_MULTISAMPLE_ARRAY = (int) 37122;
    public static final int GL_TEXTURE_BINDING_2D_MULTISAMPLE = (int) 37124;
    public static final int GL_TEXTURE_BINDING_2D_MULTISAMPLE_ARRAY = (int) 37125;
    public static final int GL_TEXTURE_FIXED_SAMPLE_LOCATIONS = (int) 37127;
    public static final int GL_TEXTURE_SAMPLES = (int) 37126;
    public static final int GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE = (int) 37130;
    public static final int GL_UNSIGNED_INT_SAMPLER_2D_MULTISAMPLE_ARRAY = (int) 37133;

    public static void glSampleMaski(int index, int mask) {
        org.lwjgl.opengl.ARBTextureMultisample.glSampleMaski(index, mask);
    }

    public static void glTexImage2DMultisample(int target, int samples, int internalformat, int width, int height,
            boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBTextureMultisample
                .glTexImage2DMultisample(target, samples, internalformat, width, height, fixedsamplelocations);
    }

    public static void glTexImage3DMultisample(int target, int samples, int internalformat, int width, int height,
            int depth, boolean fixedsamplelocations) {
        org.lwjgl.opengl.ARBTextureMultisample
                .glTexImage3DMultisample(target, samples, internalformat, width, height, depth, fixedsamplelocations);
    }
}

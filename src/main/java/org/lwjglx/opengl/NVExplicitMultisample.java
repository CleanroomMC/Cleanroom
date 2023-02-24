package org.lwjglx.opengl;

public class NVExplicitMultisample {

    public static final int GL_INT_SAMPLER_RENDERBUFFER_NV = (int) 36439;
    public static final int GL_MAX_SAMPLE_MASK_WORDS_NV = (int) 36441;
    public static final int GL_SAMPLER_RENDERBUFFER_NV = (int) 36438;
    public static final int GL_SAMPLE_MASK_NV = (int) 36433;
    public static final int GL_SAMPLE_MASK_VALUE_NV = (int) 36434;
    public static final int GL_SAMPLE_POSITION_NV = (int) 36432;
    public static final int GL_TEXTURE_BINDING_RENDERBUFFER_NV = (int) 36435;
    public static final int GL_TEXTURE_RENDERBUFFER_DATA_STORE_BINDING_NV = (int) 36436;
    public static final int GL_TEXTURE_RENDERBUFFER_NV = (int) 36437;
    public static final int GL_UNSIGNED_INT_SAMPLER_RENDERBUFFER_NV = (int) 36440;

    public static void glGetMultisampleNV(int pname, int index, java.nio.FloatBuffer val) {
        org.lwjgl.opengl.NVExplicitMultisample.glGetMultisamplefvNV(pname, index, val);
    }

    public static void glSampleMaskIndexedNV(int index, int mask) {
        org.lwjgl.opengl.NVExplicitMultisample.glSampleMaskIndexedNV(index, mask);
    }

    public static void glTexRenderbufferNV(int target, int renderbuffer) {
        org.lwjgl.opengl.NVExplicitMultisample.glTexRenderbufferNV(target, renderbuffer);
    }
}

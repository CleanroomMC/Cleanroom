package org.lwjgl.opengl;

public class ARBSamplerObjects {

    public static final int GL_SAMPLER_BINDING = (int) 35097;

    public static void glBindSampler(int unit, int sampler) {
        org.lwjgl3.opengl.ARBSamplerObjects.glBindSampler(unit, sampler);
    }

    public static void glDeleteSamplers(int sampler) {
        org.lwjgl3.opengl.ARBSamplerObjects.glDeleteSamplers(sampler);
    }

    public static void glDeleteSamplers(java.nio.IntBuffer samplers) {
        org.lwjgl3.opengl.ARBSamplerObjects.glDeleteSamplers(samplers);
    }

    public static int glGenSamplers() {
        return org.lwjgl3.opengl.ARBSamplerObjects.glGenSamplers();
    }

    public static void glGenSamplers(java.nio.IntBuffer samplers) {
        org.lwjgl3.opengl.ARBSamplerObjects.glGenSamplers(samplers);
    }

    public static int glGetSamplerParameterIi(int sampler, int pname) {
        return org.lwjgl3.opengl.ARBSamplerObjects.glGetSamplerParameterIi(sampler, pname);
    }

    public static int glGetSamplerParameterIui(int sampler, int pname) {
        return org.lwjgl3.opengl.ARBSamplerObjects.glGetSamplerParameterIui(sampler, pname);
    }

    public static float glGetSamplerParameterf(int sampler, int pname) {
        return org.lwjgl3.opengl.ARBSamplerObjects.glGetSamplerParameterf(sampler, pname);
    }

    public static int glGetSamplerParameteri(int sampler, int pname) {
        return org.lwjgl3.opengl.ARBSamplerObjects.glGetSamplerParameteri(sampler, pname);
    }

    public static boolean glIsSampler(int sampler) {
        return org.lwjgl3.opengl.ARBSamplerObjects.glIsSampler(sampler);
    }

    public static void glSamplerParameterf(int sampler, int pname, float param) {
        org.lwjgl3.opengl.ARBSamplerObjects.glSamplerParameterf(sampler, pname, param);
    }

    public static void glSamplerParameteri(int sampler, int pname, int param) {
        org.lwjgl3.opengl.ARBSamplerObjects.glSamplerParameteri(sampler, pname, param);
    }
}

package org.lwjglx.opengl;

public class ARBPointParameters {

    public static final int GL_POINT_DISTANCE_ATTENUATION_ARB = (int) 33065;
    public static final int GL_POINT_FADE_THRESHOLD_SIZE_ARB = (int) 33064;
    public static final int GL_POINT_SIZE_MAX_ARB = (int) 33063;
    public static final int GL_POINT_SIZE_MIN_ARB = (int) 33062;

    public static void glPointParameterARB(int pname, java.nio.FloatBuffer pfParams) {
        org.lwjgl.opengl.ARBPointParameters.glPointParameterfvARB(pname, pfParams);
    }

    public static void glPointParameterfARB(int pname, float param) {
        org.lwjgl.opengl.ARBPointParameters.glPointParameterfARB(pname, param);
    }
}

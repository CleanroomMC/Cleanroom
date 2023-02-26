package org.lwjglx.opengl;

public class EXTPointParameters {

    public static final int GL_DISTANCE_ATTENUATION_EXT = (int) 33065;
    public static final int GL_POINT_FADE_THRESHOLD_SIZE_EXT = (int) 33064;
    public static final int GL_POINT_SIZE_MAX_EXT = (int) 33063;
    public static final int GL_POINT_SIZE_MIN_EXT = (int) 33062;

    public static void glPointParameterEXT(int pname, java.nio.FloatBuffer pfParams) {
        org.lwjgl.opengl.EXTPointParameters.glPointParameterfvEXT(pname, pfParams);
    }

    public static void glPointParameterfEXT(int pname, float param) {
        org.lwjgl.opengl.EXTPointParameters.glPointParameterfEXT(pname, param);
    }
}

package org.lwjgl.opengl;

public class AMDSamplePositions {

    public static final int GL_SUBSAMPLE_DISTANCE_AMD = (int) 34879;

    public static void glSetMultisampleAMD(int pname, int index, java.nio.FloatBuffer val) {
        org.lwjgl3.opengl.AMDSamplePositions.glSetMultisamplefvAMD(pname, index, val);
    }
}

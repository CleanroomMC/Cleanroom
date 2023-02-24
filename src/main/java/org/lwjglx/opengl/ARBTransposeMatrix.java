package org.lwjglx.opengl;

public class ARBTransposeMatrix {

    public static final int GL_TRANSPOSE_COLOR_MATRIX_ARB = (int) 34022;
    public static final int GL_TRANSPOSE_MODELVIEW_MATRIX_ARB = (int) 34019;
    public static final int GL_TRANSPOSE_PROJECTION_MATRIX_ARB = (int) 34020;
    public static final int GL_TRANSPOSE_TEXTURE_MATRIX_ARB = (int) 34021;

    public static void glLoadTransposeMatrixARB(java.nio.FloatBuffer pfMtx) {
        org.lwjgl.opengl.ARBTransposeMatrix.glLoadTransposeMatrixfARB(pfMtx);
    }

    public static void glMultTransposeMatrixARB(java.nio.FloatBuffer pfMtx) {
        org.lwjgl.opengl.ARBTransposeMatrix.glMultTransposeMatrixfARB(pfMtx);
    }
}

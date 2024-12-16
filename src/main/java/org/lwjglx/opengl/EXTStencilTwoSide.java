package org.lwjglx.opengl;

public class EXTStencilTwoSide {

    public static final int GL_ACTIVE_STENCIL_FACE_EXT = (int) 35089;
    public static final int GL_STENCIL_TEST_TWO_SIDE_EXT = (int) 35088;

    public static void glActiveStencilFaceEXT(int face) {
        org.lwjgl.opengl.EXTStencilTwoSide.glActiveStencilFaceEXT(face);
    }
}

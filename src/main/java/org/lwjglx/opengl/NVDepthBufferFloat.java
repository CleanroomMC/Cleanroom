package org.lwjglx.opengl;

public class NVDepthBufferFloat {

    public static final int GL_DEPTH32F_STENCIL8_NV = (int) 36268;
    public static final int GL_DEPTH_BUFFER_FLOAT_MODE_NV = (int) 36271;
    public static final int GL_DEPTH_COMPONENT32F_NV = (int) 36267;
    public static final int GL_FLOAT_32_UNSIGNED_INT_24_8_REV_NV = (int) 36269;

    public static void glClearDepthdNV(double d) {
        org.lwjgl.opengl.NVDepthBufferFloat.glClearDepthdNV(d);
    }

    public static void glDepthBoundsdNV(double zmin, double zmax) {
        org.lwjgl.opengl.NVDepthBufferFloat.glDepthBoundsdNV(zmin, zmax);
    }

    public static void glDepthRangedNV(double n, double f) {
        org.lwjgl.opengl.NVDepthBufferFloat.glDepthRangedNV(n, f);
    }
}

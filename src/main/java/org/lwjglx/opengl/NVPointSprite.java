package org.lwjglx.opengl;

public class NVPointSprite {

    public static final int GL_COORD_REPLACE_NV = (int) 34914;
    public static final int GL_POINT_SPRITE_NV = (int) 34913;
    public static final int GL_POINT_SPRITE_R_MODE_NV = (int) 34915;

    public static void glPointParameterNV(int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.NVPointSprite.glPointParameterivNV(pname, params);
    }

    public static void glPointParameteriNV(int pname, int param) {
        org.lwjgl.opengl.NVPointSprite.glPointParameteriNV(pname, param);
    }
}

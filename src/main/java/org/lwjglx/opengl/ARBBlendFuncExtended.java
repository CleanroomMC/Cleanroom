package org.lwjglx.opengl;

public class ARBBlendFuncExtended {

    public static final int GL_MAX_DUAL_SOURCE_DRAW_BUFFERS = (int) 35068;
    public static final int GL_ONE_MINUS_SRC1_ALPHA = (int) 35067;
    public static final int GL_ONE_MINUS_SRC1_COLOR = (int) 35066;
    public static final int GL_SRC1_ALPHA = (int) 34185;
    public static final int GL_SRC1_COLOR = (int) 35065;

    public static void glBindFragDataLocationIndexed(int program, int colorNumber, int index,
            java.lang.CharSequence name) {
        org.lwjgl.opengl.ARBBlendFuncExtended.glBindFragDataLocationIndexed(program, colorNumber, index, name);
    }

    public static void glBindFragDataLocationIndexed(int program, int colorNumber, int index,
            java.nio.ByteBuffer name) {
        org.lwjgl.opengl.ARBBlendFuncExtended.glBindFragDataLocationIndexed(program, colorNumber, index, name);
    }

    public static int glGetFragDataIndex(int program, java.lang.CharSequence name) {
        return org.lwjgl.opengl.ARBBlendFuncExtended.glGetFragDataIndex(program, name);
    }

    public static int glGetFragDataIndex(int program, java.nio.ByteBuffer name) {
        return org.lwjgl.opengl.ARBBlendFuncExtended.glGetFragDataIndex(program, name);
    }
}

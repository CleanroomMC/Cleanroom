package org.lwjglx.opengl;

public class ARBRobustness {

    public static final int GL_CONTEXT_FLAG_ROBUST_ACCESS_BIT_ARB = (int) 4;
    public static final int GL_GUILTY_CONTEXT_RESET_ARB = (int) 33363;
    public static final int GL_INNOCENT_CONTEXT_RESET_ARB = (int) 33364;
    public static final int GL_LOSE_CONTEXT_ON_RESET_ARB = (int) 33362;
    public static final int GL_NO_RESET_NOTIFICATION_ARB = (int) 33377;
    public static final int GL_RESET_NOTIFICATION_STRATEGY_ARB = (int) 33366;
    public static final int GL_UNKNOWN_CONTEXT_RESET_ARB = (int) 33365;

    public static int glGetGraphicsResetStatusARB() {
        return org.lwjgl.opengl.ARBRobustness.glGetGraphicsResetStatusARB();
    }

    public static void glGetnColorTableARB(int target, int format, int type, java.nio.ByteBuffer table) {
        org.lwjgl.opengl.ARBRobustness.glGetnColorTableARB(target, format, type, table);
    }

    public static void glGetnColorTableARB(int target, int format, int type, java.nio.FloatBuffer table) {
        org.lwjgl.opengl.ARBRobustness.glGetnColorTableARB(target, format, type, table);
    }

    public static void glGetnCompressedTexImageARB(int target, int lod, int img_bufSize, long img_buffer_offset) {
        org.lwjgl.opengl.ARBRobustness.glGetnCompressedTexImageARB(target, lod, img_bufSize, img_buffer_offset);
    }

    public static void glGetnCompressedTexImageARB(int target, int lod, java.nio.ByteBuffer img) {
        org.lwjgl.opengl.ARBRobustness.glGetnCompressedTexImageARB(target, lod, img);
    }

    public static void glGetnConvolutionFilterARB(int target, int format, int type, int image_bufSize,
            long image_buffer_offset) {
        org.lwjgl.opengl.ARBRobustness
                .glGetnConvolutionFilterARB(target, format, type, image_bufSize, image_buffer_offset);
    }

    public static void glGetnConvolutionFilterARB(int target, int format, int type, java.nio.ByteBuffer image) {
        org.lwjgl.opengl.ARBRobustness.glGetnConvolutionFilterARB(target, format, type, image);
    }

    public static void glGetnHistogramARB(int target, boolean reset, int format, int type, int values_bufSize,
            long values_buffer_offset) {
        org.lwjgl.opengl.ARBRobustness
                .glGetnHistogramARB(target, reset, format, type, values_bufSize, values_buffer_offset);
    }

    public static void glGetnHistogramARB(int target, boolean reset, int format, int type, java.nio.ByteBuffer values) {
        org.lwjgl.opengl.ARBRobustness.glGetnHistogramARB(target, reset, format, type, values);
    }

    public static void glGetnMapdvARB(int target, int query, java.nio.DoubleBuffer v) {
        org.lwjgl.opengl.ARBRobustness.glGetnMapdvARB(target, query, v);
    }

    public static void glGetnMapfvARB(int target, int query, java.nio.FloatBuffer v) {
        org.lwjgl.opengl.ARBRobustness.glGetnMapfvARB(target, query, v);
    }

    public static void glGetnMapivARB(int target, int query, java.nio.IntBuffer v) {
        org.lwjgl.opengl.ARBRobustness.glGetnMapivARB(target, query, v);
    }

    public static void glGetnMinmaxARB(int target, boolean reset, int format, int type, int values_bufSize,
            long values_buffer_offset) {
        org.lwjgl.opengl.ARBRobustness
                .glGetnMinmaxARB(target, reset, format, type, values_bufSize, values_buffer_offset);
    }

    public static void glGetnMinmaxARB(int target, boolean reset, int format, int type, java.nio.ByteBuffer values) {
        org.lwjgl.opengl.ARBRobustness.glGetnMinmaxARB(target, reset, format, type, values);
    }

    public static void glGetnPixelMapfvARB(int map, java.nio.FloatBuffer values) {
        org.lwjgl.opengl.ARBRobustness.glGetnPixelMapfvARB(map, values);
    }

    public static void glGetnPixelMapuivARB(int map, java.nio.IntBuffer values) {
        org.lwjgl.opengl.ARBRobustness.glGetnPixelMapuivARB(map, values);
    }

    public static void glGetnPixelMapusvARB(int map, java.nio.ShortBuffer values) {
        org.lwjgl.opengl.ARBRobustness.glGetnPixelMapusvARB(map, values);
    }

    public static void glGetnPolygonStippleARB(java.nio.ByteBuffer pattern) {
        org.lwjgl.opengl.ARBRobustness.glGetnPolygonStippleARB(pattern);
    }

    public static void glGetnSeparableFilterARB(int target, int format, int type, java.nio.ByteBuffer row,
            java.nio.ByteBuffer column, java.nio.ByteBuffer span) {
        org.lwjgl.opengl.ARBRobustness.glGetnSeparableFilterARB(target, format, type, row, column, span);
    }

    public static void glGetnTexImageARB(int target, int level, int format, int type, int img_bufSize,
            long img_buffer_offset) {
        org.lwjgl.opengl.ARBRobustness.glGetnTexImageARB(target, level, format, type, img_bufSize, img_buffer_offset);
    }

    public static void glGetnTexImageARB(int target, int level, int format, int type, java.nio.ByteBuffer img) {
        org.lwjgl.opengl.ARBRobustness.glGetnTexImageARB(target, level, format, type, img);
    }

    public static void glGetnTexImageARB(int target, int level, int format, int type, java.nio.DoubleBuffer img) {
        org.lwjgl.opengl.ARBRobustness.glGetnTexImageARB(target, level, format, type, img);
    }

    public static void glGetnTexImageARB(int target, int level, int format, int type, java.nio.FloatBuffer img) {
        org.lwjgl.opengl.ARBRobustness.glGetnTexImageARB(target, level, format, type, img);
    }

    public static void glGetnTexImageARB(int target, int level, int format, int type, java.nio.IntBuffer img) {
        org.lwjgl.opengl.ARBRobustness.glGetnTexImageARB(target, level, format, type, img);
    }

    public static void glGetnTexImageARB(int target, int level, int format, int type, java.nio.ShortBuffer img) {
        org.lwjgl.opengl.ARBRobustness.glGetnTexImageARB(target, level, format, type, img);
    }

    public static void glGetnUniformdvARB(int program, int location, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.ARBRobustness.glGetnUniformdvARB(program, location, params);
    }

    public static void glGetnUniformfvARB(int program, int location, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.ARBRobustness.glGetnUniformfvARB(program, location, params);
    }

    public static void glGetnUniformivARB(int program, int location, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBRobustness.glGetnUniformivARB(program, location, params);
    }

    public static void glGetnUniformuivARB(int program, int location, java.nio.IntBuffer params) {
        org.lwjgl.opengl.ARBRobustness.glGetnUniformuivARB(program, location, params);
    }

    public static void glReadnPixelsARB(int x, int y, int width, int height, int format, int type, int data_bufSize,
            long data_buffer_offset) {
        org.lwjgl.opengl.ARBRobustness
                .glReadnPixelsARB(x, y, width, height, format, type, data_bufSize, data_buffer_offset);
    }

    public static void glReadnPixelsARB(int x, int y, int width, int height, int format, int type,
            java.nio.ByteBuffer data) {
        org.lwjgl.opengl.ARBRobustness.glReadnPixelsARB(x, y, width, height, format, type, data);
    }

    public static void glReadnPixelsARB(int x, int y, int width, int height, int format, int type,
            java.nio.FloatBuffer data) {
        org.lwjgl.opengl.ARBRobustness.glReadnPixelsARB(x, y, width, height, format, type, data);
    }

    public static void glReadnPixelsARB(int x, int y, int width, int height, int format, int type,
            java.nio.IntBuffer data) {
        org.lwjgl.opengl.ARBRobustness.glReadnPixelsARB(x, y, width, height, format, type, data);
    }

    public static void glReadnPixelsARB(int x, int y, int width, int height, int format, int type,
            java.nio.ShortBuffer data) {
        org.lwjgl.opengl.ARBRobustness.glReadnPixelsARB(x, y, width, height, format, type, data);
    }
}

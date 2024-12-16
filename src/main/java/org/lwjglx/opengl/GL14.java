package org.lwjglx.opengl;

import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

public class GL14 {

    public static final int GL_BLEND_COLOR = (int) 32773;
    public static final int GL_BLEND_DST_ALPHA = (int) 32970;
    public static final int GL_BLEND_DST_RGB = (int) 32968;
    public static final int GL_BLEND_EQUATION = (int) 32777;
    public static final int GL_BLEND_SRC_ALPHA = (int) 32971;
    public static final int GL_BLEND_SRC_RGB = (int) 32969;
    public static final int GL_COLOR_SUM = (int) 33880;
    public static final int GL_COMPARE_R_TO_TEXTURE = (int) 34894;
    public static final int GL_CURRENT_FOG_COORDINATE = (int) 33875;
    public static final int GL_CURRENT_SECONDARY_COLOR = (int) 33881;
    public static final int GL_DECR_WRAP = (int) 34056;
    public static final int GL_DEPTH_COMPONENT16 = (int) 33189;
    public static final int GL_DEPTH_COMPONENT24 = (int) 33190;
    public static final int GL_DEPTH_COMPONENT32 = (int) 33191;
    public static final int GL_DEPTH_TEXTURE_MODE = (int) 34891;
    public static final int GL_FOG_COORDINATE = (int) 33873;
    public static final int GL_FOG_COORDINATE_ARRAY = (int) 33879;
    public static final int GL_FOG_COORDINATE_ARRAY_POINTER = (int) 33878;
    public static final int GL_FOG_COORDINATE_ARRAY_STRIDE = (int) 33877;
    public static final int GL_FOG_COORDINATE_ARRAY_TYPE = (int) 33876;
    public static final int GL_FOG_COORDINATE_SOURCE = (int) 33872;
    public static final int GL_FRAGMENT_DEPTH = (int) 33874;
    public static final int GL_FUNC_ADD = (int) 32774;
    public static final int GL_FUNC_REVERSE_SUBTRACT = (int) 32779;
    public static final int GL_FUNC_SUBTRACT = (int) 32778;
    public static final int GL_GENERATE_MIPMAP = (int) 33169;
    public static final int GL_GENERATE_MIPMAP_HINT = (int) 33170;
    public static final int GL_INCR_WRAP = (int) 34055;
    public static final int GL_MAX = (int) 32776;
    public static final int GL_MAX_TEXTURE_LOD_BIAS = (int) 34045;
    public static final int GL_MIN = (int) 32775;
    public static final int GL_MIRRORED_REPEAT = (int) 33648;
    public static final int GL_POINT_DISTANCE_ATTENUATION = (int) 33065;
    public static final int GL_POINT_FADE_THRESHOLD_SIZE = (int) 33064;
    public static final int GL_POINT_SIZE_MAX = (int) 33063;
    public static final int GL_POINT_SIZE_MIN = (int) 33062;
    public static final int GL_SECONDARY_COLOR_ARRAY = (int) 33886;
    public static final int GL_SECONDARY_COLOR_ARRAY_POINTER = (int) 33885;
    public static final int GL_SECONDARY_COLOR_ARRAY_SIZE = (int) 33882;
    public static final int GL_SECONDARY_COLOR_ARRAY_STRIDE = (int) 33884;
    public static final int GL_SECONDARY_COLOR_ARRAY_TYPE = (int) 33883;
    public static final int GL_TEXTURE_COMPARE_FUNC = (int) 34893;
    public static final int GL_TEXTURE_COMPARE_MODE = (int) 34892;
    public static final int GL_TEXTURE_DEPTH_SIZE = (int) 34890;
    public static final int GL_TEXTURE_FILTER_CONTROL = (int) 34048;
    public static final int GL_TEXTURE_LOD_BIAS = (int) 34049;

    public static void glBlendColor(float red, float green, float blue, float alpha) {
        org.lwjgl.opengl.GL14.glBlendColor(red, green, blue, alpha);
    }

    public static void glBlendEquation(int mode) {
        org.lwjgl.opengl.GL14.glBlendEquation(mode);
    }

    public static void glBlendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
        org.lwjgl.opengl.GL14.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
    }

    public static void glFogCoordPointer(int type, int stride, long data_buffer_offset) {
        org.lwjgl.opengl.GL14.glFogCoordPointer(type, stride, data_buffer_offset);
    }

    public static void glFogCoordPointer(int stride, java.nio.DoubleBuffer data) {

        org.lwjgl.opengl.GL14.glFogCoordPointer(
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(data));
    }

    public static void glFogCoordPointer(int stride, java.nio.FloatBuffer data) {

        org.lwjgl.opengl.GL14.glFogCoordPointer(org.lwjgl.opengl.GL11.GL_FLOAT, stride, data);
    }

    public static void glFogCoordd(double coord) {
        org.lwjgl.opengl.GL14.glFogCoordd(coord);
    }

    public static void glFogCoordf(float coord) {
        org.lwjgl.opengl.GL14.glFogCoordf(coord);
    }

    public static void glMultiDrawArrays(int mode, java.nio.IntBuffer piFirst, java.nio.IntBuffer piCount) {
        org.lwjgl.opengl.GL14.glMultiDrawArrays(mode, piFirst, piCount);
    }

    public static void glPointParameter(int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.GL14.glPointParameterfv(pname, params);
    }

    public static void glPointParameter(int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL14.glPointParameteriv(pname, params);
    }

    public static void glPointParameterf(int pname, float param) {
        org.lwjgl.opengl.GL14.glPointParameterf(pname, param);
    }

    public static void glPointParameteri(int pname, int param) {
        org.lwjgl.opengl.GL14.glPointParameteri(pname, param);
    }

    public static void glSecondaryColor3b(byte red, byte green, byte blue) {
        org.lwjgl.opengl.GL14.glSecondaryColor3b(red, green, blue);
    }

    public static void glSecondaryColor3d(double red, double green, double blue) {
        org.lwjgl.opengl.GL14.glSecondaryColor3d(red, green, blue);
    }

    public static void glSecondaryColor3f(float red, float green, float blue) {
        org.lwjgl.opengl.GL14.glSecondaryColor3f(red, green, blue);
    }

    public static void glSecondaryColor3ub(byte red, byte green, byte blue) {
        org.lwjgl.opengl.GL14.glSecondaryColor3ub(red, green, blue);
    }

    public static void glSecondaryColorPointer(int size, int type, int stride, long data_buffer_offset) {
        org.lwjgl.opengl.GL14.glSecondaryColorPointer(size, type, stride, data_buffer_offset);
    }

    public static void glSecondaryColorPointer(int size, int stride, java.nio.DoubleBuffer data) {

        org.lwjgl.opengl.GL14.glSecondaryColorPointer(
                size,
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(data));
    }

    public static void glSecondaryColorPointer(int size, int stride, java.nio.FloatBuffer data) {

        org.lwjgl.opengl.GL14.glSecondaryColorPointer(size, org.lwjgl.opengl.GL11.GL_FLOAT, stride, data);
    }

    public static void glSecondaryColorPointer(int size, boolean unsigned, int stride, java.nio.ByteBuffer data) {

        org.lwjgl.opengl.GL14.glSecondaryColorPointer(
                size,
                (unsigned ? org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE : org.lwjgl.opengl.GL11.GL_BYTE),
                stride,
                MemoryUtil.getAddress(data));
    }

    public static void glWindowPos2d(double x, double y) {
        org.lwjgl.opengl.GL14.glWindowPos2d(x, y);
    }

    public static void glWindowPos2f(float x, float y) {
        org.lwjgl.opengl.GL14.glWindowPos2f(x, y);
    }

    public static void glWindowPos2i(int x, int y) {
        org.lwjgl.opengl.GL14.glWindowPos2i(x, y);
    }

    public static void glWindowPos3d(double x, double y, double z) {
        org.lwjgl.opengl.GL14.glWindowPos3d(x, y, z);
    }

    public static void glWindowPos3f(float x, float y, float z) {
        org.lwjgl.opengl.GL14.glWindowPos3f(x, y, z);
    }

    public static void glWindowPos3i(int x, int y, int z) {
        org.lwjgl.opengl.GL14.glWindowPos3i(x, y, z);
    }
}

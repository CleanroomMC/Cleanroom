package org.lwjglx.opengl;

public class GL21 {

    public static final int GL_COMPRESSED_SLUMINANCE = (int) 35914;
    public static final int GL_COMPRESSED_SLUMINANCE_ALPHA = (int) 35915;
    public static final int GL_COMPRESSED_SRGB = (int) 35912;
    public static final int GL_COMPRESSED_SRGB_ALPHA = (int) 35913;
    public static final int GL_CURRENT_RASTER_SECONDARY_COLOR = (int) 33887;
    public static final int GL_FLOAT_MAT2x3 = (int) 35685;
    public static final int GL_FLOAT_MAT2x4 = (int) 35686;
    public static final int GL_FLOAT_MAT3x2 = (int) 35687;
    public static final int GL_FLOAT_MAT3x4 = (int) 35688;
    public static final int GL_FLOAT_MAT4x2 = (int) 35689;
    public static final int GL_FLOAT_MAT4x3 = (int) 35690;
    public static final int GL_PIXEL_PACK_BUFFER = (int) 35051;
    public static final int GL_PIXEL_PACK_BUFFER_BINDING = (int) 35053;
    public static final int GL_PIXEL_UNPACK_BUFFER = (int) 35052;
    public static final int GL_PIXEL_UNPACK_BUFFER_BINDING = (int) 35055;
    public static final int GL_SLUMINANCE8 = (int) 35911;
    public static final int GL_SLUMINANCE8_ALPHA8 = (int) 35909;
    public static final int GL_SLUMINANCE = (int) 35910;
    public static final int GL_SLUMINANCE_ALPHA = (int) 35908;
    public static final int GL_SRGB8 = (int) 35905;
    public static final int GL_SRGB8_ALPHA8 = (int) 35907;
    public static final int GL_SRGB = (int) 35904;
    public static final int GL_SRGB_ALPHA = (int) 35906;

    public static void glUniformMatrix2x3(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix2x3fv(location, transpose, matrices);
    }

    public static void glUniformMatrix2x4(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix2x4fv(location, transpose, matrices);
    }

    public static void glUniformMatrix3x2(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix3x2fv(location, transpose, matrices);
    }

    public static void glUniformMatrix3x4(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix3x4fv(location, transpose, matrices);
    }

    public static void glUniformMatrix4x2(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix4x2fv(location, transpose, matrices);
    }

    public static void glUniformMatrix4x3(int location, boolean transpose, java.nio.FloatBuffer matrices) {
        org.lwjgl.opengl.GL21.glUniformMatrix4x3fv(location, transpose, matrices);
    }
}

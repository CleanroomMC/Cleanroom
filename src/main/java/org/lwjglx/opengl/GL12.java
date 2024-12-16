package org.lwjglx.opengl;

public class GL12 {

    public static final int GL_ALIASED_LINE_WIDTH_RANGE = (int) 33902;
    public static final int GL_ALIASED_POINT_SIZE_RANGE = (int) 33901;
    public static final int GL_BGR = (int) 32992;
    public static final int GL_BGRA = (int) 32993;
    public static final int GL_CLAMP_TO_EDGE = (int) 33071;
    public static final int GL_LIGHT_MODEL_COLOR_CONTROL = (int) 33272;
    public static final int GL_MAX_3D_TEXTURE_SIZE = (int) 32883;
    public static final int GL_MAX_ELEMENTS_INDICES = (int) 33001;
    public static final int GL_MAX_ELEMENTS_VERTICES = (int) 33000;
    public static final int GL_PACK_IMAGE_HEIGHT = (int) 32876;
    public static final int GL_PACK_SKIP_IMAGES = (int) 32875;
    public static final int GL_PROXY_TEXTURE_3D = (int) 32880;
    public static final int GL_RESCALE_NORMAL = (int) 32826;
    public static final int GL_SEPARATE_SPECULAR_COLOR = (int) 33274;
    public static final int GL_SINGLE_COLOR = (int) 33273;
    public static final int GL_SMOOTH_LINE_WIDTH_GRANULARITY = (int) 2851;
    public static final int GL_SMOOTH_LINE_WIDTH_RANGE = (int) 2850;
    public static final int GL_SMOOTH_POINT_SIZE_GRANULARITY = (int) 2835;
    public static final int GL_SMOOTH_POINT_SIZE_RANGE = (int) 2834;
    public static final int GL_TEXTURE_3D = (int) 32879;
    public static final int GL_TEXTURE_BASE_LEVEL = (int) 33084;
    public static final int GL_TEXTURE_BINDING_3D = (int) 32874;
    public static final int GL_TEXTURE_DEPTH = (int) 32881;
    public static final int GL_TEXTURE_MAX_LEVEL = (int) 33085;
    public static final int GL_TEXTURE_MAX_LOD = (int) 33083;
    public static final int GL_TEXTURE_MIN_LOD = (int) 33082;
    public static final int GL_TEXTURE_WRAP_R = (int) 32882;
    public static final int GL_UNPACK_IMAGE_HEIGHT = (int) 32878;
    public static final int GL_UNPACK_SKIP_IMAGES = (int) 32877;
    public static final int GL_UNSIGNED_BYTE_2_3_3_REV = (int) 33634;
    public static final int GL_UNSIGNED_BYTE_3_3_2 = (int) 32818;
    public static final int GL_UNSIGNED_INT_10_10_10_2 = (int) 32822;
    public static final int GL_UNSIGNED_INT_2_10_10_10_REV = (int) 33640;
    public static final int GL_UNSIGNED_INT_8_8_8_8 = (int) 32821;
    public static final int GL_UNSIGNED_INT_8_8_8_8_REV = (int) 33639;
    public static final int GL_UNSIGNED_SHORT_1_5_5_5_REV = (int) 33638;
    public static final int GL_UNSIGNED_SHORT_4_4_4_4 = (int) 32819;
    public static final int GL_UNSIGNED_SHORT_4_4_4_4_REV = (int) 33637;
    public static final int GL_UNSIGNED_SHORT_5_5_5_1 = (int) 32820;
    public static final int GL_UNSIGNED_SHORT_5_6_5 = (int) 33635;
    public static final int GL_UNSIGNED_SHORT_5_6_5_REV = (int) 33636;

    public static void glCopyTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int x, int y,
            int width, int height) {
        org.lwjgl.opengl.GL12.glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public static void glDrawRangeElements(int mode, int start, int end, int indices_count, int type,
            long indices_buffer_offset) {
        org.lwjgl.opengl.GL12.glDrawRangeElements(mode, start, end, indices_count, type, indices_buffer_offset);
    }

    public static void glDrawRangeElements(int mode, int start, int end, java.nio.ByteBuffer indices) {
        org.lwjgl.opengl.GL12.glDrawRangeElements(mode, start, end, indices);
    }

    public static void glDrawRangeElements(int mode, int start, int end, java.nio.IntBuffer indices) {
        org.lwjgl.opengl.GL12.glDrawRangeElements(mode, start, end, indices);
    }

    public static void glDrawRangeElements(int mode, int start, int end, java.nio.ShortBuffer indices) {
        org.lwjgl.opengl.GL12.glDrawRangeElements(mode, start, end, indices);
    }

    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
            int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL12.glTexImage3D(
                target,
                level,
                internalFormat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
            int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
            int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
            int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
            int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    public static void glTexImage3D(int target, int level, int internalFormat, int width, int height, int depth,
            int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexImage3D(target, level, internalFormat, width, height, depth, border, format, type, pixels);
    }

    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.GL12.glTexSubImage3D(
                target,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }

    public static void glTexSubImage3D(int target, int level, int xoffset, int yoffset, int zoffset, int width,
            int height, int depth, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.GL12
                .glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    }
}

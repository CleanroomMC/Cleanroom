package org.lwjglx.opengl;

import org.lwjglx.MemoryUtil;
import org.lwjglx.lwjgl3ify.BufferCasts;

public class EXTDirectStateAccess {

    public static final int GL_PROGRAM_MATRIX_EXT = (int) 36397;
    public static final int GL_PROGRAM_MATRIX_STACK_DEPTH_EXT = (int) 36399;
    public static final int GL_TRANSPOSE_PROGRAM_MATRIX_EXT = (int) 36398;

    public static void glBindMultiTextureEXT(int texunit, int target, int texture) {
        org.lwjgl.opengl.EXTDirectStateAccess.glBindMultiTextureEXT(texunit, target, texture);
    }

    public static int glCheckNamedFramebufferStatusEXT(int framebuffer, int target) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glCheckNamedFramebufferStatusEXT(framebuffer, target);
    }

    public static void glClientAttribDefaultEXT(int mask) {
        org.lwjgl.opengl.EXTDirectStateAccess.glClientAttribDefaultEXT(mask);
    }

    public static void glCompressedMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexImage1DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                border,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCompressedMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, data);
    }

    public static void glCompressedMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCompressedMultiTexImage2DEXT(texunit, target, level, internalformat, width, height, border, data);
    }

    public static void glCompressedMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                data);
    }

    public static void glCompressedMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width,
            int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexSubImage1DEXT(
                texunit,
                target,
                level,
                xoffset,
                width,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width,
            int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCompressedMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, data);
    }

    public static void glCompressedMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int width, int height, int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexSubImage2DEXT(
                texunit,
                target,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int width, int height, int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexSubImage2DEXT(
                texunit,
                target,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                data);
    }

    public static void glCompressedMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexSubImage3DEXT(
                texunit,
                target,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedMultiTexSubImage3DEXT(
                texunit,
                target,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                data);
    }

    public static void glCompressedTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureImage1DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                border,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCompressedTextureImage1DEXT(texture, target, level, internalformat, width, border, data);
    }

    public static void glCompressedTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCompressedTextureImage2DEXT(texture, target, level, internalformat, width, height, border, data);
    }

    public static void glCompressedTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                data);
    }

    public static void glCompressedTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width,
            int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureSubImage1DEXT(
                texture,
                target,
                level,
                xoffset,
                width,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width,
            int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCompressedTextureSubImage1DEXT(texture, target, level, xoffset, width, format, data);
    }

    public static void glCompressedTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset,
            int width, int height, int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureSubImage2DEXT(
                texture,
                target,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset,
            int width, int height, int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureSubImage2DEXT(
                texture,
                target,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                data);
    }

    public static void glCompressedTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int data_imageSize, long data_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureSubImage3DEXT(
                texture,
                target,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                data_imageSize,
                data_buffer_offset);
    }

    public static void glCompressedTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCompressedTextureSubImage3DEXT(
                texture,
                target,
                level,
                xoffset,
                yoffset,
                zoffset,
                width,
                height,
                depth,
                format,
                data);
    }

    public static void glCopyMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int x, int y,
            int width, int border) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyMultiTexImage1DEXT(texunit, target, level, internalformat, x, y, width, border);
    }

    public static void glCopyMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int x, int y,
            int width, int height, int border) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyMultiTexImage2DEXT(texunit, target, level, internalformat, x, y, width, height, border);
    }

    public static void glCopyMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int x, int y,
            int width) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCopyMultiTexSubImage1DEXT(texunit, target, level, xoffset, x, y, width);
    }

    public static void glCopyMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int x,
            int y, int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyMultiTexSubImage2DEXT(texunit, target, level, xoffset, yoffset, x, y, width, height);
    }

    public static void glCopyMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int x, int y, int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyMultiTexSubImage3DEXT(texunit, target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public static void glCopyTextureImage1DEXT(int texture, int target, int level, int internalformat, int x, int y,
            int width, int border) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyTextureImage1DEXT(texture, target, level, internalformat, x, y, width, border);
    }

    public static void glCopyTextureImage2DEXT(int texture, int target, int level, int internalformat, int x, int y,
            int width, int height, int border) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyTextureImage2DEXT(texture, target, level, internalformat, x, y, width, height, border);
    }

    public static void glCopyTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int x, int y,
            int width) {
        org.lwjgl.opengl.EXTDirectStateAccess.glCopyTextureSubImage1DEXT(texture, target, level, xoffset, x, y, width);
    }

    public static void glCopyTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int x,
            int y, int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, x, y, width, height);
    }

    public static void glCopyTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset,
            int zoffset, int x, int y, int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glCopyTextureSubImage3DEXT(texture, target, level, xoffset, yoffset, zoffset, x, y, width, height);
    }

    public static void glDisableClientStateIndexedEXT(int array, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glDisableClientStateIndexedEXT(array, index);
    }

    public static void glDisableClientStateiEXT(int array, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glDisableClientStateiEXT(array, index);
    }

    public static void glDisableIndexedEXT(int cap, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glDisableIndexedEXT(cap, index);
    }

    public static void glDisableVertexArrayAttribEXT(int vaobj, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glDisableVertexArrayAttribEXT(vaobj, index);
    }

    public static void glDisableVertexArrayEXT(int vaobj, int array) {
        org.lwjgl.opengl.EXTDirectStateAccess.glDisableVertexArrayEXT(vaobj, array);
    }

    public static void glEnableClientStateIndexedEXT(int array, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glEnableClientStateIndexedEXT(array, index);
    }

    public static void glEnableClientStateiEXT(int array, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glEnableClientStateiEXT(array, index);
    }

    public static void glEnableIndexedEXT(int cap, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glEnableIndexedEXT(cap, index);
    }

    public static void glEnableVertexArrayAttribEXT(int vaobj, int index) {
        org.lwjgl.opengl.EXTDirectStateAccess.glEnableVertexArrayAttribEXT(vaobj, index);
    }

    public static void glEnableVertexArrayEXT(int vaobj, int array) {
        org.lwjgl.opengl.EXTDirectStateAccess.glEnableVertexArrayEXT(vaobj, array);
    }

    public static void glFlushMappedNamedBufferRangeEXT(int buffer, long offset, long length) {
        org.lwjgl.opengl.EXTDirectStateAccess.glFlushMappedNamedBufferRangeEXT(buffer, offset, length);
    }

    public static void glFramebufferDrawBufferEXT(int framebuffer, int mode) {
        org.lwjgl.opengl.EXTDirectStateAccess.glFramebufferDrawBufferEXT(framebuffer, mode);
    }

    public static void glFramebufferDrawBuffersEXT(int framebuffer, java.nio.IntBuffer bufs) {
        org.lwjgl.opengl.EXTDirectStateAccess.glFramebufferDrawBuffersEXT(framebuffer, bufs);
    }

    public static void glFramebufferReadBufferEXT(int framebuffer, int mode) {
        org.lwjgl.opengl.EXTDirectStateAccess.glFramebufferReadBufferEXT(framebuffer, mode);
    }

    public static void glGenerateMultiTexMipmapEXT(int texunit, int target) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGenerateMultiTexMipmapEXT(texunit, target);
    }

    public static void glGenerateTextureMipmapEXT(int texture, int target) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGenerateTextureMipmapEXT(texture, target);
    }

    public static boolean glGetBooleanIndexedEXT(int pname, int index) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetBooleanIndexedEXT(pname, index);
    }

    public static void glGetCompressedMultiTexImageEXT(int texunit, int target, int level, long img_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetCompressedMultiTexImageEXT(texunit, target, level, img_buffer_offset);
    }

    public static void glGetCompressedMultiTexImageEXT(int texunit, int target, int level, java.nio.ByteBuffer img) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetCompressedMultiTexImageEXT(texunit, target, level, img);
    }

    public static void glGetCompressedMultiTexImageEXT(int texunit, int target, int level, java.nio.IntBuffer img) {

        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetCompressedMultiTexImageEXT(texunit, target, level, MemoryUtil.getAddress(img));
    }

    public static void glGetCompressedMultiTexImageEXT(int texunit, int target, int level, java.nio.ShortBuffer img) {

        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetCompressedMultiTexImageEXT(texunit, target, level, MemoryUtil.getAddress(img));
    }

    public static void glGetCompressedTextureImageEXT(int texture, int target, int level, long img_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetCompressedTextureImageEXT(texture, target, level, img_buffer_offset);
    }

    public static void glGetCompressedTextureImageEXT(int texture, int target, int level, java.nio.ByteBuffer img) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetCompressedTextureImageEXT(texture, target, level, img);
    }

    public static void glGetCompressedTextureImageEXT(int texture, int target, int level, java.nio.IntBuffer img) {

        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetCompressedTextureImageEXT(texture, target, level, MemoryUtil.getAddress(img));
    }

    public static void glGetCompressedTextureImageEXT(int texture, int target, int level, java.nio.ShortBuffer img) {

        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetCompressedTextureImageEXT(texture, target, level, MemoryUtil.getAddress(img));
    }

    public static void glGetDoubleEXT(int pname, int index, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetDoublei_vEXT(pname, index, params);
    }

    public static double glGetDoubleIndexedEXT(int pname, int index) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetDoubleIndexedEXT(pname, index);
    }

    public static void glGetDoubleIndexedEXT(int pname, int index, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetDoubleIndexedvEXT(pname, index, params);
    }

    public static void glGetFloatEXT(int pname, int index, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetFloati_vEXT(pname, index, params);
    }

    public static float glGetFloatIndexedEXT(int pname, int index) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetFloatIndexedEXT(pname, index);
    }

    public static void glGetFloatIndexedEXT(int pname, int index, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetFloatIndexedvEXT(pname, index, params);
    }

    public static void glGetFramebufferParameterEXT(int framebuffer, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetFramebufferParameterivEXT(framebuffer, pname, param);
    }

    public static int glGetIntegerIndexedEXT(int pname, int index) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetIntegerIndexedEXT(pname, index);
    }

    public static void glGetMultiTexEnvEXT(int texunit, int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexEnvfvEXT(texunit, target, pname, params);
    }

    public static void glGetMultiTexEnvEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexEnvivEXT(texunit, target, pname, params);
    }

    public static void glGetMultiTexGenEXT(int texunit, int coord, int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexGendvEXT(texunit, coord, pname, params);
    }

    public static void glGetMultiTexGenEXT(int texunit, int coord, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexGenfvEXT(texunit, coord, pname, params);
    }

    public static void glGetMultiTexGenEXT(int texunit, int coord, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexGenivEXT(texunit, coord, pname, params);
    }

    public static void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetMultiTexImageEXT(texunit, target, level, format, type, pixels_buffer_offset);
    }

    public static void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexImageEXT(texunit, target, level, format, type, pixels);
    }

    public static void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type,
            java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexImageEXT(texunit, target, level, format, type, pixels);
    }

    public static void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexImageEXT(texunit, target, level, format, type, pixels);
    }

    public static void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexImageEXT(texunit, target, level, format, type, pixels);
    }

    public static void glGetMultiTexImageEXT(int texunit, int target, int level, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexImageEXT(texunit, target, level, format, type, pixels);
    }

    public static void glGetMultiTexLevelParameterEXT(int texunit, int target, int level, int pname,
            java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexLevelParameterfvEXT(texunit, target, level, pname, params);
    }

    public static void glGetMultiTexLevelParameterEXT(int texunit, int target, int level, int pname,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexLevelParameterivEXT(texunit, target, level, pname, params);
    }

    public static float glGetMultiTexLevelParameterfEXT(int texunit, int target, int level, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexLevelParameterfEXT(texunit, target, level, pname);
    }

    public static int glGetMultiTexLevelParameteriEXT(int texunit, int target, int level, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexLevelParameteriEXT(texunit, target, level, pname);
    }

    public static void glGetMultiTexParameterEXT(int texunit, int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterfvEXT(texunit, target, pname, params);
    }

    public static void glGetMultiTexParameterEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterivEXT(texunit, target, pname, params);
    }

    public static void glGetMultiTexParameterIEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterIivEXT(texunit, target, pname, params);
    }

    public static int glGetMultiTexParameterIiEXT(int texunit, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterIiEXT(texunit, target, pname);
    }

    public static void glGetMultiTexParameterIuEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterIuivEXT(texunit, target, pname, params);
    }

    public static int glGetMultiTexParameterIuiEXT(int texunit, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterIuiEXT(texunit, target, pname);
    }

    public static float glGetMultiTexParameterfEXT(int texunit, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameterfEXT(texunit, target, pname);
    }

    public static int glGetMultiTexParameteriEXT(int texunit, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetMultiTexParameteriEXT(texunit, target, pname);
    }

    public static void glGetNamedBufferParameterEXT(int buffer, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedBufferParameterivEXT(buffer, pname, params);
    }

    public static void glGetNamedBufferSubDataEXT(int buffer, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glGetNamedBufferSubDataEXT(int buffer, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glGetNamedBufferSubDataEXT(int buffer, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glGetNamedBufferSubDataEXT(int buffer, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glGetNamedBufferSubDataEXT(int buffer, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glGetNamedFramebufferAttachmentParameterEXT(int framebuffer, int attachment, int pname,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetNamedFramebufferAttachmentParameterivEXT(framebuffer, attachment, pname, params);
    }

    public static void glGetNamedProgramEXT(int program, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedProgramivEXT(program, target, pname, params);
    }

    public static void glGetNamedProgramLocalParameterEXT(int program, int target, int index,
            java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedProgramLocalParameterdvEXT(program, target, index, params);
    }

    public static void glGetNamedProgramLocalParameterEXT(int program, int target, int index,
            java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedProgramLocalParameterfvEXT(program, target, index, params);
    }

    public static void glGetNamedProgramLocalParameterIEXT(int program, int target, int index,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedProgramLocalParameterIivEXT(program, target, index, params);
    }

    public static void glGetNamedProgramLocalParameterIuEXT(int program, int target, int index,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedProgramLocalParameterIuivEXT(program, target, index, params);
    }

    public static void glGetNamedProgramStringEXT(int program, int target, int pname, java.nio.ByteBuffer string) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedProgramStringEXT(program, target, pname, string);
    }

    public static void glGetNamedRenderbufferParameterEXT(int renderbuffer, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetNamedRenderbufferParameterivEXT(renderbuffer, pname, params);
    }

    public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type,
            long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glGetTextureImageEXT(texture, target, level, format, type, pixels_buffer_offset);
    }

    public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type,
            java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureImageEXT(texture, target, level, format, type, pixels);
    }

    public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type,
            java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureImageEXT(texture, target, level, format, type, pixels);
    }

    public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type,
            java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureImageEXT(texture, target, level, format, type, pixels);
    }

    public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type,
            java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureImageEXT(texture, target, level, format, type, pixels);
    }

    public static void glGetTextureImageEXT(int texture, int target, int level, int format, int type,
            java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureImageEXT(texture, target, level, format, type, pixels);
    }

    public static void glGetTextureLevelParameterEXT(int texture, int target, int level, int pname,
            java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureLevelParameterfvEXT(texture, target, level, pname, params);
    }

    public static void glGetTextureLevelParameterEXT(int texture, int target, int level, int pname,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureLevelParameterivEXT(texture, target, level, pname, params);
    }

    public static float glGetTextureLevelParameterfEXT(int texture, int target, int level, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureLevelParameterfEXT(texture, target, level, pname);
    }

    public static int glGetTextureLevelParameteriEXT(int texture, int target, int level, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureLevelParameteriEXT(texture, target, level, pname);
    }

    public static void glGetTextureParameterEXT(int texture, int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterfvEXT(texture, target, pname, params);
    }

    public static void glGetTextureParameterEXT(int texture, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterivEXT(texture, target, pname, params);
    }

    public static void glGetTextureParameterIEXT(int texture, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterIivEXT(texture, target, pname, params);
    }

    public static int glGetTextureParameterIiEXT(int texture, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterIiEXT(texture, target, pname);
    }

    public static void glGetTextureParameterIuEXT(int texture, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterIuivEXT(texture, target, pname, params);
    }

    public static int glGetTextureParameterIuiEXT(int texture, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterIuiEXT(texture, target, pname);
    }

    public static float glGetTextureParameterfEXT(int texture, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameterfEXT(texture, target, pname);
    }

    public static int glGetTextureParameteriEXT(int texture, int target, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetTextureParameteriEXT(texture, target, pname);
    }

    public static int glGetVertexArrayIntegerEXT(int vaobj, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetVertexArrayIntegerEXT(vaobj, pname);
    }

    public static void glGetVertexArrayIntegerEXT(int vaobj, int index, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetVertexArrayIntegeri_vEXT(vaobj, index, pname, param);
    }

    public static void glGetVertexArrayIntegerEXT(int vaobj, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glGetVertexArrayIntegervEXT(vaobj, pname, param);
    }

    public static int glGetVertexArrayIntegeriEXT(int vaobj, int index, int pname) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glGetVertexArrayIntegeriEXT(vaobj, index, pname);
    }

    public static boolean glIsEnabledIndexedEXT(int cap, int index) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glIsEnabledIndexedEXT(cap, index);
    }

    public static java.nio.ByteBuffer glMapNamedBufferEXT(int buffer, int access, long length,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glMapNamedBufferEXT(buffer, access, length, old_buffer);
    }

    public static java.nio.ByteBuffer glMapNamedBufferEXT(int buffer, int access, java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glMapNamedBufferEXT(buffer, access, old_buffer);
    }

    public static java.nio.ByteBuffer glMapNamedBufferRangeEXT(int buffer, long offset, long length, int access,
            java.nio.ByteBuffer old_buffer) {
        return org.lwjgl.opengl.EXTDirectStateAccess
                .glMapNamedBufferRangeEXT(buffer, offset, length, access, old_buffer);
    }

    public static void glMatrixFrustumEXT(int matrixMode, double l, double r, double b, double t, double n, double f) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixFrustumEXT(matrixMode, l, r, b, t, n, f);
    }

    public static void glMatrixLoadEXT(int matrixMode, java.nio.DoubleBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixLoaddEXT(matrixMode, m);
    }

    public static void glMatrixLoadEXT(int matrixMode, java.nio.FloatBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixLoadfEXT(matrixMode, m);
    }

    public static void glMatrixLoadIdentityEXT(int matrixMode) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixLoadIdentityEXT(matrixMode);
    }

    public static void glMatrixLoadTransposeEXT(int matrixMode, java.nio.DoubleBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixLoadTransposedEXT(matrixMode, m);
    }

    public static void glMatrixLoadTransposeEXT(int matrixMode, java.nio.FloatBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixLoadTransposefEXT(matrixMode, m);
    }

    public static void glMatrixMultEXT(int matrixMode, java.nio.DoubleBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixMultdEXT(matrixMode, m);
    }

    public static void glMatrixMultEXT(int matrixMode, java.nio.FloatBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixMultfEXT(matrixMode, m);
    }

    public static void glMatrixMultTransposeEXT(int matrixMode, java.nio.DoubleBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixMultTransposedEXT(matrixMode, m);
    }

    public static void glMatrixMultTransposeEXT(int matrixMode, java.nio.FloatBuffer m) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixMultTransposefEXT(matrixMode, m);
    }

    public static void glMatrixOrthoEXT(int matrixMode, double l, double r, double b, double t, double n, double f) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixOrthoEXT(matrixMode, l, r, b, t, n, f);
    }

    public static void glMatrixPopEXT(int matrixMode) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixPopEXT(matrixMode);
    }

    public static void glMatrixPushEXT(int matrixMode) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixPushEXT(matrixMode);
    }

    public static void glMatrixRotatedEXT(int matrixMode, double angle, double x, double y, double z) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixRotatedEXT(matrixMode, angle, x, y, z);
    }

    public static void glMatrixRotatefEXT(int matrixMode, float angle, float x, float y, float z) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixRotatefEXT(matrixMode, angle, x, y, z);
    }

    public static void glMatrixScaledEXT(int matrixMode, double x, double y, double z) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixScaledEXT(matrixMode, x, y, z);
    }

    public static void glMatrixScalefEXT(int matrixMode, float x, float y, float z) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixScalefEXT(matrixMode, x, y, z);
    }

    public static void glMatrixTranslatedEXT(int matrixMode, double x, double y, double z) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixTranslatedEXT(matrixMode, x, y, z);
    }

    public static void glMatrixTranslatefEXT(int matrixMode, float x, float y, float z) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMatrixTranslatefEXT(matrixMode, x, y, z);
    }

    public static void glMultiTexBufferEXT(int texunit, int target, int internalformat, int buffer) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexBufferEXT(texunit, target, internalformat, buffer);
    }

    public static void glMultiTexCoordPointerEXT(int texunit, int size, int type, int stride,
            long pointer_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexCoordPointerEXT(texunit, size, type, stride, pointer_buffer_offset);
    }

    public static void glMultiTexCoordPointerEXT(int texunit, int size, int stride, java.nio.DoubleBuffer pointer) {

        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexCoordPointerEXT(
                texunit,
                size,
                org.lwjgl.opengl.GL11.GL_DOUBLE,
                stride,
                BufferCasts.toByteBuffer(pointer));
    }

    public static void glMultiTexCoordPointerEXT(int texunit, int size, int stride, java.nio.FloatBuffer pointer) {

        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexCoordPointerEXT(texunit, size, org.lwjgl.opengl.GL11.GL_FLOAT, stride, pointer);
    }

    public static void glMultiTexEnvEXT(int texunit, int target, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexEnvfvEXT(texunit, target, pname, params);
    }

    public static void glMultiTexEnvEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexEnvivEXT(texunit, target, pname, params);
    }

    public static void glMultiTexEnvfEXT(int texunit, int target, int pname, float param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexEnvfEXT(texunit, target, pname, param);
    }

    public static void glMultiTexEnviEXT(int texunit, int target, int pname, int param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexEnviEXT(texunit, target, pname, param);
    }

    public static void glMultiTexGenEXT(int texunit, int coord, int pname, java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexGendvEXT(texunit, coord, pname, params);
    }

    public static void glMultiTexGenEXT(int texunit, int coord, int pname, java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexGenfvEXT(texunit, coord, pname, params);
    }

    public static void glMultiTexGenEXT(int texunit, int coord, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexGenivEXT(texunit, coord, pname, params);
    }

    public static void glMultiTexGendEXT(int texunit, int coord, int pname, double param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexGendEXT(texunit, coord, pname, param);
    }

    public static void glMultiTexGenfEXT(int texunit, int coord, int pname, float param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexGenfEXT(texunit, coord, pname, param);
    }

    public static void glMultiTexGeniEXT(int texunit, int coord, int pname, int param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexGeniEXT(texunit, coord, pname, param);
    }

    public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage1DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glMultiTexImage1DEXT(int texunit, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexImage1DEXT(texunit, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage2DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage2DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexImage3DEXT(int texunit, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexImage3DEXT(
                texunit,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glMultiTexParameterEXT(int texunit, int target, int pname, java.nio.FloatBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterfvEXT(texunit, target, pname, param);
    }

    public static void glMultiTexParameterEXT(int texunit, int target, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterivEXT(texunit, target, pname, param);
    }

    public static void glMultiTexParameterIEXT(int texunit, int target, int pname, int param) {

        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterIivEXT(texunit, target, pname, new int[] { param });
    }

    public static void glMultiTexParameterIEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterIivEXT(texunit, target, pname, params);
    }

    public static void glMultiTexParameterIuEXT(int texunit, int target, int pname, int param) {

        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterIuivEXT(texunit, target, pname, new int[] { param });
    }

    public static void glMultiTexParameterIuEXT(int texunit, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterIuivEXT(texunit, target, pname, params);
    }

    public static void glMultiTexParameterfEXT(int texunit, int target, int pname, float param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameterfEXT(texunit, target, pname, param);
    }

    public static void glMultiTexParameteriEXT(int texunit, int target, int pname, int param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexParameteriEXT(texunit, target, pname, param);
    }

    public static void glMultiTexRenderbufferEXT(int texunit, int target, int renderbuffer) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexRenderbufferEXT(texunit, target, renderbuffer);
    }

    public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format,
            int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, type, pixels_buffer_offset);
    }

    public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format,
            int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, type, pixels);
    }

    public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format,
            int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, type, pixels);
    }

    public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format,
            int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, type, pixels);
    }

    public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format,
            int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, type, pixels);
    }

    public static void glMultiTexSubImage1DEXT(int texunit, int target, int level, int xoffset, int width, int format,
            int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage1DEXT(texunit, target, level, xoffset, width, format, type, pixels);
    }

    public static void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage2DEXT(
                texunit,
                target,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage2DEXT(texunit, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage2DEXT(texunit, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage2DEXT(texunit, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage2DEXT(texunit, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glMultiTexSubImage2DEXT(int texunit, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glMultiTexSubImage2DEXT(texunit, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage3DEXT(
                texunit,
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

    public static void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage3DEXT(
                texunit,
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
                pixels);
    }

    public static void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage3DEXT(
                texunit,
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
                pixels);
    }

    public static void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage3DEXT(
                texunit,
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
                pixels);
    }

    public static void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage3DEXT(
                texunit,
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
                pixels);
    }

    public static void glMultiTexSubImage3DEXT(int texunit, int target, int level, int xoffset, int yoffset,
            int zoffset, int width, int height, int depth, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glMultiTexSubImage3DEXT(
                texunit,
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
                pixels);
    }

    public static void glNamedBufferDataEXT(int buffer, long data_size, int usage) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferDataEXT(buffer, data_size, usage);
    }

    public static void glNamedBufferDataEXT(int buffer, java.nio.ByteBuffer data, int usage) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferDataEXT(buffer, data, usage);
    }

    public static void glNamedBufferDataEXT(int buffer, java.nio.DoubleBuffer data, int usage) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferDataEXT(buffer, data, usage);
    }

    public static void glNamedBufferDataEXT(int buffer, java.nio.FloatBuffer data, int usage) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferDataEXT(buffer, data, usage);
    }

    public static void glNamedBufferDataEXT(int buffer, java.nio.IntBuffer data, int usage) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferDataEXT(buffer, data, usage);
    }

    public static void glNamedBufferDataEXT(int buffer, java.nio.ShortBuffer data, int usage) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferDataEXT(buffer, data, usage);
    }

    public static void glNamedBufferSubDataEXT(int buffer, long offset, java.nio.ByteBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glNamedBufferSubDataEXT(int buffer, long offset, java.nio.DoubleBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glNamedBufferSubDataEXT(int buffer, long offset, java.nio.FloatBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glNamedBufferSubDataEXT(int buffer, long offset, java.nio.IntBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glNamedBufferSubDataEXT(int buffer, long offset, java.nio.ShortBuffer data) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedBufferSubDataEXT(buffer, offset, data);
    }

    public static void glNamedCopyBufferSubDataEXT(int readBuffer, int writeBuffer, long readoffset, long writeoffset,
            long size) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedCopyBufferSubDataEXT(readBuffer, writeBuffer, readoffset, writeoffset, size);
    }

    public static void glNamedFramebufferRenderbufferEXT(int framebuffer, int attachment, int renderbuffertarget,
            int renderbuffer) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedFramebufferRenderbufferEXT(framebuffer, attachment, renderbuffertarget, renderbuffer);
    }

    public static void glNamedFramebufferTexture1DEXT(int framebuffer, int attachment, int textarget, int texture,
            int level) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedFramebufferTexture1DEXT(framebuffer, attachment, textarget, texture, level);
    }

    public static void glNamedFramebufferTexture2DEXT(int framebuffer, int attachment, int textarget, int texture,
            int level) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedFramebufferTexture2DEXT(framebuffer, attachment, textarget, texture, level);
    }

    public static void glNamedFramebufferTexture3DEXT(int framebuffer, int attachment, int textarget, int texture,
            int level, int zoffset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedFramebufferTexture3DEXT(framebuffer, attachment, textarget, texture, level, zoffset);
    }

    public static void glNamedFramebufferTextureEXT(int framebuffer, int attachment, int texture, int level) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedFramebufferTextureEXT(framebuffer, attachment, texture, level);
    }

    public static void glNamedFramebufferTextureFaceEXT(int framebuffer, int attachment, int texture, int level,
            int face) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedFramebufferTextureFaceEXT(framebuffer, attachment, texture, level, face);
    }

    public static void glNamedFramebufferTextureLayerEXT(int framebuffer, int attachment, int texture, int level,
            int layer) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedFramebufferTextureLayerEXT(framebuffer, attachment, texture, level, layer);
    }

    public static void glNamedProgramLocalParameter4EXT(int program, int target, int index,
            java.nio.DoubleBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameter4dvEXT(program, target, index, params);
    }

    public static void glNamedProgramLocalParameter4EXT(int program, int target, int index,
            java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameter4fvEXT(program, target, index, params);
    }

    public static void glNamedProgramLocalParameter4dEXT(int program, int target, int index, double x, double y,
            double z, double w) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameter4dEXT(program, target, index, x, y, z, w);
    }

    public static void glNamedProgramLocalParameter4fEXT(int program, int target, int index, float x, float y, float z,
            float w) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameter4fEXT(program, target, index, x, y, z, w);
    }

    public static void glNamedProgramLocalParameterI4EXT(int program, int target, int index,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameterI4ivEXT(program, target, index, params);
    }

    public static void glNamedProgramLocalParameterI4iEXT(int program, int target, int index, int x, int y, int z,
            int w) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameterI4iEXT(program, target, index, x, y, z, w);
    }

    public static void glNamedProgramLocalParameterI4uEXT(int program, int target, int index,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameterI4uivEXT(program, target, index, params);
    }

    public static void glNamedProgramLocalParameterI4uiEXT(int program, int target, int index, int x, int y, int z,
            int w) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameterI4uiEXT(program, target, index, x, y, z, w);
    }

    public static void glNamedProgramLocalParameters4EXT(int program, int target, int index,
            java.nio.FloatBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParameters4fvEXT(program, target, index, params);
    }

    public static void glNamedProgramLocalParametersI4EXT(int program, int target, int index,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParametersI4ivEXT(program, target, index, params);
    }

    public static void glNamedProgramLocalParametersI4uEXT(int program, int target, int index,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramLocalParametersI4uivEXT(program, target, index, params);
    }

    public static void glNamedProgramStringEXT(int program, int target, int format, java.nio.ByteBuffer string) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedProgramStringEXT(program, target, format, string);
    }

    public static void glNamedRenderbufferStorageEXT(int renderbuffer, int internalformat, int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedRenderbufferStorageEXT(renderbuffer, internalformat, width, height);
    }

    public static void glNamedRenderbufferStorageMultisampleCoverageEXT(int renderbuffer, int coverageSamples,
            int colorSamples, int internalformat, int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess.glNamedRenderbufferStorageMultisampleCoverageEXT(
                renderbuffer,
                coverageSamples,
                colorSamples,
                internalformat,
                width,
                height);
    }

    public static void glNamedRenderbufferStorageMultisampleEXT(int renderbuffer, int samples, int internalformat,
            int width, int height) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glNamedRenderbufferStorageMultisampleEXT(renderbuffer, samples, internalformat, width, height);
    }

    public static void glProgramUniform1EXT(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform1fvEXT(program, location, value);
    }

    public static void glProgramUniform1EXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform1ivEXT(program, location, value);
    }

    public static void glProgramUniform1fEXT(int program, int location, float v0) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform1fEXT(program, location, v0);
    }

    public static void glProgramUniform1iEXT(int program, int location, int v0) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform1iEXT(program, location, v0);
    }

    public static void glProgramUniform1uEXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform1uivEXT(program, location, value);
    }

    public static void glProgramUniform1uiEXT(int program, int location, int v0) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform1uiEXT(program, location, v0);
    }

    public static void glProgramUniform2EXT(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform2fvEXT(program, location, value);
    }

    public static void glProgramUniform2EXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform2ivEXT(program, location, value);
    }

    public static void glProgramUniform2fEXT(int program, int location, float v0, float v1) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform2fEXT(program, location, v0, v1);
    }

    public static void glProgramUniform2iEXT(int program, int location, int v0, int v1) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform2iEXT(program, location, v0, v1);
    }

    public static void glProgramUniform2uEXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform2uivEXT(program, location, value);
    }

    public static void glProgramUniform2uiEXT(int program, int location, int v0, int v1) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform2uiEXT(program, location, v0, v1);
    }

    public static void glProgramUniform3EXT(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform3fvEXT(program, location, value);
    }

    public static void glProgramUniform3EXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform3ivEXT(program, location, value);
    }

    public static void glProgramUniform3fEXT(int program, int location, float v0, float v1, float v2) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform3fEXT(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3iEXT(int program, int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform3iEXT(program, location, v0, v1, v2);
    }

    public static void glProgramUniform3uEXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform3uivEXT(program, location, value);
    }

    public static void glProgramUniform3uiEXT(int program, int location, int v0, int v1, int v2) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform3uiEXT(program, location, v0, v1, v2);
    }

    public static void glProgramUniform4EXT(int program, int location, java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform4fvEXT(program, location, value);
    }

    public static void glProgramUniform4EXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform4ivEXT(program, location, value);
    }

    public static void glProgramUniform4fEXT(int program, int location, float v0, float v1, float v2, float v3) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform4fEXT(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4iEXT(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform4iEXT(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniform4uEXT(int program, int location, java.nio.IntBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform4uivEXT(program, location, value);
    }

    public static void glProgramUniform4uiEXT(int program, int location, int v0, int v1, int v2, int v3) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniform4uiEXT(program, location, v0, v1, v2, v3);
    }

    public static void glProgramUniformMatrix2EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix2fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2x3EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix2x3fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix2x4EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix2x4fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix3fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3x2EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix3x2fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix3x4EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix3x4fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix4fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4x2EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix4x2fvEXT(program, location, transpose, value);
    }

    public static void glProgramUniformMatrix4x3EXT(int program, int location, boolean transpose,
            java.nio.FloatBuffer value) {
        org.lwjgl.opengl.EXTDirectStateAccess.glProgramUniformMatrix4x3fvEXT(program, location, transpose, value);
    }

    public static void glPushClientAttribDefaultEXT(int mask) {
        org.lwjgl.opengl.EXTDirectStateAccess.glPushClientAttribDefaultEXT(mask);
    }

    public static void glTextureBufferEXT(int texture, int target, int internalformat, int buffer) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureBufferEXT(texture, target, internalformat, buffer);
    }

    public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage1DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTextureImage1DEXT(int texture, int target, int level, int internalformat, int width,
            int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureImage1DEXT(texture, target, level, internalformat, width, border, format, type, pixels);
    }

    public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage2DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage2DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureImage3DEXT(int texture, int target, int level, int internalformat, int width,
            int height, int depth, int border, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureImage3DEXT(
                texture,
                target,
                level,
                internalformat,
                width,
                height,
                depth,
                border,
                format,
                type,
                pixels);
    }

    public static void glTextureParameterEXT(int texture, int target, int pname, java.nio.FloatBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterfvEXT(texture, target, pname, param);
    }

    public static void glTextureParameterEXT(int texture, int target, int pname, java.nio.IntBuffer param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterivEXT(texture, target, pname, param);
    }

    public static void glTextureParameterIEXT(int texture, int target, int pname, int param) {

        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterIivEXT(texture, target, pname, new int[] { param });
    }

    public static void glTextureParameterIEXT(int texture, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterIivEXT(texture, target, pname, params);
    }

    public static void glTextureParameterIuEXT(int texture, int target, int pname, int param) {

        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterIuivEXT(texture, target, pname, new int[] { param });
    }

    public static void glTextureParameterIuEXT(int texture, int target, int pname, java.nio.IntBuffer params) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterIuivEXT(texture, target, pname, params);
    }

    public static void glTextureParameterfEXT(int texture, int target, int pname, float param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameterfEXT(texture, target, pname, param);
    }

    public static void glTextureParameteriEXT(int texture, int target, int pname, int param) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureParameteriEXT(texture, target, pname, param);
    }

    public static void glTextureRenderbufferEXT(int texture, int target, int renderbuffer) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureRenderbufferEXT(texture, target, renderbuffer);
    }

    public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format,
            int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, pixels_buffer_offset);
    }

    public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format,
            int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format,
            int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format,
            int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format,
            int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage1DEXT(int texture, int target, int level, int xoffset, int width, int format,
            int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage1DEXT(texture, target, level, xoffset, width, format, type, pixels);
    }

    public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage2DEXT(
                texture,
                target,
                level,
                xoffset,
                yoffset,
                width,
                height,
                format,
                type,
                pixels_buffer_offset);
    }

    public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage2DEXT(int texture, int target, int level, int xoffset, int yoffset, int width,
            int height, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glTextureSubImage2DEXT(texture, target, level, xoffset, yoffset, width, height, format, type, pixels);
    }

    public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int type, long pixels_buffer_offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage3DEXT(
                texture,
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

    public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int type, java.nio.ByteBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage3DEXT(
                texture,
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
                pixels);
    }

    public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int type, java.nio.DoubleBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage3DEXT(
                texture,
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
                pixels);
    }

    public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int type, java.nio.FloatBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage3DEXT(
                texture,
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
                pixels);
    }

    public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int type, java.nio.IntBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage3DEXT(
                texture,
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
                pixels);
    }

    public static void glTextureSubImage3DEXT(int texture, int target, int level, int xoffset, int yoffset, int zoffset,
            int width, int height, int depth, int format, int type, java.nio.ShortBuffer pixels) {
        org.lwjgl.opengl.EXTDirectStateAccess.glTextureSubImage3DEXT(
                texture,
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
                pixels);
    }

    public static boolean glUnmapNamedBufferEXT(int buffer) {
        return org.lwjgl.opengl.EXTDirectStateAccess.glUnmapNamedBufferEXT(buffer);
    }

    public static void glVertexArrayColorOffsetEXT(int vaobj, int buffer, int size, int type, int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayColorOffsetEXT(vaobj, buffer, size, type, stride, offset);
    }

    public static void glVertexArrayEdgeFlagOffsetEXT(int vaobj, int buffer, int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayEdgeFlagOffsetEXT(vaobj, buffer, stride, offset);
    }

    public static void glVertexArrayFogCoordOffsetEXT(int vaobj, int buffer, int type, int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayFogCoordOffsetEXT(vaobj, buffer, type, stride, offset);
    }

    public static void glVertexArrayIndexOffsetEXT(int vaobj, int buffer, int type, int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayIndexOffsetEXT(vaobj, buffer, type, stride, offset);
    }

    public static void glVertexArrayMultiTexCoordOffsetEXT(int vaobj, int buffer, int texunit, int size, int type,
            int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glVertexArrayMultiTexCoordOffsetEXT(vaobj, buffer, texunit, size, type, stride, offset);
    }

    public static void glVertexArrayNormalOffsetEXT(int vaobj, int buffer, int type, int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayNormalOffsetEXT(vaobj, buffer, type, stride, offset);
    }

    public static void glVertexArraySecondaryColorOffsetEXT(int vaobj, int buffer, int size, int type, int stride,
            long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glVertexArraySecondaryColorOffsetEXT(vaobj, buffer, size, type, stride, offset);
    }

    public static void glVertexArrayTexCoordOffsetEXT(int vaobj, int buffer, int size, int type, int stride,
            long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayTexCoordOffsetEXT(vaobj, buffer, size, type, stride, offset);
    }

    public static void glVertexArrayVertexAttribIOffsetEXT(int vaobj, int buffer, int index, int size, int type,
            int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glVertexArrayVertexAttribIOffsetEXT(vaobj, buffer, index, size, type, stride, offset);
    }

    public static void glVertexArrayVertexAttribOffsetEXT(int vaobj, int buffer, int index, int size, int type,
            boolean normalized, int stride, long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess
                .glVertexArrayVertexAttribOffsetEXT(vaobj, buffer, index, size, type, normalized, stride, offset);
    }

    public static void glVertexArrayVertexOffsetEXT(int vaobj, int buffer, int size, int type, int stride,
            long offset) {
        org.lwjgl.opengl.EXTDirectStateAccess.glVertexArrayVertexOffsetEXT(vaobj, buffer, size, type, stride, offset);
    }
}

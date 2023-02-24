package org.lwjglx.opengl;

public class GL31 {

    public static final int GL_ACTIVE_UNIFORM_BLOCKS = (int) 35382;
    public static final int GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = (int) 35381;
    public static final int GL_COPY_READ_BUFFER = (int) 36662;
    public static final int GL_COPY_READ_BUFFER_BINDING = (int) 36662;
    public static final int GL_COPY_WRITE_BUFFER = (int) 36663;
    public static final int GL_COPY_WRITE_BUFFER_BINDING = (int) 36663;
    public static final int GL_INVALID_INDEX = (int) -1;
    public static final int GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = (int) 35379;
    public static final int GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS = (int) 35378;
    public static final int GL_MAX_COMBINED_UNIFORM_BLOCKS = (int) 35374;
    public static final int GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = (int) 35377;
    public static final int GL_MAX_FRAGMENT_UNIFORM_BLOCKS = (int) 35373;
    public static final int GL_MAX_GEOMETRY_UNIFORM_BLOCKS = (int) 35372;
    public static final int GL_MAX_RECTANGLE_TEXTURE_SIZE = (int) 34040;
    public static final int GL_MAX_TEXTURE_BUFFER_SIZE = (int) 35883;
    public static final int GL_MAX_UNIFORM_BLOCK_SIZE = (int) 35376;
    public static final int GL_MAX_UNIFORM_BUFFER_BINDINGS = (int) 35375;
    public static final int GL_MAX_VERTEX_UNIFORM_BLOCKS = (int) 35371;
    public static final int GL_PRIMITIVE_RESTART = (int) 36765;
    public static final int GL_PRIMITIVE_RESTART_INDEX = (int) 36766;
    public static final int GL_PROXY_TEXTURE_RECTANGLE = (int) 34039;
    public static final int GL_R16_SNORM = (int) 36760;
    public static final int GL_R8_SNORM = (int) 36756;
    public static final int GL_RED_SNORM = (int) 36752;
    public static final int GL_RG16_SNORM = (int) 36761;
    public static final int GL_RG8_SNORM = (int) 36757;
    public static final int GL_RGB16_SNORM = (int) 36762;
    public static final int GL_RGB8_SNORM = (int) 36758;
    public static final int GL_RGBA16_SNORM = (int) 36763;
    public static final int GL_RGBA8_SNORM = (int) 36759;
    public static final int GL_RGBA_SNORM = (int) 36755;
    public static final int GL_RGB_SNORM = (int) 36754;
    public static final int GL_RG_SNORM = (int) 36753;
    public static final int GL_SAMPLER_2D_RECT = (int) 35683;
    public static final int GL_SAMPLER_2D_RECT_SHADOW = (int) 35684;
    public static final int GL_SIGNED_NORMALIZED = (int) 36764;
    public static final int GL_TEXTURE_BINDING_BUFFER = (int) 35884;
    public static final int GL_TEXTURE_BINDING_RECTANGLE = (int) 34038;
    public static final int GL_TEXTURE_BUFFER = (int) 35882;
    public static final int GL_TEXTURE_BUFFER_DATA_STORE_BINDING = (int) 35885;
    public static final int GL_TEXTURE_BUFFER_FORMAT = (int) 35886;
    public static final int GL_TEXTURE_RECTANGLE = (int) 34037;
    public static final int GL_UNIFORM_ARRAY_STRIDE = (int) 35388;
    public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORMS = (int) 35394;
    public static final int GL_UNIFORM_BLOCK_ACTIVE_UNIFORM_INDICES = (int) 35395;
    public static final int GL_UNIFORM_BLOCK_BINDING = (int) 35391;
    public static final int GL_UNIFORM_BLOCK_DATA_SIZE = (int) 35392;
    public static final int GL_UNIFORM_BLOCK_INDEX = (int) 35386;
    public static final int GL_UNIFORM_BLOCK_NAME_LENGTH = (int) 35393;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_FRAGMENT_SHADER = (int) 35398;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_GEOMETRY_SHADER = (int) 35397;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_VERTEX_SHADER = (int) 35396;
    public static final int GL_UNIFORM_BUFFER = (int) 35345;
    public static final int GL_UNIFORM_BUFFER_BINDING = (int) 35368;
    public static final int GL_UNIFORM_BUFFER_OFFSET_ALIGNMENT = (int) 35380;
    public static final int GL_UNIFORM_BUFFER_SIZE = (int) 35370;
    public static final int GL_UNIFORM_BUFFER_START = (int) 35369;
    public static final int GL_UNIFORM_IS_ROW_MAJOR = (int) 35390;
    public static final int GL_UNIFORM_MATRIX_STRIDE = (int) 35389;
    public static final int GL_UNIFORM_NAME_LENGTH = (int) 35385;
    public static final int GL_UNIFORM_OFFSET = (int) 35387;
    public static final int GL_UNIFORM_SIZE = (int) 35384;
    public static final int GL_UNIFORM_TYPE = (int) 35383;

    public static void glCopyBufferSubData(int readtarget, int writetarget, long readoffset, long writeoffset,
            long size) {
        org.lwjgl.opengl.GL31.glCopyBufferSubData(readtarget, writetarget, readoffset, writeoffset, size);
    }

    public static void glDrawArraysInstanced(int mode, int first, int count, int primcount) {
        org.lwjgl.opengl.GL31.glDrawArraysInstanced(mode, first, count, primcount);
    }

    public static void glDrawElementsInstanced(int mode, int indices_count, int type, long indices_buffer_offset,
            int primcount) {
        org.lwjgl.opengl.GL31.glDrawElementsInstanced(mode, indices_count, type, indices_buffer_offset, primcount);
    }

    public static void glDrawElementsInstanced(int mode, java.nio.ByteBuffer indices, int primcount) {
        org.lwjgl.opengl.GL31.glDrawElementsInstanced(mode, indices, primcount);
    }

    public static void glDrawElementsInstanced(int mode, java.nio.IntBuffer indices, int primcount) {
        org.lwjgl.opengl.GL31.glDrawElementsInstanced(mode, indices, primcount);
    }

    public static void glDrawElementsInstanced(int mode, java.nio.ShortBuffer indices, int primcount) {
        org.lwjgl.opengl.GL31.glDrawElementsInstanced(mode, indices, primcount);
    }

    public static void glGetActiveUniformBlock(int program, int uniformBlockIndex, int pname,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL31.glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    }

    public static java.lang.String glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize) {
        return org.lwjgl.opengl.GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize);
    }

    public static void glGetActiveUniformBlockName(int program, int uniformBlockIndex, java.nio.IntBuffer length,
            java.nio.ByteBuffer uniformBlockName) {
        org.lwjgl.opengl.GL31.glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    public static int glGetActiveUniformBlocki(int program, int uniformBlockIndex, int pname) {
        return org.lwjgl.opengl.GL31.glGetActiveUniformBlocki(program, uniformBlockIndex, pname);
    }

    public static java.lang.String glGetActiveUniformName(int program, int uniformIndex, int bufSize) {
        return org.lwjgl.opengl.GL31.glGetActiveUniformName(program, uniformIndex, bufSize);
    }

    public static void glGetActiveUniformName(int program, int uniformIndex, java.nio.IntBuffer length,
            java.nio.ByteBuffer uniformName) {
        org.lwjgl.opengl.GL31.glGetActiveUniformName(program, uniformIndex, length, uniformName);
    }

    public static void glGetActiveUniforms(int program, java.nio.IntBuffer uniformIndices, int pname,
            java.nio.IntBuffer params) {
        org.lwjgl.opengl.GL31.glGetActiveUniformsiv(program, uniformIndices, pname, params);
    }

    public static int glGetActiveUniformsi(int program, int uniformIndex, int pname) {
        return org.lwjgl.opengl.GL31.glGetActiveUniformsi(program, uniformIndex, pname);
    }

    public static int glGetUniformBlockIndex(int program, java.lang.CharSequence uniformBlockName) {
        return org.lwjgl.opengl.GL31.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public static int glGetUniformBlockIndex(int program, java.nio.ByteBuffer uniformBlockName) {
        return org.lwjgl.opengl.GL31.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public static void glGetUniformIndices(int program, java.lang.CharSequence[] uniformNames,
            java.nio.IntBuffer uniformIndices) {
        org.lwjgl.opengl.GL31.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    public static void glPrimitiveRestartIndex(int index) {
        org.lwjgl.opengl.GL31.glPrimitiveRestartIndex(index);
    }

    public static void glTexBuffer(int target, int internalformat, int buffer) {
        org.lwjgl.opengl.GL31.glTexBuffer(target, internalformat, buffer);
    }

    public static void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        org.lwjgl.opengl.GL31.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }
}

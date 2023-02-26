package org.lwjglx.opengl;

public class ARBUniformBufferObject {

    public static final int GL_ACTIVE_UNIFORM_BLOCKS = (int) 35382;
    public static final int GL_ACTIVE_UNIFORM_BLOCK_MAX_NAME_LENGTH = (int) 35381;
    public static final int GL_INVALID_INDEX = (int) -1;
    public static final int GL_MAX_COMBINED_FRAGMENT_UNIFORM_COMPONENTS = (int) 35379;
    public static final int GL_MAX_COMBINED_GEOMETRY_UNIFORM_COMPONENTS = (int) 35378;
    public static final int GL_MAX_COMBINED_UNIFORM_BLOCKS = (int) 35374;
    public static final int GL_MAX_COMBINED_VERTEX_UNIFORM_COMPONENTS = (int) 35377;
    public static final int GL_MAX_FRAGMENT_UNIFORM_BLOCKS = (int) 35373;
    public static final int GL_MAX_GEOMETRY_UNIFORM_BLOCKS = (int) 35372;
    public static final int GL_MAX_UNIFORM_BLOCK_SIZE = (int) 35376;
    public static final int GL_MAX_UNIFORM_BUFFER_BINDINGS = (int) 35375;
    public static final int GL_MAX_VERTEX_UNIFORM_BLOCKS = (int) 35371;
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

    public static void glBindBufferBase(int target, int index, int buffer) {
        org.lwjgl.opengl.ARBUniformBufferObject.glBindBufferBase(target, index, buffer);
    }

    public static void glBindBufferRange(int target, int index, int buffer, long offset, long size) {
        org.lwjgl.opengl.ARBUniformBufferObject.glBindBufferRange(target, index, buffer, offset, size);
    }

    public static java.lang.String glGetActiveUniformBlockName(int program, int uniformBlockIndex, int bufSize) {
        return org.lwjgl.opengl.ARBUniformBufferObject.glGetActiveUniformBlockName(program, uniformBlockIndex, bufSize);
    }

    public static void glGetActiveUniformBlockName(int program, int uniformBlockIndex, java.nio.IntBuffer length,
            java.nio.ByteBuffer uniformBlockName) {
        org.lwjgl.opengl.ARBUniformBufferObject
                .glGetActiveUniformBlockName(program, uniformBlockIndex, length, uniformBlockName);
    }

    public static int glGetActiveUniformBlocki(int program, int uniformBlockIndex, int pname) {
        return org.lwjgl.opengl.ARBUniformBufferObject.glGetActiveUniformBlocki(program, uniformBlockIndex, pname);
    }

    public static java.lang.String glGetActiveUniformName(int program, int uniformIndex, int bufSize) {
        return org.lwjgl.opengl.ARBUniformBufferObject.glGetActiveUniformName(program, uniformIndex, bufSize);
    }

    public static void glGetActiveUniformName(int program, int uniformIndex, java.nio.IntBuffer length,
            java.nio.ByteBuffer uniformName) {
        org.lwjgl.opengl.ARBUniformBufferObject.glGetActiveUniformName(program, uniformIndex, length, uniformName);
    }

    public static int glGetActiveUniformsi(int program, int uniformIndex, int pname) {
        return org.lwjgl.opengl.ARBUniformBufferObject.glGetActiveUniformsi(program, uniformIndex, pname);
    }

    public static int glGetUniformBlockIndex(int program, java.lang.CharSequence uniformBlockName) {
        return org.lwjgl.opengl.ARBUniformBufferObject.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public static int glGetUniformBlockIndex(int program, java.nio.ByteBuffer uniformBlockName) {
        return org.lwjgl.opengl.ARBUniformBufferObject.glGetUniformBlockIndex(program, uniformBlockName);
    }

    public static void glGetUniformIndices(int program, java.lang.CharSequence[] uniformNames,
            java.nio.IntBuffer uniformIndices) {
        org.lwjgl.opengl.ARBUniformBufferObject.glGetUniformIndices(program, uniformNames, uniformIndices);
    }

    public static void glUniformBlockBinding(int program, int uniformBlockIndex, int uniformBlockBinding) {
        org.lwjgl.opengl.ARBUniformBufferObject.glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    }
}

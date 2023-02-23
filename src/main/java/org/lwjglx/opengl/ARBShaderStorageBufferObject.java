package org.lwjglx.opengl;

public class ARBShaderStorageBufferObject {

    public static final int GL_MAX_COMBINED_SHADER_OUTPUT_RESOURCES = (int) 36665;
    public static final int GL_MAX_COMBINED_SHADER_STORAGE_BLOCKS = (int) 37084;
    public static final int GL_MAX_COMPUTE_SHADER_STORAGE_BLOCKS = (int) 37083;
    public static final int GL_MAX_FRAGMENT_SHADER_STORAGE_BLOCKS = (int) 37082;
    public static final int GL_MAX_GEOMETRY_SHADER_STORAGE_BLOCKS = (int) 37079;
    public static final int GL_MAX_SHADER_STORAGE_BLOCK_SIZE = (int) 37086;
    public static final int GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS = (int) 37085;
    public static final int GL_MAX_TESS_CONTROL_SHADER_STORAGE_BLOCKS = (int) 37080;
    public static final int GL_MAX_TESS_EVALUATION_SHADER_STORAGE_BLOCKS = (int) 37081;
    public static final int GL_MAX_VERTEX_SHADER_STORAGE_BLOCKS = (int) 37078;
    public static final int GL_SHADER_STORAGE_BARRIER_BIT = (int) 8192;
    public static final int GL_SHADER_STORAGE_BUFFER = (int) 37074;
    public static final int GL_SHADER_STORAGE_BUFFER_BINDING = (int) 37075;
    public static final int GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT = (int) 37087;
    public static final int GL_SHADER_STORAGE_BUFFER_SIZE = (int) 37077;
    public static final int GL_SHADER_STORAGE_BUFFER_START = (int) 37076;

    public static void glShaderStorageBlockBinding(int program, int storageBlockIndex, int storageBlockBinding) {
        org.lwjgl.opengl.ARBShaderStorageBufferObject
                .glShaderStorageBlockBinding(program, storageBlockIndex, storageBlockBinding);
    }
}

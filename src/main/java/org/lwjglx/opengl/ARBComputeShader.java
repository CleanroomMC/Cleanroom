package org.lwjglx.opengl;

public class ARBComputeShader {

    public static final int GL_ATOMIC_COUNTER_BUFFER_REFERENCED_BY_COMPUTE_SHADER = (int) 37101;
    public static final int GL_COMPUTE_SHADER = (int) 37305;
    public static final int GL_COMPUTE_SHADER_BIT = (int) 32;
    public static final int GL_COMPUTE_WORK_GROUP_SIZE = (int) 33383;
    public static final int GL_DISPATCH_INDIRECT_BUFFER = (int) 37102;
    public static final int GL_DISPATCH_INDIRECT_BUFFER_BINDING = (int) 37103;
    public static final int GL_MAX_COMBINED_COMPUTE_UNIFORM_COMPONENTS = (int) 33382;
    public static final int GL_MAX_COMPUTE_ATOMIC_COUNTERS = (int) 33381;
    public static final int GL_MAX_COMPUTE_ATOMIC_COUNTER_BUFFERS = (int) 33380;
    public static final int GL_MAX_COMPUTE_IMAGE_UNIFORMS = (int) 37309;
    public static final int GL_MAX_COMPUTE_SHARED_MEMORY_SIZE = (int) 33378;
    public static final int GL_MAX_COMPUTE_TEXTURE_IMAGE_UNITS = (int) 37308;
    public static final int GL_MAX_COMPUTE_UNIFORM_BLOCKS = (int) 37307;
    public static final int GL_MAX_COMPUTE_UNIFORM_COMPONENTS = (int) 33379;
    public static final int GL_MAX_COMPUTE_WORK_GROUP_COUNT = (int) 37310;
    public static final int GL_MAX_COMPUTE_WORK_GROUP_INVOCATIONS = (int) 37099;
    public static final int GL_MAX_COMPUTE_WORK_GROUP_SIZE = (int) 37311;
    public static final int GL_UNIFORM_BLOCK_REFERENCED_BY_COMPUTE_SHADER = (int) 37100;

    public static void glDispatchCompute(int num_groups_x, int num_groups_y, int num_groups_z) {
        org.lwjgl.opengl.ARBComputeShader.glDispatchCompute(num_groups_x, num_groups_y, num_groups_z);
    }

    public static void glDispatchComputeIndirect(long indirect) {
        org.lwjgl.opengl.ARBComputeShader.glDispatchComputeIndirect(indirect);
    }
}

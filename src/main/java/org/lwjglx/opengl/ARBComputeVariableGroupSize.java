package org.lwjglx.opengl;

public class ARBComputeVariableGroupSize {

    public static final int GL_MAX_COMPUTE_FIXED_GROUP_INVOCATIONS_ARB = (int) 37099;
    public static final int GL_MAX_COMPUTE_FIXED_GROUP_SIZE_ARB = (int) 37311;
    public static final int GL_MAX_COMPUTE_VARIABLE_GROUP_INVOCATIONS_ARB = (int) 37700;
    public static final int GL_MAX_COMPUTE_VARIABLE_GROUP_SIZE_ARB = (int) 37701;

    public static void glDispatchComputeGroupSizeARB(int num_groups_x, int num_groups_y, int num_groups_z,
            int group_size_x, int group_size_y, int group_size_z) {
        org.lwjgl.opengl.ARBComputeVariableGroupSize.glDispatchComputeGroupSizeARB(
                num_groups_x,
                num_groups_y,
                num_groups_z,
                group_size_x,
                group_size_y,
                group_size_z);
    }
}

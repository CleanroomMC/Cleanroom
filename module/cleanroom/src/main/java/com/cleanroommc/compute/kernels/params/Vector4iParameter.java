package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import org.joml.Vector4i;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

record Vector4iParameter(int x, int y, int z, int w) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg4i(kernel, index, x, y, z, w), index, new Vector4i(x,y,z,w));
    }
}

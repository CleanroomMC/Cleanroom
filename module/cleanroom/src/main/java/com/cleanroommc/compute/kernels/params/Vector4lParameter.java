package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

record Vector4lParameter(long x, long y, long z, long w) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg4l(kernel, index, x, y, z, w), index, String.format("{%d; %d; %d; %d}", x, y, z, w));
    }
}

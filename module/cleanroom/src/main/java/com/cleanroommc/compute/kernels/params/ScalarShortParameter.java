package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

record ScalarShortParameter(short value) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg1s(kernel, index, value), index, value);
    }
}

package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

public record BufferParameter(Buffer buffer) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg1p(kernel, index, buffer.handle), index, buffer);
    }
}

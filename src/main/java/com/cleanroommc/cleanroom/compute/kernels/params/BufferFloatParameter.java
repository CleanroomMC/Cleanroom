package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;

import java.nio.FloatBuffer;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

public record BufferFloatParameter(FloatBuffer value) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
    }
}

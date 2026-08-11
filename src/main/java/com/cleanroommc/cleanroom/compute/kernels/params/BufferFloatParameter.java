package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import java.nio.FloatBuffer;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

record BufferFloatParameter(FloatBuffer value) implements KernelParameter {

    public BufferFloatParameter {
        Preconditions.checkArgument((value.remaining() > 0 && value.remaining() < 5) || value.remaining() == 8 || value.remaining() == 16,
                "The only types supported this OpenCL function are: float, float2, float3, float4, float8 and float16.");
    }

    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
    }
}

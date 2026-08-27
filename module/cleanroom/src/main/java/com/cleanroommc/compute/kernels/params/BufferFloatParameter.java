package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import java.nio.FloatBuffer;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

/**
 * Represents a parameter that is a float buffer.
 * @param value The float buffer to pass to the kernel.
 * @apiNote The only types supported by this OpenCL function are: float, float2, float3, float4, float8, and float16.
 * @author EΣrie
 */
record BufferFloatParameter(FloatBuffer value) implements KernelParameter {

    public BufferFloatParameter {
        Preconditions.checkArgument((value.remaining() > 0 && value.remaining() < 5) || value.remaining() == 8 || value.remaining() == 16,
                "The only types supported this by OpenCL function are: float, float2, float3, float4, float8, and float16.");
    }

    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, value), index, value);
    }
}

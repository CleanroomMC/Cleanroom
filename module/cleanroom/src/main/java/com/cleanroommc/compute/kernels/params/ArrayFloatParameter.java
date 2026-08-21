package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

record ArrayFloatParameter(float... values) implements KernelParameter {

    public ArrayFloatParameter {
        Preconditions.checkArgument((values.length > 0 && values.length < 5) || values.length == 8 || values.length == 16,
                "The only types supported this OpenCL function are: float, float2, float3, float4, float8 and float16.");
    }

    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, values), index, values);
    }
}

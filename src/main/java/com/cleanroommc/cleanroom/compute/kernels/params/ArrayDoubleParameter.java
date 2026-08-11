package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

record ArrayDoubleParameter(double... values) implements KernelParameter {

    public ArrayDoubleParameter {
        Preconditions.checkArgument((values.length > 0 && values.length < 5) || values.length == 8 || values.length == 16,
                "The only types supported this OpenCL function are: double, double2, double3, double4, double8 and double16.");
    }

    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, values), index, values);
    }
}

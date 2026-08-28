package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

/**
 * Represents a parameter that is an array of ints.
 * @param values The values to pass to the kernel.
 * @apiNote The only types supported by this OpenCL function are: int, int2, int3, int4, int8, int16, and their unsigned variants.
 * @author EΣrie
 */
record ArrayIntParameter(int... values) implements KernelParameter {

    public ArrayIntParameter {
        Preconditions.checkArgument((values.length > 0 && values.length < 5) || values.length == 8 || values.length == 16,
                "The only types supported by this OpenCL function are: int, int2, int3, int4, int8, int16, and their unsigned variants.");
    }

    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, values), index, values);
    }
}

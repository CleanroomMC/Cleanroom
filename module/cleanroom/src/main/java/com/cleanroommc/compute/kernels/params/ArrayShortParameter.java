package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

/**
 * Represents a parameter that is an array of shorts.
 * @param values The values to pass to the kernel.
 * @apiNote The only types supported by this OpenCL function are: short, short2, short3, short4, short8, short16, and their unsigned variants.
 * @author EΣrie
 */
record ArrayShortParameter(short... values) implements KernelParameter {

    public ArrayShortParameter {
        Preconditions.checkArgument((values.length > 0 && values.length < 5) || values.length == 8 || values.length == 16,
                "The only types supported by this OpenCL function are: short, short2, short3, short4, short8, short16, and their unsigned variants.");
    }


    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg(kernel, index, values), index, values);
    }
}

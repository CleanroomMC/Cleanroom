package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.cleanroommc.cleanroom.compute.pipes.Pipe;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

public record PipeParameter(@NonNull Pipe pipe) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg1p(kernel, index, pipe.handle), index, pipe);
    }
}

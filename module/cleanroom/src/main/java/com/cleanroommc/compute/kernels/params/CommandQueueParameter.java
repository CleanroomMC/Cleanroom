package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.cmd.CommandQueue;
import com.cleanroommc.cleanroom.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

public record CommandQueueParameter(CommandQueue queue) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg1p(kernel, index, queue.commandQueue), index, queue);
    }
}

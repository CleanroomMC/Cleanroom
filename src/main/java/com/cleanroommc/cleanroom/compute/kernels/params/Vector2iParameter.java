package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import org.joml.Vector2i;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

public record Vector2iParameter(int x, int y) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg2i(kernel, index, x, y), index, new Vector2i(x,y));
    }
}

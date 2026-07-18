package com.cleanroommc.cleanroom.compute.kernels.params;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import org.joml.Vector2d;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleKernelParamError;

public record Vector2dParameter(double x, double y) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg2d(kernel, index, x, y), index, new Vector2d(x,y));
    }
}

package com.cleanroommc.compute.kernels.params;

import com.cleanroommc.compute.errors.KernelError;
import com.cleanroommc.compute.images.Image;
import org.lwjgl.opencl.CL10;

import static com.cleanroommc.compute.utils.ErrorUtils.handleKernelParamError;

public record ImageParameter<CT>(Image<CT> image) implements KernelParameter {
    @Override
    public void bindParameter(long kernel, int index) throws KernelError, OutOfMemoryError {
        handleKernelParamError(CL10.clSetKernelArg1p(kernel, index, image.handle), index, image);
    }
}

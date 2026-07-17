package com.cleanroommc.cleanroom.compute.utils;

import com.cleanroommc.cleanroom.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;

public record ErrorUtils() {
    public static <T> void handleKernelParamError(int err, int index, T value) {
        switch (err) {
            case CL10.CL_INVALID_KERNEL -> throw new KernelError("Invalid kernel.");
            case CL10.CL_INVALID_ARG_INDEX -> throw new KernelError(String.format("Invalid kernel argument index %d", index));
            case CL10.CL_INVALID_ARG_VALUE -> throw new KernelError(String.format("Invalid kernel argument value %s.", value));
            case CL10.CL_INVALID_MEM_OBJECT -> throw new KernelError("Invalid memory object for argument.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources to set kernel parameter.");
        }
    }
}

package com.cleanroommc.cleanroom.compute.kernels;

import com.cleanroommc.cleanroom.compute.errors.CompilationError;
import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.google.common.collect.ImmutableMap;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;

import java.util.Map;

public record Kernel(long kernel, ImmutableMap<String, String> arguments) {
    public Kernel(long program, KernelMetadata meta) {
        this(createKernel(program, meta), ImmutableMap.copyOf(meta.arguments));
    }

    private static long createKernel(long program, KernelMetadata meta) {
        int[] err_codes = new int[1];
        long kernel = CL10.clCreateKernel(program, meta.kernelName, err_codes);
        switch(err_codes[0]) {
            case CL10.CL_INVALID_PROGRAM, CL10.CL_INVALID_PROGRAM_EXECUTABLE -> throw new CompilationError(String.format("Program fro kernel %s has not been compiled properly.", meta.kernelName));
            case CL10.CL_INVALID_KERNEL_NAME -> throw new KernelError(String.format("Program does not contain kernel %s.", meta.kernelName));
            case CL10.CL_INVALID_KERNEL_DEFINITION -> throw new KernelError(String.format("Kernel %s has different definitions on different devices.", meta.kernelName));
            case CL10.CL_INVALID_VALUE -> throw new NullPointerException("Kernel name is null for program.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL kernel.");
        }
        return kernel;
    }

    public void invoke(Map<String, PointerBuffer> arguments) {
        // TODO: Kernel invocation
    }
}

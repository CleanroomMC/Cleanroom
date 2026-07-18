package com.cleanroommc.cleanroom.compute.kernels;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.errors.CompilationError;
import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.cleanroommc.cleanroom.compute.kernels.params.KernelParameterList;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.system.MemoryStack;

import java.util.Map;

public record Kernel(long kernel, ImmutableMap<String, String> arguments) {
    public Kernel(long program, KernelMetadata meta) {
        this(createKernel(program, meta), ImmutableMap.copyOf(meta.arguments));
    }

    private static long createKernel(long program, @NonNull KernelMetadata meta) {
        int[] err_codes = new int[1];
        long kernel = CL10.clCreateKernel(program, meta.kernelName, err_codes);
        switch(err_codes[0]) {
            case CL10.CL_INVALID_PROGRAM, CL10.CL_INVALID_PROGRAM_EXECUTABLE -> throw new CompilationError(String.format("Program for kernel %s has not been compiled properly.", meta.kernelName));
            case CL10.CL_INVALID_KERNEL_NAME -> throw new KernelError(String.format("Program does not contain kernel %s.", meta.kernelName));
            case CL10.CL_INVALID_KERNEL_DEFINITION -> throw new KernelError(String.format("Kernel %s has different definitions on different devices.", meta.kernelName));
            case CL10.CL_INVALID_VALUE -> throw new NullPointerException("Kernel name is null for program.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL kernel.");
        }
        return kernel;
    }

    public long invoke(MemoryStack stack, long commandQueue, long device,
                       final @NonNull KernelParameterList arguments,
                       final long @Nullable [] workGroupOffsets,
                       final long @NonNull [] workGroupSizes,
                       final long... dependencies) {
        Preconditions.checkNotNull(workGroupSizes);
        Preconditions.checkNotNull(arguments);
        int dim;
        arguments.bindAllParameters(this);
        PointerBuffer offsets, sizes, local;
        if (workGroupOffsets != null) {
            Preconditions.checkArgument(workGroupSizes.length == workGroupOffsets.length);
            dim = workGroupSizes.length;
        } else {
            dim = 1;
        }
        long[] deviceSizes = Compute.instance().getDeviceItemSizes(device);
        offsets = stack.mallocPointer(dim);
        sizes = stack.mallocPointer(dim);
        local = stack.mallocPointer(dim);
        Preconditions.checkArgument(workGroupSizes.length < 3);
        sizes.put(workGroupSizes);
        for (int i = 0; i < workGroupSizes.length; i++) {
            local.put(gcd(workGroupSizes[i], deviceSizes[i]));
        }
        if (workGroupOffsets == null) {
            for (int i = 0; i < dim; i++) {
                offsets.put(0);
            }
        }
        offsets.rewind();
        sizes.rewind();
        local.rewind();
        PointerBuffer eventWaitList = null;
        if (dependencies.length > 0) {
            eventWaitList = stack.mallocPointer(dependencies.length);
            eventWaitList.put(dependencies);
            eventWaitList.rewind();
        }
        PointerBuffer event = stack.mallocPointer(1);
        switch (CL10.clEnqueueNDRangeKernel(commandQueue, kernel,
                dim, offsets, sizes, local,
                eventWaitList, event)) {
            case CL10.CL_INVALID_KERNEL_ARGS -> throw new KernelError("Invalid kernel arguments.");
            case CL10.CL_INVALID_WORK_DIMENSION -> throw new KernelError(String.format("Invalid work dimension %d", dim));
            case CL10.CL_INVALID_GLOBAL_WORK_SIZE -> throw new KernelError("Work group size is invalid.");
            case CL10.CL_INVALID_GLOBAL_OFFSET -> throw new KernelError("Invalid offset");
            case CL10.CL_INVALID_WORK_GROUP_SIZE -> throw new KernelError("Local group size is not divisible by global group size.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to invoke OpenCL kernel.");
        }
        return event.get(0);
    }

    private static long gcd(long a, long b){
        long tmp;
        while(b != 0){
            tmp = a % b;
            a = b;
            b = tmp;
        }
        return a;
    }
}

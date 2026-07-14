package com.cleanroommc.cleanroom.compute.cmd;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.errors.KernelError;
import com.cleanroommc.cleanroom.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.cleanroom.compute.kernels.Kernel;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

public class CommandQueue implements Closeable {

    public final long commandQueue;
    private final long device;

    public CommandQueue(long device) {
        int[] err = new int[1];
        commandQueue = CL20.clCreateCommandQueueWithProperties(
                Compute.instance().context,
                device,
                null,
                err
        );
        this.device = device;
        switch (err[0]) {
            case CL10.CL_INVALID_CONTEXT -> throw new RuntimeException("Invalid OpenCL context.");
            case CL10.CL_INVALID_DEVICE -> throw new UnavaliableDeviceError("Out of context device provided to command queue.");
            case CL10.CL_INVALID_VALUE -> throw new RuntimeException("Invalid value provided as command queue properties.");
            case CL10.CL_INVALID_QUEUE_PROPERTIES -> throw new RuntimeException("Queue properties unsupported by device");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL command queue.");
        }
    }

    public Event dispatchKernel(Kernel kernel,
                                final PointerBuffer[] arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long @Nullable [] workGroupSizes,
                                final long... dependencies) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return this.dispatchKernel(stack, kernel, arguments, workGroupOffsets, workGroupSizes, dependencies);
        }
    }

    public Event dispatchKernel(MemoryStack stack, Kernel kernel,
                         final PointerBuffer[] arguments,
                         final long @Nullable [] workGroupOffsets,
                         final long @Nullable [] workGroupSizes,
                         final long... dependencies) {
        return new Event(kernel.invoke(stack, commandQueue, device, arguments, workGroupOffsets, workGroupSizes, dependencies), stack);
    }

    @Override
    public void close() throws IOException {
        CL20.clReleaseCommandQueue(commandQueue);
    }

    public final class Event {
        public final long eventID;
        private final MemoryStack stack;

        Event(long eventID, MemoryStack stack) {
            this.eventID = eventID;
            this.stack = stack;
        }

        public Event next(Kernel kernel,
                          final PointerBuffer[] arguments,
                          final long @Nullable [] workGroupOffsets,
                          final long @Nullable [] workGroupSizes,
                          final long... dependencies) {
            return dispatchKernel(stack, kernel, arguments, workGroupOffsets, workGroupSizes, eventID);
        }
    }
}

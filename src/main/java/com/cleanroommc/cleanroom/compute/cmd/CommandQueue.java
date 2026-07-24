package com.cleanroommc.cleanroom.compute.cmd;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.buffers.Buffer;
import com.cleanroommc.cleanroom.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.cleanroom.compute.kernels.Kernel;
import com.cleanroommc.cleanroom.compute.kernels.params.KernelParameterList;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.io.IOException;
import java.nio.FloatBuffer;

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
                                final @NonNull KernelParameterList arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long @NonNull [] workGroupSizes,
                                final long... dependencies) {
        Preconditions.checkNotNull(workGroupSizes);
        Preconditions.checkNotNull(arguments);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return this.dispatchKernel(stack, kernel, arguments, workGroupOffsets, workGroupSizes, dependencies);
        }
    }

    public Event dispatchKernel(@NonNull MemoryStack stack, Kernel kernel,
                         final @NonNull KernelParameterList arguments,
                         final long @Nullable [] workGroupOffsets,
                         final long @NonNull [] workGroupSizes,
                         final long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(workGroupSizes);
        Preconditions.checkNotNull(arguments);
        return new Event(kernel.invoke(stack, commandQueue, device, arguments, workGroupOffsets, workGroupSizes, dependencies), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final @NonNull FloatBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return this.bufferWrite(stack, buffer, data, offset, blocking, events);
        }
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final @NonNull FloatBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return this.bufferWrite(stack, buffer, offset, data, events);
        }
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return this.bufferWrite(stack, buffer, data, blocking, events);
        }
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        try (MemoryStack stack = MemoryStack.stackPush()) {
            return this.bufferWrite(stack, buffer, data, events);
        }
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
                          final @NonNull KernelParameterList arguments,
                          final long @Nullable [] workGroupOffsets,
                          final long @NonNull [] workGroupSizes,
                          final long... dependencies) {
            Preconditions.checkNotNull(workGroupSizes);
            Preconditions.checkNotNull(arguments);
            return dispatchKernel(stack, kernel, arguments, workGroupOffsets, workGroupSizes, eventID);
        }

        public Event next(@NonNull Buffer buffer,
                          final @NonNull FloatBuffer data,
                          final boolean blocking,
                          final long offset) {
            Preconditions.checkNotNull(buffer);
            Preconditions.checkNotNull(data);
            return bufferWrite(stack, buffer, data, blocking, offset, eventID);
        }

        public Event next(@NonNull Buffer buffer,
                          final @NonNull FloatBuffer data,
                          final long offset) {
            Preconditions.checkNotNull(buffer);
            Preconditions.checkNotNull(data);
            return bufferWrite(stack, buffer, data, offset, eventID);
        }

        public Event next(@NonNull Buffer buffer,
                          final @NonNull FloatBuffer data,
                          final boolean blocking) {
            Preconditions.checkNotNull(buffer);
            Preconditions.checkNotNull(data);
            return bufferWrite(stack, buffer, data, blocking, eventID);
        }

        public Event next(@NonNull Buffer buffer,
                          final @NonNull FloatBuffer data) {
            Preconditions.checkNotNull(buffer);
            Preconditions.checkNotNull(data);
            return bufferWrite(stack, buffer, data, eventID);
        }

        public void execute() {
            CL10.clFlush(commandQueue);
        }
    }
}

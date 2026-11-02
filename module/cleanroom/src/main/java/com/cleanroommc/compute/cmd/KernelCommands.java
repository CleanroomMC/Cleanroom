package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.ComputeConfig;
import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.errors.BufferError;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.compute.images.Image;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.cleanroommc.compute.smrtptr.GarbageCollector;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CL20;
import org.lwjgl.system.MemoryStack;

import java.lang.ref.Cleaner;
import java.nio.*;
import java.util.Set;

import com.cleanroommc.compute.cmd.CommandQueue.Event;

/** KernelCommands: operations bound to one command queue. */
public final class KernelCommands {
    private final CommandQueue queue;

    KernelCommands(CommandQueue queue) {
        this.queue = queue;
    }
    /**
     * Queues the execution of an OpenCL Kernel over an ND-Range.
     * @param kernel The kernel
     * @param arguments The arguments to the kernel
     * @param workGroupOffsets Which index will each dimension of the work group start at.
     * @param workGroupSizes Dimensionality if the work groups.
     * @param dependencies What does this kernel depend on?
     * @return The chain
     * @author EΣrie
     * @see Kernel#invoke(MemoryStack, CommandQueue, long, KernelParameterList, long[], long[], Event...)
     */
    public Event dispatch(Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long @NonNull [] workGroupSizes,
                                final Event... dependencies) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        Preconditions.checkNotNull(workGroupSizes);
        Preconditions.checkNotNull(arguments);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.dispatch(stack, kernel, arguments, workGroupOffsets, workGroupSizes, dependencies);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * Queues the execution of an OpenCL Kernel over an ND-Range.
     * @param stack MemoryStack
     * @param kernel The kernel
     * @param arguments The arguments to the kernel
     * @param workGroupOffsets Which index will each dimension of the work group start at.
     * @param workGroupSizes Dimensionality if the work groups.
     * @param dependencies What does this kernel depend on?
     * @return The chain
     * @author EΣrie
     * @see Kernel#invoke(MemoryStack, CommandQueue, long, KernelParameterList, long[], long[], Event...)
     */
    public Event dispatch(@NonNull MemoryStack stack, Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long @NonNull [] workGroupSizes,
                                final Event... dependencies) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(workGroupSizes);
        Preconditions.checkNotNull(arguments);
                    return queue.new Event(kernel.invoke(stack, queue, queue.device(), arguments, workGroupOffsets, workGroupSizes, dependencies), stack);
    }

    /**
     * Queues the execution of an OpenCL Kernel as a task.
     * @param kernel The kernel
     * @param arguments The arguments to the kernel
     * @param dependencies What does this kernel depend on?
     * @return The chain
     * @author EΣrie
     * @see Kernel#invoke(MemoryStack, CommandQueue, long, KernelParameterList, Event...)
     */
    public Event dispatch(@NonNull Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final Event... dependencies) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        Preconditions.checkNotNull(arguments);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = queue.new Event(kernel.invoke(stack, queue, queue.device(), arguments, dependencies), stack);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * Queues the execution of an OpenCL Kernel as a task.
     * @param stack MemoryStack
     * @param kernel The kernel
     * @param arguments The arguments to the kernel
     * @param dependencies What does this kernel depend on?
     * @return The chain
     * @author EΣrie
     * @see Kernel#invoke(MemoryStack, CommandQueue, long, KernelParameterList, Event...)
     */
    public Event dispatch(@NonNull MemoryStack stack, @NonNull Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final Event... dependencies) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(arguments);
                    return queue.new Event(kernel.invoke(stack, queue, queue.device(), arguments, dependencies), stack);
    }
}

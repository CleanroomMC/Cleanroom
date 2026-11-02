package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.ComputeConfig;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

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
                                final KernelParameterList arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long [] workGroupSizes,
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
    public Event dispatch(MemoryStack stack, Kernel kernel,
                                final KernelParameterList arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long [] workGroupSizes,
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
    public Event dispatch(Kernel kernel,
                                final KernelParameterList arguments,
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
    public Event dispatch(MemoryStack stack, Kernel kernel,
                                final KernelParameterList arguments,
                                final Event... dependencies) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(arguments);
                    return queue.new Event(kernel.invoke(stack, queue, queue.device(), arguments, dependencies), stack);
    }
}

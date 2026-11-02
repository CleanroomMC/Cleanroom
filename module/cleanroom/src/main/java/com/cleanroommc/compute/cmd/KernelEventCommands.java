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

/** KernelEventCommands: operations bound to one event chain. */
public final class KernelEventCommands {
    private final CommandQueue queue;
    private final Event event;

    KernelEventCommands(CommandQueue queue, Event event) {
        this.queue = queue;
        this.event = event;
    }
    /**
     * Queues a kernel operation after this event and returns the next event in the chain.
     * @param kernel Kernel to enqueue.
     * @param arguments Arguments passed to the kernel.
     * @param workGroupOffsets Global work offsets for each dimension, or null for zero offsets.
     * @param workGroupSizes Global work sizes for each dimension.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @see KernelCommands#dispatch(Kernel, KernelParameterList, long[], long[], Event...)
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public @NonNull Event dispatch(@NonNull Kernel kernel,
                               final @NonNull KernelParameterList arguments,
                               final long @Nullable [] workGroupOffsets,
                               final long @NonNull [] workGroupSizes,
                               final Event... dependencies) throws IllegalStateException {
        Preconditions.checkNotNull(kernel);
        Preconditions.checkNotNull(arguments);
        Preconditions.checkNotNull(workGroupSizes);

        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.kernels.dispatch(
            event.stack,
            kernel,
            arguments,
            workGroupOffsets,
            workGroupSizes,
            dependencyEvents
        );


        return event.transferOwnership(next);

    }

    /**
     * Queues a kernel operation after this event and returns the next event in the chain.
     * @param kernel Kernel to enqueue.
     * @param arguments Arguments passed to the kernel.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @see KernelCommands#dispatch(Kernel, KernelParameterList, long[], long[], Event...)
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public @NonNull Event dispatch(@NonNull Kernel kernel,
                               final @NonNull KernelParameterList arguments,
                               final Event... dependencies) throws IllegalStateException {
        Preconditions.checkNotNull(kernel);
        Preconditions.checkNotNull(arguments);

        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.kernels.dispatch(event.stack,
            kernel,
            arguments,
            dependencyEvents
        );


        return event.transferOwnership(next);

    }
}

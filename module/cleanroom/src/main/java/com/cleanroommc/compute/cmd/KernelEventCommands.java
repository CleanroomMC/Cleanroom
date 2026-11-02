package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.Nullable;

import java.nio.*;

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
    public Event dispatch(Kernel kernel,
                               final KernelParameterList arguments,
                               final long @Nullable [] workGroupOffsets,
                               final long [] workGroupSizes,
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
    public Event dispatch(Kernel kernel,
                               final KernelParameterList arguments,
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

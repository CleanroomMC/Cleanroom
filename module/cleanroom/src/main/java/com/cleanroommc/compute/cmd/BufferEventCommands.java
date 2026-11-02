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

/** BufferEventCommands: operations bound to one event chain. */
public final class BufferEventCommands {
    private final CommandQueue queue;
    private final Event event;

    BufferEventCommands(CommandQueue queue, Event event) {
        this.queue = queue;
        this.event = event;
    }
    /**
     * Writes data from a float array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     */
    public @NonNull Event write(@NonNull Buffer buffer,
                                final float @NonNull [] data,
                                final long offset,
                                final boolean blocking,
                                final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, offset, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param offset Byte offset at which the operation starts.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                       final long offset,
                       final float @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, offset, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final float @NonNull [] data,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, 0, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final float @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                       final double @NonNull [] data,
                       final long offset,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, offset, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param offset Byte offset at which the operation starts.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                       final long offset,
                       final double @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, offset, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final double @NonNull [] data,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, 0, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final double @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#write(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> @NonNull Event write(
        @NonNull Buffer buffer,
        @NonNull B data,
        long offset,
        boolean blocking,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );

        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(
            buffer.write(event.stack, queue, data, blocking, offset, dependencyEvents),
            event.stack,
            buffer,
            blocking,
            event.nonBlockingWrites
        );


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param offset Byte offset at which the operation starts.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#write(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> @NonNull Event write(
        @NonNull Buffer buffer,
        long offset,
        @NonNull B data,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(buffer, data, offset, true, dependencies);
    }

    /**
     * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#write(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> @NonNull Event write(
        @NonNull Buffer buffer,
        @NonNull B data,
        boolean blocking,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(buffer, data, 0, blocking, dependencies);
    }

    /**
     * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#write(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> @NonNull Event write(
        @NonNull Buffer buffer,
        @NonNull B data,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(buffer, data, 0, true, dependencies);
    }

    /**
     * Writes data from a short array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                       final short @NonNull [] data,
                       final long offset,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, offset, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param offset Byte offset at which the operation starts.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                       final long offset,
                       final short @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, offset, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final short @NonNull [] data,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, 0, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final short @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                       final int @NonNull [] data,
                       final long offset,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, offset, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param offset Byte offset at which the operation starts.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                       final long offset,
                       final int @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, offset, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final int @NonNull [] data,
                       final boolean blocking,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(buffer.write(event.stack, queue, data, blocking, 0, dependencyEvents), event.stack, buffer, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to a buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to write to.
     * @param data Data to write to the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or data is null.
     * @throws IllegalArgumentException If data is empty, the write exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support writing.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the write.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                       final int @NonNull [] data,
                       final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.write(event.stack, buffer, data, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a float array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                      float @NonNull [] target,
                      final long offset,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, offset, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a float array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                      final long offset,
                      float @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, offset, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a float array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      float @NonNull [] target,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a float array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      float @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a double array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                      double @NonNull [] target,
                      final long offset,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, offset, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a double array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                      final long offset,
                      double @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, offset, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a double array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      double @NonNull [] target,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a double array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      double @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#read(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> @NonNull Event read(
        @NonNull Buffer buffer,
        @NonNull B target,
        long offset,
        boolean blocking,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );

        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(
            event.stack,
            buffer,
            target,
            offset,
            blocking,
            dependencyEvents
        );


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#read(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> @NonNull Event read(
        @NonNull Buffer buffer,
        long offset,
        @NonNull B target,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(buffer, target, offset, true, dependencies);
    }

    /**
     * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#read(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> @NonNull Event read(
        @NonNull Buffer buffer,
        @NonNull B target,
        boolean blocking,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(buffer, target, 0, blocking, dependencies);
    }

    /**
     * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @see BufferCommands#read(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> @NonNull Event read(
        @NonNull Buffer buffer,
        @NonNull B target,
        final Event... dependencies
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(buffer, target, 0, true, dependencies);
    }

    /**
     * Reads data from a buffer into a short array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                      short @NonNull [] target,
                      final long offset,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, offset, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a short array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                      final long offset,
                      short @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, offset, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a short array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      short @NonNull [] target,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, target, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into a short array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      short @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into an int array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                      int @NonNull [] target,
                      final long offset,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, offset, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into an int array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                      final long offset,
                      int @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, offset, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into an int array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      int @NonNull [] target,
                      final boolean blocking,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(buffer);

        next = queue.buffers.read(event.stack, buffer, target, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads data from a buffer into an int array after this event and returns the next event in the chain.
     * @param buffer The buffer to read from.
     * @param target Destination for the data read from the buffer.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @return The next event in the chain.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If this event can no longer be chained or the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                      int @NonNull [] target,
                      final Event... dependencies) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.buffers.read(event.stack, buffer, target, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }
}

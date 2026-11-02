package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.errors.BufferError;
import com.google.common.base.Preconditions;

import java.nio.*;

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
    public Event write(Buffer buffer,
                                final float [] data,
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
    public Event write(Buffer buffer,
                       final long offset,
                       final float [] data,
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
    public Event write(Buffer buffer,
                       final float [] data,
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
    public Event write(Buffer buffer,
                       final float [] data,
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
    public Event write(Buffer buffer,
                       final double [] data,
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
    public Event write(Buffer buffer,
                       final long offset,
                       final double [] data,
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
    public Event write(Buffer buffer,
                       final double [] data,
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
    public Event write(Buffer buffer,
                       final double [] data,
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
    public <B extends java.nio.Buffer> Event write(
        Buffer buffer,
        B data,
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
    public <B extends java.nio.Buffer> Event write(
        Buffer buffer,
        long offset,
        B data,
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
    public <B extends java.nio.Buffer> Event write(
        Buffer buffer,
        B data,
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
    public <B extends java.nio.Buffer> Event write(
        Buffer buffer,
        B data,
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
    public Event write(Buffer buffer,
                       final short [] data,
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
    public Event write(Buffer buffer,
                       final long offset,
                       final short [] data,
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
    public Event write(Buffer buffer,
                       final short [] data,
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
    public Event write(Buffer buffer,
                       final short [] data,
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
    public Event write(Buffer buffer,
                       final int [] data,
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
    public Event write(Buffer buffer,
                       final long offset,
                       final int [] data,
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
    public Event write(Buffer buffer,
                       final int [] data,
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
    public Event write(Buffer buffer,
                       final int [] data,
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
    public Event read(Buffer buffer,
                      float [] target,
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
    public Event read(Buffer buffer,
                      final long offset,
                      float [] target,
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
    public Event read(Buffer buffer,
                      float [] target,
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
    public Event read(Buffer buffer,
                      float [] target,
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
    public Event read(Buffer buffer,
                      double [] target,
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
    public Event read(Buffer buffer,
                      final long offset,
                      double [] target,
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
    public Event read(Buffer buffer,
                      double [] target,
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
    public Event read(Buffer buffer,
                      double [] target,
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
    public <B extends java.nio.Buffer> Event read(
        Buffer buffer,
        B target,
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
    public <B extends java.nio.Buffer> Event read(
        Buffer buffer,
        long offset,
        B target,
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
    public <B extends java.nio.Buffer> Event read(
        Buffer buffer,
        B target,
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
    public <B extends java.nio.Buffer> Event read(
        Buffer buffer,
        B target,
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
    public Event read(Buffer buffer,
                      short [] target,
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
    public Event read(Buffer buffer,
                      final long offset,
                      short [] target,
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
    public Event read(Buffer buffer,
                      short [] target,
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
    public Event read(Buffer buffer,
                      short [] target,
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
    public Event read(Buffer buffer,
                      int [] target,
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
    public Event read(Buffer buffer,
                      final long offset,
                      int [] target,
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
    public Event read(Buffer buffer,
                      int [] target,
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
    public Event read(Buffer buffer,
                      int [] target,
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

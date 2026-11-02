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

/** BufferCommands: operations bound to one command queue. */
public final class BufferCommands {
    private final CommandQueue queue;

    BufferCommands(CommandQueue queue) {
        this.queue = queue;
    }
    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final float @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return queue.createWriteEvent(buffer.write(stack, queue, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull Buffer buffer,
                             final long offset,
                             final float @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, offset, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final double @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull Buffer buffer,
                             final long offset,
                             final double @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, offset, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a NIOBuffer.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @param <B> Type of buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        @NonNull B data,
        long offset,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);

        return queue.createWriteEvent(
            buffer.write(stack, queue, data, blocking, offset, events),
            stack,
            buffer,
            blocking,
            null
        );
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @param <B> Type of buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        long offset,
        @NonNull B data,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(stack, buffer, data, offset, true, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @param <B> Type of buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        @NonNull B data,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(stack, buffer, data, 0, blocking, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @param <B> Type of buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        @NonNull B data,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(stack, buffer, data, 0, true, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @param <B> Type of buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull Buffer buffer,
        @NonNull B data,
        long offset,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);

        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();

        try {
            Event event = write(
                stack,
                buffer,
                data,
                offset,
                blocking,
                events
            );

            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull Buffer buffer,
        long offset,
        @NonNull B data,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(buffer, data, offset, true, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @param <B> Type of Buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull Buffer buffer,
        @NonNull B data,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(buffer, data, 0, blocking, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @param <B> Type of Buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> Event write(
        @NonNull Buffer buffer,
        @NonNull B data,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return write(buffer, data, 0, true, events);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final short @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull Buffer buffer,
                             final long offset,
                             final short @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, offset, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final int @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(data);
            Preconditions.checkNotNull(buffer);
            return queue.createWriteEvent(buffer.write(stack, queue, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event write(@NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This is always a blocking write.
     */
    public Event write(@NonNull Buffer buffer,
                             final long offset,
                             final int @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, offset, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This always writes at offset 0.
     */
    public Event write(@NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final boolean blocking, final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Write data to the buffer from ain int array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event write(@NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.write(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            float @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                            final long offset,
                            float @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, offset, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            float @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, float[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            float @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            double @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                            final long offset,
                            double @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, offset, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            double @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, double[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            double @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        @NonNull B target,
        long offset,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);

        return queue.new Event(
            buffer.read(stack, queue, target, blocking, offset, events),
            stack
        );
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        long offset,
        @NonNull B target,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(stack, buffer, target, offset, true, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        @NonNull B target,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(stack, buffer, target, 0, blocking, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull MemoryStack stack,
        @NonNull Buffer buffer,
        @NonNull B target,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(stack, buffer, target, 0, true, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull Buffer buffer,
        @NonNull B target,
        long offset,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);

        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();

        try {
            Event event = read(
                stack,
                buffer,
                target,
                offset,
                blocking,
                events
            );

            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull Buffer buffer,
        long offset,
        @NonNull B target,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(buffer, target, offset, true, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull Buffer buffer,
        @NonNull B target,
        boolean blocking,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(buffer, target, 0, blocking, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event read(
        @NonNull Buffer buffer,
        @NonNull B target,
        Event... events
    ) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        return read(buffer, target, 0, true, events);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            short @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                            final long offset,
                            short @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, offset, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            short @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, short[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            short @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            int @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
                    Preconditions.checkNotNull(stack);
            Preconditions.checkNotNull(target);
            Preconditions.checkNotNull(buffer);
            return queue.new Event(buffer.read(stack, queue, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event read(@NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, offset, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event read(@NonNull Buffer buffer,
                            final long offset,
                            int @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, offset, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            int @NonNull [] target,
                            final boolean blocking,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, blocking, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, int[], boolean, long, Event...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event read(@NonNull Buffer buffer,
                            int @NonNull [] target,
                            final Event... events) throws NullPointerException, IllegalArgumentException, IllegalStateException, BufferError, OutOfMemoryError {
        MemoryStack stack = MemoryStack.create(ComputeConfig.stackSize).push();
        try {
            Event event = this.read(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }
}

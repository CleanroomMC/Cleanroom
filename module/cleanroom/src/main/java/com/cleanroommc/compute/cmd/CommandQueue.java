package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.errors.BufferError;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.compute.images.Image;
import com.cleanroommc.compute.kernels.Kernel;
import com.cleanroommc.compute.kernels.params.KernelParameterList;
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

import java.nio.*;
import java.util.Set;

/**
 * An OpenCL CommandQueue
 * @apiNote This is how you should use almost everything in OpenCL.
 * @author EΣrie
 */
public class CommandQueue extends SmartPointer {

    public final long commandQueue;
    private final long device;

    /**
     * Creates a new CommandQueue for a device.
     * @param device Commands passed via this queue will be executed on this device.
     * @author EΣrie
     * @throws RuntimeException If the OpenCL context is invalid or,
     * the properties of the command queue are invalid or unsupported by the device.
     * @throws UnavaliableDeviceError If the device is not available for the context.
     * @throws OutOfMemoryError If there are not enough resources available to create the command queue.
     * @see CommandQueueDispatch#dispatch(String) 
     * @see CommandQueueDispatch#dispatch(String, boolean, boolean, boolean) 
     */
    CommandQueue(long device) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        super();
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

    //<editor-fold desc="Kernel Dispatch">

    /**
     * Queues the execution of an OpenCL Kernel over an ND-Range.
     * @param kernel The kernel
     * @param arguments The arguments to the kernel
     * @param workGroupOffsets Which index will each dimension of the work group start at.
     * @param workGroupSizes Dimensionality if the work groups.
     * @param dependencies What does this kernel depend on?
     * @return The chain
     * @author EΣrie
     * @see Kernel#invoke(MemoryStack, long, long, KernelParameterList, long[], long[], long...) 
     */
    public Event dispatchKernel(Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final long @Nullable [] workGroupOffsets,
                                final long @NonNull [] workGroupSizes,
                                final long... dependencies) {
        Preconditions.checkNotNull(workGroupSizes);
        Preconditions.checkNotNull(arguments);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.dispatchKernel(stack, kernel, arguments, workGroupOffsets, workGroupSizes, dependencies);
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
     * @see Kernel#invoke(MemoryStack, long, long, KernelParameterList, long[], long[], long...)
     */
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

    /**
     * Queues the execution of an OpenCL Kernel as a task.
     * @param kernel The kernel
     * @param arguments The arguments to the kernel
     * @param dependencies What does this kernel depend on?
     * @return The chain
     * @author EΣrie
     * @see Kernel#invoke(MemoryStack, long, long, KernelParameterList, long...)
     */
    public Event dispatchKernel(@NonNull Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final long... dependencies) {
        Preconditions.checkNotNull(arguments);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = new Event(kernel.invoke(stack, this.commandQueue, this.device, arguments, dependencies), stack);
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
     * @see Kernel#invoke(MemoryStack, long, long, KernelParameterList, long...)
     */
    public Event dispatchKernel(@NonNull MemoryStack stack, @NonNull Kernel kernel,
                                final @NonNull KernelParameterList arguments,
                                final long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(arguments);
        return new Event(kernel.invoke(stack, this.commandQueue, this.device, arguments, dependencies), stack);
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Write">

    //<editor-fold desc="Buffer Write Float">

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, float[], long, boolean, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, float[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final float @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, float[], boolean, Event...)
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
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, float[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, float[], long, boolean, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, offset, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, float[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final float @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, offset, data, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, float[], boolean, Event...)
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
    public Event bufferWrite(@NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, float[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, float[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Write Double">

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, double[], long, boolean, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, double[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final double @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, double[], boolean, Event...)
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
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, double[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, double[], long, boolean, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, offset, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, double[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final double @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, offset, data, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, double[], boolean, Event...)
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
    public Event bufferWrite(@NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, double[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, double[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Write NIO Buffer">

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
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            @NonNull B data,
            long offset,
            boolean blocking,
            long... events
    ) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);

        return createWriteEvent(
                buffer.write(stack, this, data, blocking, offset, events),
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
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, java.nio.Buffer, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            long offset,
            @NonNull B data,
            long... events
    ) {
        return bufferWrite(stack, buffer, data, offset, true, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, java.nio.Buffer, boolean, Event...)
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
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            @NonNull B data,
            boolean blocking,
            long... events
    ) {
        return bufferWrite(stack, buffer, data, 0, blocking, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @param <B> Type of buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, java.nio.Buffer, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            @NonNull B data,
            long... events
    ) {
        return bufferWrite(stack, buffer, data, 0, true, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, java.nio.Buffer, long, boolean, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull Buffer buffer,
            @NonNull B data,
            long offset,
            boolean blocking,
            long... events
    ) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);

        MemoryStack stack = MemoryStack.create().push();

        try {
            Event event = bufferWrite(
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
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, java.nio.Buffer, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull Buffer buffer,
            long offset,
            @NonNull B data,
            long... events
    ) {
        return bufferWrite(buffer, data, offset, true, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @param <B> Type of Buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, java.nio.Buffer, boolean, Event...)
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
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull Buffer buffer,
            @NonNull B data,
            boolean blocking,
            long... events
    ) {
        return bufferWrite(buffer, data, 0, blocking, events);
    }

    /**
     * <p>Write data to the buffer from a NIO Array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @param <B> Type of Buffer
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, java.nio.Buffer, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event bufferWrite(
            @NonNull Buffer buffer,
            @NonNull B data,
            long... events
    ) {
        return bufferWrite(buffer, data, 0, true, events);
    }
    //</editor-fold>

    //<editor-fold desc="Buffer Write Short">

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, short[], long, boolean, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, short[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final short @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, short[], boolean, Event...)
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
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, short[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, short[], long, boolean, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, offset, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, short[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final short @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, offset, data, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, short[], boolean, Event...)
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
    public Event bufferWrite(@NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, short[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, short[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Write Int">

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, int[], long, boolean, Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, offset, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, int[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final int @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, offset, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, int[], boolean, Event...)
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
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, blocking, 0, events), stack, buffer, blocking, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, int[], Event...)
     * @throws NullPointerException If stack or data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return createWriteEvent(buffer.write(stack, this, data, true, 0, events), stack, buffer, true, null);
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param buffer The buffer to write to
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, int[], long, boolean, Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, offset, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, long, int[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     * @apiNote This is always a blocking write.
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final int @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, offset, data, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, int[], boolean, Event...)
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
    public Event bufferWrite(@NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, blocking, events);
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
     * @see Buffer#write(MemoryStack, CommandQueue, int[], boolean, long, long...)
     * @see CommandQueue.Event#write(Buffer, int[], Event...)
     * @throws NullPointerException If data is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public Event bufferWrite(@NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferWrite(stack, buffer, data, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Buffer Read">

    //<editor-fold desc="Buffer Read Float">

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            float @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a float array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, offset, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            float @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, offset, target, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            float @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Read Double">

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            double @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a double array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, offset, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            double @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, offset, target, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            double @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Read NIO Buffer">

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
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            @NonNull B target,
            long offset,
            boolean blocking,
            long... events
    ) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);

        return new Event(
                buffer.read(stack, this, target, blocking, offset, events),
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
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            long offset,
            @NonNull B target,
            long... events
    ) {
        return bufferRead(stack, buffer, target, offset, true, events);
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
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            @NonNull B target,
            boolean blocking,
            long... events
    ) {
        return bufferRead(stack, buffer, target, 0, blocking, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull MemoryStack stack,
            @NonNull Buffer buffer,
            @NonNull B target,
            long... events
    ) {
        return bufferRead(stack, buffer, target, 0, true, events);
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
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull Buffer buffer,
            @NonNull B target,
            long offset,
            boolean blocking,
            long... events
    ) {
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);

        MemoryStack stack = MemoryStack.create().push();

        try {
            Event event = bufferRead(
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
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull Buffer buffer,
            long offset,
            @NonNull B target,
            long... events
    ) {
        return bufferRead(buffer, target, offset, true, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull Buffer buffer,
            @NonNull B target,
            boolean blocking,
            long... events
    ) {
        return bufferRead(buffer, target, 0, blocking, events);
    }

    /**
     * <p>Read data from the buffer into a NIO buffer.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Buffer#read(MemoryStack, CommandQueue, java.nio.Buffer, boolean, long, long...)
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public <B extends java.nio.Buffer> Event bufferRead(
            @NonNull Buffer buffer,
            @NonNull B target,
            long... events
    ) {
        return bufferRead(buffer, target, 0, true, events);
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Read Short">

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            short @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a short array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, offset, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            short @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, offset, target, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            short @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //<editor-fold desc="Buffer Read Int">

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param offset Byte offset at which the operation starts.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            int @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, offset, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, blocking, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If stack, buffer, or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this, target, true, 0, events), stack);
    }

    /**
     * <p>Read data from the buffer into a int array.</p>
     * @param buffer The buffer involved in the operation.
     * @param target Destination for the data read from the buffer.
     * @param offset Byte offset at which the operation starts.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param events OpenCL event IDs this operation depends on.
     * @return Event of the operation.
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, offset, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            int @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, offset, target, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            int @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, blocking, events);
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
     * @throws NullPointerException If buffer or target is null.
     * @throws IllegalArgumentException If the target is empty, the read exceeds the buffer, the command queue is closed, or an event ID is negative.
     * @throws IllegalStateException If the buffer does not support reading.
     * @throws BufferError If the buffer or one of the events is invalid, or the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError If there are not enough resources available to perform the read.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates at offset 0.
     */
    public Event bufferRead(@NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long... events) {
        MemoryStack stack = MemoryStack.create().push();
        try {
            Event event = this.bufferRead(stack, buffer, target, events);
            event.ownsStack = true;
            return event;
        } catch (RuntimeException | Error exception) {
            stack.close();
            throw exception;
        }
    }

    //</editor-fold>

    //</editor-fold>

    //<editor-fold desc="Image Fill">

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Color used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event imageFill(@NonNull MemoryStack stack, @NonNull Image<CT> image,
                                                           @NonNull B color, @NonNull CT from, @NonNull CT size, int mipmap,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.fill(stack, this, color, from, size, mipmap, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event imageFill(@NonNull MemoryStack stack, @NonNull Image<CT> image,
                                                           @NonNull B color, @NonNull CT from, @NonNull CT size,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.fill(stack, this, color, from, size, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageFill(@NonNull MemoryStack stack, @NonNull Image<CT> image,
                                                           int @NonNull [] color, @NonNull CT from, @NonNull CT size, int mipmap,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.fill(stack, this, color, from, size, mipmap, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageFill(@NonNull MemoryStack stack, @NonNull Image<CT> image,
                                                           int @NonNull [] color, @NonNull CT from, @NonNull CT size,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.fill(stack, this, color, from, size, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Color used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageFill(@NonNull MemoryStack stack, @NonNull Image<CT> image,
                                float @NonNull [] color, @NonNull CT from, @NonNull CT size, int mipmap,
                                long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.fill(stack, this, color, from, size, mipmap, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageFill(@NonNull MemoryStack stack, @NonNull Image<CT> image,
                                float @NonNull [] color, @NonNull CT from, @NonNull CT size,
                                long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.fill(stack, this, color, from, size, dependencies), stack);
    }

    //</editor-fold>

    //<editor-fold desc="Image Copy">

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param mipmapFrom Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param mipmapTo Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT1, CT2> Event imageCopy(@NonNull MemoryStack stack,
                                      @NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                      @NonNull CT1 from, int mipmapFrom,
                                      @NonNull CT2 to, int mipmapTo, @NonNull CT2 size,
                                      long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return new Event(start.copy(stack, this, destination, from, mipmapFrom, to, mipmapTo, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param mipmapTo Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote The source uses mipmap level 0.
     */
    public <CT1, CT2> Event imageCopy(@NonNull MemoryStack stack,
                                      @NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                      @NonNull CT1 from,
                                      @NonNull CT2 to, int mipmapTo, @NonNull CT2 size,
                                      long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return new Event(start.copy(stack, this, destination, from, to, mipmapTo, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param mipmapFrom Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote The destination uses mipmap level 0.
     */
    public <CT1, CT2> Event imageCopy(@NonNull MemoryStack stack,
                                      @NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                      @NonNull CT1 from, int mipmapFrom,
                                      @NonNull CT2 to, @NonNull CT2 size,
                                      long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return new Event(start.copy(stack, this, destination, from, mipmapFrom, to, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote Both source and destination use mipmap level 0.
     */
    public <CT1, CT2> Event imageCopy(@NonNull MemoryStack stack,
                                      @NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                      @NonNull CT1 from,
                                      @NonNull CT2 to, @NonNull CT2 size,
                                      long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return new Event(start.copy(stack, this, destination, from, to, size, dependencies), stack);
    }

    //</editor-fold>

    //<editor-fold desc="Image Read">

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, @NonNull B buffer,
                                                           boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT, B extends java.nio.Buffer> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, @NonNull B buffer,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, @NonNull CT size,
                                                           long rowPitch, long slicePitch, @NonNull B buffer,
                                                           boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, @NonNull CT size,
                                                           long rowPitch, long slicePitch, @NonNull B buffer,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, buffer, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, short @NonNull [] array,
                                                           boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, short @NonNull [] array,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, @NonNull CT size,
                                                           long rowPitch, long slicePitch, short @NonNull [] array,
                                                           boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, @NonNull CT size,
                                                           long rowPitch, long slicePitch, short @NonNull [] array,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, int @NonNull [] array,
                                                           boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, int @NonNull [] array,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, @NonNull CT size,
                                                           long rowPitch, long slicePitch, int @NonNull [] array,
                                                           boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                                           @NonNull CT from, @NonNull CT size,
                                                           long rowPitch, long slicePitch, int @NonNull [] array,
                                                           long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageRead(@NonNull MemoryStack stack, Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return new Event(image.read(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }


    //</editor-fold>

    //<editor-fold desc="Image Write">

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, int mipmap, @NonNull CT size,
                                                            long rowPitch, long slicePitch, @NonNull B buffer,
                                                            boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT, B extends java.nio.Buffer> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, int mipmap, @NonNull CT size,
                                                            long rowPitch, long slicePitch, @NonNull B buffer, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, @NonNull CT size,
                                                            long rowPitch, long slicePitch, @NonNull B buffer,
                                                            boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, @NonNull CT size,
                                                            long rowPitch, long slicePitch, @NonNull B buffer, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, buffer, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, int mipmap, @NonNull CT size,
                                 long rowPitch, long slicePitch, short @NonNull [] array,
                                 boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, int mipmap, @NonNull CT size,
                                 long rowPitch, long slicePitch, short @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, @NonNull CT size,
                                 long rowPitch, long slicePitch, short @NonNull [] array,
                                 boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, @NonNull CT size,
                                 long rowPitch, long slicePitch, short @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }


    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, int mipmap, @NonNull CT size,
                                                            long rowPitch, long slicePitch, int @NonNull [] array,
                                                            boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, int mipmap, @NonNull CT size,
                                                            long rowPitch, long slicePitch, int @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, @NonNull CT size,
                                                            long rowPitch, long slicePitch, int @NonNull [] array,
                                                            boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, @NonNull CT size,
                                 long rowPitch, long slicePitch, int @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, int mipmap, @NonNull CT size,
                                 long rowPitch, long slicePitch, float @NonNull [] array,
                                 boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, int mipmap, @NonNull CT size,
                                 long rowPitch, long slicePitch, float @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, @NonNull CT size,
                                 long rowPitch, long slicePitch, float @NonNull [] array,
                                 boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                 @NonNull CT from, @NonNull CT size,
                                 long rowPitch, long slicePitch, float @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }


    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, int mipmap, @NonNull CT size,
                                                            long rowPitch, long slicePitch, double @NonNull [] array,
                                                            boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, int mipmap, @NonNull CT size,
                                                            long rowPitch, long slicePitch, double @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, @NonNull CT size,
                                                            long rowPitch, long slicePitch, double @NonNull [] array,
                                                            boolean blocking, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event imageWrite(@NonNull MemoryStack stack, Image<CT> image,
                                                            @NonNull CT from, @NonNull CT size,
                                                            long rowPitch, long slicePitch, double @NonNull [] array, long... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return createWriteEvent(image.write(stack, this, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    //</editor-fold>

    @Override
    public void close() {
        super.close();
        CL20.clReleaseCommandQueue(commandQueue);
    }

    private Event createWriteEvent(long eventID,
                                   @NonNull MemoryStack stack,
                                   @NonNull SmartPointer object,
                                   boolean blocking,
                                   @Nullable Set<SmartPointer> nonBlockingWrites) {
        Event event = new Event(eventID, stack, nonBlockingWrites);
        event.updateNonBlockingWrites(object, blocking);
        return event;
    }

    public final class Event {
        public final long eventID;
        private final MemoryStack stack;

        /** True only when this chain created the stack itself. */
        private boolean ownsStack;

        /**
         * A chained Event transfers its stack ownership to the next Event.
         * The old Event may still be used as an OpenCL dependency (eventID),
         * but it may no longer append commands to the chain.
         */
        private boolean chainable = true;

        private @Nullable Set<SmartPointer> nonBlockingWrites;

        Event(long eventID, @NonNull MemoryStack stack) {
            this(eventID, stack, null);
        }

        Event(long eventID, @NonNull MemoryStack stack, @Nullable Set<SmartPointer> nonBlockingWrites) {
            this.eventID = eventID;
            this.stack = Preconditions.checkNotNull(stack);
            this.nonBlockingWrites = nonBlockingWrites;
        }

        /**
         * Queues a kernel operation after this event and returns the next event in the chain.
         * @param kernel Kernel to enqueue.
         * @param arguments Arguments passed to the kernel.
         * @param workGroupOffsets Global work offsets for each dimension, or null for zero offsets.
         * @param workGroupSizes Global work sizes for each dimension.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @see CommandQueue#dispatchKernel(Kernel, KernelParameterList, long[], long[], long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public @NonNull Event next(@NonNull Kernel kernel,
                                   final @NonNull KernelParameterList arguments,
                                   final long @Nullable [] workGroupOffsets,
                                   final long @NonNull [] workGroupSizes,
                                   final Event... dependencies) {
            Preconditions.checkNotNull(kernel);
            Preconditions.checkNotNull(arguments);
            Preconditions.checkNotNull(workGroupSizes);

            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = dispatchKernel(
                        stack,
                        kernel,
                        arguments,
                        workGroupOffsets,
                        workGroupSizes,
                        dependencyIDs
                );
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Queues a kernel operation after this event and returns the next event in the chain.
         * @param kernel Kernel to enqueue.
         * @param arguments Arguments passed to the kernel.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @see CommandQueue#dispatchKernel(Kernel, KernelParameterList, long[], long[], long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public @NonNull Event next(@NonNull Kernel kernel,
                                   final @NonNull KernelParameterList arguments,
                                   final Event... dependencies) {
            Preconditions.checkNotNull(kernel);
            Preconditions.checkNotNull(arguments);

            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = dispatchKernel(stack,
                        kernel,
                        arguments,
                        dependencyIDs
                );
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //<editor-fold desc="Buffer Write">

        //<editor-fold desc="Buffer Write Float">

        /**
         * Writes data from a float array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public @NonNull Event write(@NonNull Buffer buffer,
                                    final float @NonNull [] data,
                                    final long offset,
                                    final boolean blocking,
                                    final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, offset, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a float array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param offset Byte offset at which the operation starts.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final float @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Writes data from a float array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final float @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, 0, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a float array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final float @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write Double">

        /**
         * Writes data from a double array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event write(@NonNull Buffer buffer,
                           final double @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, offset, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a double array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param offset Byte offset at which the operation starts.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final double @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Writes data from a double array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final double @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, 0, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a double array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final double @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write NIO Buffer">

        /**
         * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public <B extends java.nio.Buffer> @NonNull Event write(
                @NonNull Buffer buffer,
                @NonNull B data,
                long offset,
                boolean blocking,
                final Event... dependencies
        ) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );

            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(
                        buffer.write(stack, CommandQueue.this, data, blocking, offset, dependencyIDs),
                        stack,
                        buffer,
                        blocking,
                        nonBlockingWrites
                );
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param offset Byte offset at which the operation starts.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public <B extends java.nio.Buffer> @NonNull Event write(
                @NonNull Buffer buffer,
                long offset,
                @NonNull B data,
                final Event... dependencies
        ) {
            return write(buffer, data, offset, true, dependencies);
        }

        /**
         * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public <B extends java.nio.Buffer> @NonNull Event write(
                @NonNull Buffer buffer,
                @NonNull B data,
                boolean blocking,
                final Event... dependencies
        ) {
            return write(buffer, data, 0, blocking, dependencies);
        }

        /**
         * Writes data from a NIO buffer to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public <B extends java.nio.Buffer> @NonNull Event write(
                @NonNull Buffer buffer,
                @NonNull B data,
                final Event... dependencies
        ) {
            return write(buffer, data, 0, true, dependencies);
        }
        //</editor-fold>
        //<editor-fold desc="Buffer Write Short">

        /**
         * Writes data from a short array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event write(@NonNull Buffer buffer,
                           final short @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, offset, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a short array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param offset Byte offset at which the operation starts.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final short @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Writes data from a short array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final short @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, 0, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from a short array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final short @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write Int">

        /**
         * Writes data from an int array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event write(@NonNull Buffer buffer,
                           final int @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, offset, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from an int array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param offset Byte offset at which the operation starts.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final int @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Writes data from an int array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final int @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(buffer.write(stack, CommandQueue.this, data, blocking, 0, dependencyIDs), stack, buffer, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Writes data from an int array to a buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to write to.
         * @param data Data to write to the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event write(@NonNull Buffer buffer,
                           final int @NonNull [] data,
                           final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>

        //</editor-fold>

        //<editor-fold desc="Buffer Read">

        //<editor-fold desc="Buffer Read Float">

        /**
         * Reads data from a buffer into a float array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event read(@NonNull Buffer buffer,
                          float @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a float array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param offset Byte offset at which the operation starts.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          float @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a float array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          float @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a float array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          float @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read Double">

        /**
         * Reads data from a buffer into a double array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event read(@NonNull Buffer buffer,
                          double @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a double array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param offset Byte offset at which the operation starts.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          double @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a double array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          double @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a double array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          double @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read NIO Buffer">

        /**
         * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public <B extends java.nio.Buffer> @NonNull Event read(
                @NonNull Buffer buffer,
                @NonNull B target,
                long offset,
                boolean blocking,
                final Event... dependencies
        ) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );

            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(
                        stack,
                        buffer,
                        target,
                        offset,
                        blocking,
                        dependencyIDs
                );
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param offset Byte offset at which the operation starts.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public <B extends java.nio.Buffer> @NonNull Event read(
                @NonNull Buffer buffer,
                long offset,
                @NonNull B target,
                final Event... dependencies
        ) {
            return read(buffer, target, offset, true, dependencies);
        }

        /**
         * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public <B extends java.nio.Buffer> @NonNull Event read(
                @NonNull Buffer buffer,
                @NonNull B target,
                boolean blocking,
                final Event... dependencies
        ) {
            return read(buffer, target, 0, blocking, dependencies);
        }

        /**
         * Reads data from a buffer into a NIO buffer after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, long, boolean, long...)
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public <B extends java.nio.Buffer> @NonNull Event read(
                @NonNull Buffer buffer,
                @NonNull B target,
                final Event... dependencies
        ) {
            return read(buffer, target, 0, true, dependencies);
        }
        //</editor-fold>
        //<editor-fold desc="Buffer Read Short">

        /**
         * Reads data from a buffer into a short array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event read(@NonNull Buffer buffer,
                          short @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a short array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param offset Byte offset at which the operation starts.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          short @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a short array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          short @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into a short array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          short @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read Int">

        /**
         * Reads data from a buffer into an int array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param offset Byte offset at which the operation starts.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event read(@NonNull Buffer buffer,
                          int @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into an int array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param offset Byte offset at which the operation starts.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         */
        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          int @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into an int array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param blocking Whether the operation blocks until the transfer is complete.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          int @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(buffer);

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        /**
         * Reads data from a buffer into an int array after this event and returns the next event in the chain.
         * @param buffer The buffer to read from.
         * @param target Destination for the data read from the buffer.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This is always a blocking operation.
         * @apiNote This operates at offset 0.
         */
        public Event read(@NonNull Buffer buffer,
                          int @NonNull [] target,
                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>

        //</editor-fold>

        //<editor-fold desc="Image Fill">

        /**
         * Fills an image region after this event and returns the next event in the chain.
         * @param image The image to fill.
         * @param color Colour used to fill the image region.
         * @param from Origin of the image region.
         * @param size Size of the image region.
         * @param mipmap Mipmap level of the image.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <CT> Image coordinate type.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public <CT, B extends java.nio.Buffer> @NonNull Event fill(@NonNull Image<CT> image, @NonNull B color,
                                                                   @NonNull CT from, @NonNull CT size, int mipmap,
                                                                   final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageFill(stack, image, color, from, size, mipmap, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Fills an image region after this event and returns the next event in the chain.
         * @param image The image to fill.
         * @param color Colour used to fill the image region.
         * @param from Origin of the image region.
         * @param size Size of the image region.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <CT> Image coordinate type.
         * @param <B> Type of NIO buffer.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates on mipmap level 0.
         */
        public <CT, B extends java.nio.Buffer> @NonNull Event fill(@NonNull Image<CT> image, @NonNull B color,
                                                                   @NonNull CT from, @NonNull CT size,
                                                                   final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageFill(stack, image, color, from, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Fills an image region after this event and returns the next event in the chain.
         * @param image The image to fill.
         * @param color Colour used to fill the image region.
         * @param from Origin of the image region.
         * @param size Size of the image region.
         * @param mipmap Mipmap level of the image.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <CT> Image coordinate type.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public <CT> @NonNull Event fill(@NonNull Image<CT> image, int @NonNull [] color,
                                        @NonNull CT from, @NonNull CT size, int mipmap,
                                        final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageFill(stack, image, color, from, size, mipmap, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Fills an image region after this event and returns the next event in the chain.
         * @param image The image to fill.
         * @param color Colour used to fill the image region.
         * @param from Origin of the image region.
         * @param size Size of the image region.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <CT> Image coordinate type.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates on mipmap level 0.
         */
        public <CT> @NonNull Event fill(@NonNull Image<CT> image, int @NonNull [] color,
                                        @NonNull CT from, @NonNull CT size,
                                        final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageFill(stack, image, color, from, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Fills an image region after this event and returns the next event in the chain.
         * @param image The image to fill.
         * @param color Colour used to fill the image region.
         * @param from Origin of the image region.
         * @param size Size of the image region.
         * @param mipmap Mipmap level of the image.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <CT> Image coordinate type.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public <CT> @NonNull Event fill(@NonNull Image<CT> image, float @NonNull [] color,
                                        @NonNull CT from, @NonNull CT size, int mipmap,
                                        final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageFill(stack, image, color, from, size, mipmap, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        /**
         * Fills an image region after this event and returns the next event in the chain.
         * @param image The image to fill.
         * @param color Colour used to fill the image region.
         * @param from Origin of the image region.
         * @param size Size of the image region.
         * @param dependencies Additional events this operation depends on. They are consumed by this chain step.
         * @param <CT> Image coordinate type.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         * @apiNote This operates on mipmap level 0.
         */
        public <CT> @NonNull Event fill(@NonNull Image<CT> image, float @NonNull [] color,
                                        @NonNull CT from, @NonNull CT size,
                                        final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageFill(stack, image, color, from, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }


        //</editor-fold>

        //<editor-fold desc="Image Copy">

        public <CT1, CT2> @NonNull Event copy(@NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                              @NonNull CT1 from, int mipmapFrom,
                                              @NonNull CT2 to, int mipmapTo, @NonNull CT2 size,
                                              final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            checkNonBlockingWrite(start);

            try {
                next = imageCopy(stack, start, destination, from, mipmapFrom, to, mipmapTo, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT1, CT2> @NonNull Event copy(@NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                              @NonNull CT1 from,
                                              @NonNull CT2 to, int mipmapTo, @NonNull CT2 size,
                                              final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            checkNonBlockingWrite(start);

            try {
                next = imageCopy(stack, start, destination, from, to, mipmapTo, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT1, CT2> @NonNull Event copy(@NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                              @NonNull CT1 from, int mipmapFrom,
                                              @NonNull CT2 to, @NonNull CT2 size,
                                              final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            checkNonBlockingWrite(start);

            try {
                next = imageCopy(stack, start, destination, from, mipmapFrom, to, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT1, CT2> @NonNull Event copy(@NonNull Image<CT1> start, @NonNull Image<CT2> destination,
                                              @NonNull CT1 from,
                                              @NonNull CT2 to, @NonNull CT2 size,
                                              final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            checkNonBlockingWrite(start);

            try {
                next = imageCopy(stack, start, destination, from, to, size, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }


        //</editor-fold>

        //<editor-fold desc="Image Read">

        public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                          @NonNull CT from, int mipmap, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                          @NonNull CT from, int mipmap, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                          @NonNull CT from, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, buffer, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                          @NonNull CT from, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, buffer, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, short @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, short @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, short @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, short @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, int @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, int @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, int @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, int @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, float @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, float @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, float @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, float @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, double @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, double @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, double @NonNull [] array,
                               boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            if (!blocking) checkNonBlockingWrite(image);

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            if (blocking) clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event read(Image<CT> image,
                               @NonNull CT from, @NonNull CT size,
                               long rowPitch, long slicePitch, double @NonNull [] array,
                               final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageRead(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        //</editor-fold>

        //<editor-fold desc="Image Write">

        public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                          @NonNull CT from, int mipmap, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                          @NonNull CT from, int mipmap, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                          @NonNull CT from, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, size, rowPitch, slicePitch, buffer, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                          @NonNull CT from, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, size, rowPitch, slicePitch, buffer, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, short @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, short @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, short @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, short @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, int @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, int @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, int @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, int @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, float @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, int mipmap, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                boolean blocking, final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = createWriteEvent(image.write(stack, CommandQueue.this, from, size, rowPitch, slicePitch, array, blocking, dependencyIDs), stack, image, blocking, nonBlockingWrites);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public <CT> Event write(Image<CT> image,
                                @NonNull CT from, @NonNull CT size,
                                long rowPitch, long slicePitch, double @NonNull [] array,
                                final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = imageWrite(stack, image, from, size, rowPitch, slicePitch, array, true, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            clearNonBlockingWrites();
            return transferOwnership(next);
        }


        //</editor-fold>

        public Event barrier(final Event... dependencies) {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;
            PointerBuffer dependenciesBuffer = PointerBuffer.allocateDirect(dependencyIDs.length + 2);
            dependenciesBuffer.put(0, 0);
            dependenciesBuffer.put(1, eventID);
            for (long dependencyID : dependencyIDs) {
                dependenciesBuffer.put(dependencyID);
            }
            dependenciesBuffer.rewind();

            CL12.clEnqueueBarrierWithWaitList(commandQueue,
                    dependenciesBuffer.slice(1, dependencyIDs.length + 1),
                    dependenciesBuffer.slice(0, 1)
            );

            releaseDependencies(dependencies);

            clearNonBlockingWrites();
            return transferOwnership(new Event(dependenciesBuffer.get(0), this.stack));
        }

        /**
         * Flushes the queue and ends this Event chain. If the stack was created
         * internally by CommandQueue, its active frame is closed here.
         * A caller-supplied MemoryStack is never closed by Event.
         */
        public void execute() {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );

            try {
                CL10.clFlush(commandQueue);
                CL10.clReleaseEvent(eventID);
            } finally {
                chainable = false;
                releaseOwnedStack();
                CommandQueue.this.refresh();
            }
        }

        /**
         * Transfers ownership of the current chain stack to the next Event.
         */
        private Event transferOwnership(Event next) {
            Preconditions.checkNotNull(next);

            if (next.nonBlockingWrites == null) {
                next.nonBlockingWrites = this.nonBlockingWrites;
            } else if (this.nonBlockingWrites != null && next.nonBlockingWrites != this.nonBlockingWrites) {
                this.nonBlockingWrites.addAll(next.nonBlockingWrites);
                next.nonBlockingWrites = this.nonBlockingWrites;
            }

            next.ownsStack = this.ownsStack;
            this.ownsStack = false;
            this.chainable = false;
            return next;
        }

        /**
         * The current event is always the first OpenCL dependency.
         */
        private long[] dependencyIDs(Event... dependencies) {
            Preconditions.checkNotNull(dependencies);
            long[] result = new long[dependencies.length + 1];
            result[0] = eventID;

            for (int i = 0; i < dependencies.length; i++) {
                Event dependency = Preconditions.checkNotNull(dependencies[i]);
                Preconditions.checkArgument(
                        dependency != this,
                        "The current Event is already an implicit dependency."
                );
                result[i + 1] = dependency.eventID;
            }

            return result;
        }

        private void releaseDependencies(Event... dependencies) {
            for (Event dependency : dependencies) {
                if (dependency != null && dependency != this) {
                    dependency.releaseOwnedStack();
                    dependency.chainable = false;
                }
            }
        }

        /**
         * Releases only stacks created by CommandQueue itself. An externally
         * supplied MemoryStack remains the caller's responsibility.
         */
        private void releaseOwnedStack() {
            if (ownsStack) {
                stack.close();
                ownsStack = false;
                chainable = false;
            }
        }

        private void updateNonBlockingWrites(@NonNull SmartPointer object, boolean blocking) {
            Preconditions.checkNotNull(object);

            if (blocking) {
                clearNonBlockingWrites();
                return;
            }

            if (nonBlockingWrites == null) {
                nonBlockingWrites = new ReferenceArraySet<>();
            }
            nonBlockingWrites.add(object);
        }

        private void clearNonBlockingWrites() {
            if (nonBlockingWrites != null)
                nonBlockingWrites.clear();
        }

        private void checkNonBlockingWrite(SmartPointer object) {
            if (nonBlockingWrites != null && nonBlockingWrites.contains(object)) {
                Compute.instance().LOGGER.warn("Potential data race caused by operation involving {} after non-blocking write.", object instanceof Buffer ? "a buffer" : "an image");
            }
        }
    }
}

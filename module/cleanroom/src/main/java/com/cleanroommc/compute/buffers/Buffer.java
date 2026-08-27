package com.cleanroommc.compute.buffers;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.errors.BufferError;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CL11;
import org.lwjgl.opencl.CLBufferRegion;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.Closeable;
import java.io.IOException;
import java.nio.*;
import java.util.List;
import java.util.Set;

/**
 * OpenCL Buffer
 * @author EΣrie
 */
public class Buffer extends SmartPointer {

    private final @Nullable Buffer parent;
    private final List<Buffer> children = new ReferenceArrayList<>();
    /**
     * The actual OpenCL Buffer
     */
    public final long handle;
    /**
     * Size of this buffer.
     */
    public final long size;
    private final Set<BufferFlags> flags = new ObjectArraySet<>();
    private final @Nullable GLBuffer glBuffer;
    /**
     * Is this Buffer readable?
     */
    public final boolean canRead;
    /**
     * Is this Buffer writable?
     */
    public final boolean canWrite;

    /**
     * Creates an OpenCL Buffer
     * @param stack MemoryStack
     * @param size length of the buffer in bytes
     * @param flags {@link BufferFlags Buffer memory flags}
     * @author EΣrie
     */
    public Buffer(@NonNull MemoryStack stack, long size, @NonNull BufferFlags... flags) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkArgument(size != 0, "Size can not be equal to 0.");
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");

        super();

        long openCLFlags = 0;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);
            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = null;
        this.size = size;
        this.glBuffer = null;
        this.canRead = canRead;
        this.canWrite = canWrite;

        try (MemoryStack substack = stack.push()) {
            IntBuffer err = substack.mallocInt(1);
            handle = CL10.clCreateBuffer(Compute.instance().context, openCLFlags, size, err);

            switch (err.get(0)) {
                case CL10.CL_INVALID_CONTEXT -> throw new IllegalStateException("Can't create buffer, invalid context. This should not hapen. Something is seriously wrong.");
                case CL10.CL_INVALID_VALUE -> throw new IllegalArgumentException("Can't create buffer, invalid provided flags.");
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }
    }

    /**
     * Creates an OpenCL Buffer from host memory.
     * @param stack MemoryStack
     * @param hostMemoryBuffer NIO Buffer whose data will be copied to the OpenCL buffer.
     * @param flags {@link BufferFlags Buffer memory flags}
     * @param <B> NIO Buffer type
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Buffer(@NonNull MemoryStack stack, @NonNull B hostMemoryBuffer, @NonNull BufferFlags... flags) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(hostMemoryBuffer);
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");

        super();

        long openCLFlags = CL10.CL_MEM_COPY_HOST_PTR;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);
            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = null;
        this.size = switch (hostMemoryBuffer) {
            case ByteBuffer buffer -> buffer.remaining();
            case ShortBuffer buffer -> (long) buffer.remaining() * Short.BYTES;
            case IntBuffer buffer -> (long) buffer.remaining() * Integer.BYTES;
            case FloatBuffer buffer -> (long) buffer.remaining() * Float.BYTES;
            case DoubleBuffer buffer -> (long) buffer.remaining() * Double.BYTES;
            default -> throw new IllegalArgumentException("Unsupported host memory buffer type.");
        };
        this.glBuffer = null;
        this.canRead = canRead;
        this.canWrite = canWrite;

        try (MemoryStack substack = stack.push()) {
            IntBuffer err = substack.mallocInt(1);

            handle = switch (hostMemoryBuffer) {
                case ByteBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case ShortBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case IntBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case FloatBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case DoubleBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                default -> throw new IllegalArgumentException("Unsupported host memory buffer type.");
            };

            switch (err.get(0)) {
                case CL10.CL_INVALID_CONTEXT -> throw new IllegalStateException("Can't create buffer, invalid context. This should not hapen. Something is seriously wrong.");
                case CL10.CL_INVALID_VALUE -> throw new IllegalArgumentException("Can't create buffer, invalid provided flags.");
                case CL10.CL_INVALID_HOST_PTR -> throw new IllegalArgumentException("Can't create buffer, invalid host memory buffer.");
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }
    }

    /**
     * Create a sub-buffer
     * @param stack MemoryStack
     * @param parent Parent buffer. This buffer will be divided from said parent.
     * @param offset Offset in bytes from the beginning of the parent buffer.
     * @param size Size in bytes of the sub-buffer.
     * @param flags {@link BufferFlags Buffer memory flags}
     * @author EΣrie
     */
    public Buffer(@NonNull MemoryStack stack, @NonNull Buffer parent, long offset, long size, @NonNull BufferFlags... flags) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(parent);
        Preconditions.checkArgument(offset + size <= parent.size, "Subbuffer goes outside parent buffer.");
        Preconditions.checkArgument(size != 0, "Size can not be equal to 0.");
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");

        super();

        long openCLFlags = 0;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);

            for (BufferFlags parentFlag : parent.flags) {
                Preconditions.checkArgument(!parentFlag.isConflicting(flag));
            }

            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = parent;
        this.size = size;
        this.glBuffer = null;
        this.canRead = canRead;
        this.canWrite = canWrite;

        try (MemoryStack substack = stack.push()) {
            IntBuffer err = substack.mallocInt(1);
            ByteBuffer region = substack.malloc(CLBufferRegion.SIZE);

            try (CLBufferRegion tmp = new CLBufferRegion(region)) {
                tmp.set(offset, size);
            }

            handle = CL11.clCreateSubBuffer(parent.handle, openCLFlags, CL11.CL_BUFFER_CREATE_TYPE_REGION, region, err);

            switch (err.get(0)) {
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }

        this.parent.children.add(this);
    }

    /**
     * Create an OpenCL Buffer.
     * @param size Size of the buffer in bytes.
     * @param flags {@link BufferFlags Buffer memory flags}
     * @author EΣrie
     */
    public Buffer(long size, @NonNull BufferFlags... flags) {
        Preconditions.checkArgument(size != 0, "Size can not be equal to 0.");
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");

        super();

        long openCLFlags = 0;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);
            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = null;
        this.size = size;
        this.glBuffer = null;
        this.canRead = canRead;
        this.canWrite = canWrite;

        try (MemoryStack substack = MemoryStack.stackPush()) {
            IntBuffer err = substack.mallocInt(1);
            handle = CL10.clCreateBuffer(Compute.instance().context, openCLFlags, size, err);

            switch (err.get(0)) {
                case CL10.CL_INVALID_CONTEXT -> throw new IllegalStateException("Can't create buffer, invalid context. This should not hapen. Something is seriously wrong.");
                case CL10.CL_INVALID_VALUE -> throw new IllegalArgumentException("Can't create buffer, invalid provided flags.");
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }
    }

    /**
     * Creates an OpenCL Buffer from host memory.
     * @param hostMemoryBuffer NIO Buffer whose data will be copied to the OpenCL buffer.
     * @param flags {@link BufferFlags Buffer memory flags}
     * @param <B> NIO Buffer type
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> Buffer(@NonNull B hostMemoryBuffer, @NonNull BufferFlags... flags) {
        Preconditions.checkNotNull(hostMemoryBuffer);
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");

        super();

        long openCLFlags = CL10.CL_MEM_COPY_HOST_PTR;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);
            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = null;
        this.size = switch (hostMemoryBuffer) {
            case ByteBuffer buffer -> buffer.remaining();
            case ShortBuffer buffer -> (long) buffer.remaining() * Short.BYTES;
            case IntBuffer buffer -> (long) buffer.remaining() * Integer.BYTES;
            case FloatBuffer buffer -> (long) buffer.remaining() * Float.BYTES;
            case DoubleBuffer buffer -> (long) buffer.remaining() * Double.BYTES;
            default -> throw new IllegalArgumentException("Unsupported host memory buffer type.");
        };
        this.glBuffer = null;
        this.canRead = canRead;
        this.canWrite = canWrite;

        try (MemoryStack substack = MemoryStack.stackPush()) {
            IntBuffer err = substack.mallocInt(1);

            handle = switch (hostMemoryBuffer) {
                case ByteBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case ShortBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case IntBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case FloatBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                case DoubleBuffer buffer -> CL10.clCreateBuffer(Compute.instance().context, openCLFlags, buffer, err);
                default -> throw new IllegalArgumentException("Unsupported host memory buffer type.");
            };

            switch (err.get(0)) {
                case CL10.CL_INVALID_CONTEXT -> throw new IllegalStateException("Can't create buffer, invalid context. This should not hapen. Something is seriously wrong.");
                case CL10.CL_INVALID_VALUE -> throw new IllegalArgumentException("Can't create buffer, invalid provided flags.");
                case CL10.CL_INVALID_HOST_PTR -> throw new IllegalArgumentException("Can't create buffer, invalid host memory buffer.");
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }
    }

    /**
     * Create a sub-buffer
     * @param parent Parent buffer. This buffer will be divided from said parent.
     * @param offset Offset in bytes from the beginning of the parent buffer.
     * @param size Size in bytes of the sub-buffer.
     * @param flags {@link BufferFlags Buffer memory flags}
     * @author EΣrie
     */
    public Buffer(@NonNull Buffer parent, long offset, long size, @NonNull BufferFlags... flags) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkArgument(offset + size <= parent.size, "Subbuffer goes outside parent buffer.");
        Preconditions.checkArgument(size != 0, "Size can not be equal to 0.");
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");

        super();

        long openCLFlags = 0;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);

            for (BufferFlags parentFlag : parent.flags) {
                Preconditions.checkArgument(!parentFlag.isConflicting(flag));
            }

            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = parent;
        this.size = size;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.glBuffer = null;

        try (MemoryStack substack = MemoryStack.stackPush()) {
            IntBuffer err = substack.mallocInt(1);
            ByteBuffer region = substack.malloc(CLBufferRegion.SIZEOF);

            try (CLBufferRegion tmp = new CLBufferRegion(region)) {
                tmp.set(offset, size);
            }

            handle = CL11.clCreateSubBuffer(parent.handle, openCLFlags, CL11.CL_BUFFER_CREATE_TYPE_REGION, region, err);

            switch (err.get(0)) {
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }

        this.parent.children.add(this);
    }

    /**
     * Create an OpenCL Buffer from an OpenGL buffer.
     * This is necessary for GL sharing.
     * @param glBuffer The OpenGL buffer.
     * @param flags {@link BufferFlags Buffer memory flags}
     * @author EΣrie
     */
    public Buffer(@NonNull GLBuffer glBuffer, @NonNull BufferFlags... flags) {
        Preconditions.checkNotNull(glBuffer);
        Preconditions.checkNotNull(flags);
        Preconditions.checkArgument(flags.length != 0, "At least one flag must be provided.");
        Preconditions.checkArgument(glBuffer.bufferID != 0, "Can't create buffer from a null GL buffer.");

        super();

        long openCLFlags = 0;
        boolean canRead = false;
        boolean canWrite = false;

        for (BufferFlags flag : flags) {
            Preconditions.checkNotNull(flag);
            Preconditions.checkArgument(flag.ordinal() < 3, "Flag %s is not allowed for GL buffers.", flag);

            openCLFlags |= flag.flags;
            canRead |= flag.canRead;
            canWrite |= flag.canWrite;
            this.flags.add(flag);
        }

        this.parent = null;
        this.size = -1;
        this.glBuffer = glBuffer;
        this.canRead = canRead;
        this.canWrite = canWrite;

        int[] err = new int[1];

        this.handle = CL10GL.clCreateFromGLBuffer(
                Compute.instance().context,
                openCLFlags,
                glBuffer.bufferID,
                err
        );

        switch (err[0]) {
            case CL10GL.CL_INVALID_GL_OBJECT -> throw new BufferError("Can't create buffer, invalid gl object.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
        }
    }

    /**
     * <p>Write data to the buffer from a float array.</p>
     * @param stack MemoryStack
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, float[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, long, float[], long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, float[], boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, float[], long...)
     * @see CommandQueue#bufferWrite(Buffer, float[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, float[], boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, long, float[], long...)
     * @see CommandQueue#bufferWrite(Buffer, float[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                      float @NonNull [] data, boolean blocking,
                      long offset, long... events) throws
            NullPointerException, IllegalArgumentException,
            IllegalStateException, BufferError, OutOfMemoryError {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size,
                "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue.commandQueue, handle, blocking, offset, data, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data to the buffer from a double array.</p>
     * @param stack MemoryStack
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, double[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, long, double[], long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, double[], boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, double[], long...)
     * @see CommandQueue#bufferWrite(Buffer, double[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, double[], boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, long, double[], long...)
     * @see CommandQueue#bufferWrite(Buffer, double[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @author EΣrie
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     */
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                      double @NonNull [] data, boolean blocking,
                      long offset, long... events) throws
            NullPointerException, IllegalArgumentException,
            IllegalStateException, BufferError, OutOfMemoryError {
        final int sizeof = 8;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size,
                "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue.commandQueue, handle, blocking, offset, data, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data to the buffer from a short array.</p>
     * @param stack MemoryStack
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, short[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, long, short[], long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, short[], boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, short[], long...)
     * @see CommandQueue#bufferWrite(Buffer, short[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, short[], boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, long, short[], long...)
     * @see CommandQueue#bufferWrite(Buffer, short[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, short @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 2;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue.commandQueue, handle, blocking, offset, data,
                    glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data to the buffer from an int array.</p>
     * @param stack MemoryStack
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @return Event of the write operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, int[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, long, int[], long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, int[], boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, int[], long...)
     * @see CommandQueue#bufferWrite(Buffer, int[], long, boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, int[], boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, long, int[], long...)
     * @see CommandQueue#bufferWrite(Buffer, int[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue.commandQueue, handle, blocking, offset, data,
                    glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    // TODO: Test this one and decide if it should stay.
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, byte @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 1;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            ByteBuffer buffer = substack.malloc(data.length);
            buffer.put(data);
            buffer.flip();

            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue.commandQueue, handle, blocking, offset, buffer, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data to the buffer from a NIO Buffer.</p>
     * @param stack MemoryStack
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param data Data to write to the buffer.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the writing.
     * @param events What this operation depends on.
     * @param <B> Type of the NIO buffer this will fetch data from.
     * @return Event of the write operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, java.nio.Buffer, long, boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, long, java.nio.Buffer, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, java.nio.Buffer, boolean, long...)
     * @see CommandQueue#bufferWrite(MemoryStack, Buffer, java.nio.Buffer, long...)
     * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, long, boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, boolean, long...)
     * @see CommandQueue#bufferWrite(Buffer, long, java.nio.Buffer, long...)
     * @see CommandQueue#bufferWrite(Buffer, java.nio.Buffer, long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If data is empty, an attempt to write data beyond the buffer's end is made,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support writing.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to write to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> long write(
            @NonNull MemoryStack stack,
            CommandQueue commandQueue,
            @NonNull B data,
            boolean blocking,
            long offset,
            long... events
    ) {
        final int sizeof = bufferElementSize(data);

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.remaining() > 0, "Attempted to write data of size 0.");
        Preconditions.checkArgument(
                glBuffer == null || offset + ((long) data.remaining() * sizeof) <= size,
                "Attempted to write more data than the buffer can hold."
        );
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());
        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null) {
                acquireGLObjects(substack, commandQueue, dependencies, event);
            }
            PointerBuffer waitList = glBuffer == null
                    ? dependencies
                    : dependencies.getPointerBuffer(1);
            checkBufferWriteErrors(switch (data) {
                case ByteBuffer buffer ->
                        CL10.clEnqueueWriteBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case ShortBuffer buffer ->
                        CL10.clEnqueueWriteBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case IntBuffer buffer ->
                        CL10.clEnqueueWriteBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case FloatBuffer buffer ->
                        CL10.clEnqueueWriteBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case DoubleBuffer buffer ->
                        CL10.clEnqueueWriteBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            if (events != null && glBuffer == null) {
                for (long dependency : events) {
                    CL10.clReleaseEvent(dependency);
                }
            } else if (glBuffer != null) {
                releaseGLObjects(commandQueue, dependencies, event);
            }
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read data from the buffer and write it to a NIO Buffer.</p>
     * @param stack MemoryStack
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param target Buffer where data from the OpenCL buffer will be written to.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the reading.
     * @param events What this operation depends on.
     * @param <B> Type of the NIO buffer this will fetch data from.
     * @return Event of the read operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, java.nio.Buffer, long, boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, long, java.nio.Buffer, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, java.nio.Buffer, boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, java.nio.Buffer, long...)
     * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, long, boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, long, java.nio.Buffer, long...)
     * @see CommandQueue#bufferRead(Buffer, java.nio.Buffer, long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If the target buffer is too small,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support reading.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to read from to the buffer.
     * @author EΣrie
     */
    public <B extends java.nio.Buffer> long read(
            @NonNull MemoryStack stack,
            CommandQueue commandQueue,
            @NonNull B target,
            boolean blocking,
            long offset,
            long... events
    ) {
        final int sizeof = bufferElementSize(target);

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(
                target.remaining() >= sizeof,
                "Attempted to write to a buffer that is too small."
        );
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null) {
                acquireGLObjects(substack, commandQueue, dependencies, event);
            }
            PointerBuffer waitList = glBuffer == null
                    ? dependencies
                    : dependencies.getPointerBuffer(1);
            checkBufferReadErrors(switch (target) {
                case ByteBuffer buffer ->
                        CL10.clEnqueueReadBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case ShortBuffer buffer ->
                        CL10.clEnqueueReadBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case IntBuffer buffer ->
                        CL10.clEnqueueReadBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case FloatBuffer buffer ->
                        CL10.clEnqueueReadBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                case DoubleBuffer buffer ->
                        CL10.clEnqueueReadBuffer(
                                commandQueue.commandQueue,
                                handle,
                                blocking,
                                offset,
                                buffer,
                                waitList,
                                event
                        );
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            if (events != null && glBuffer == null) {
                for (long dependency : events) {
                    CL10.clReleaseEvent(dependency);
                }
            } else if (glBuffer != null) {
                releaseGLObjects(commandQueue, dependencies, event);
            }
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read data from the buffer and write it to a short array.</p>
     * @param stack MemoryStack
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param target Array where data from the OpenCL buffer will be written to.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the reading.
     * @param events What this operation depends on.
     * @return Event of the read operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, short[], long, boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, long, short[], long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, short[], boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, short[], long...)
     * @see CommandQueue#bufferRead(Buffer, short[], long, boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, short[], boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, long, short[], long...)
     * @see CommandQueue#bufferRead(Buffer, short[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If the target array is too small,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support reading.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to read from the buffer.
     * @author EΣrie
     */
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, short @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 2;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferReadErrors(CL10.clEnqueueReadBuffer(commandQueue.commandQueue, this.handle, blocking, offset, target, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read data from the buffer and write it to an int array.</p>
     * @param stack MemoryStack
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param target Array where data from the OpenCL buffer will be written to.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the reading.
     * @param events What this operation depends on.
     * @return Event of the read operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, int[], long, boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, long, int[], long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, int[], boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, int[], long...)
     * @see CommandQueue#bufferRead(Buffer, int[], long, boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, int[], boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, long, int[], long...)
     * @see CommandQueue#bufferRead(Buffer, int[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If the target array is too small,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support reading.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to read from the buffer.
     * @author EΣrie
     */
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferReadErrors(CL10.clEnqueueReadBuffer(commandQueue.commandQueue, this.handle, blocking, offset, target, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read data from the buffer and write it to a float array.</p>
     * @param stack MemoryStack
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param target Array where data from the OpenCL buffer will be written to.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the reading.
     * @param events What this operation depends on.
     * @return Event of the read operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, float[], long, boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, long, float[], long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, float[], boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, float[], long...)
     * @see CommandQueue#bufferRead(Buffer, float[], long, boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, float[], boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, long, float[], long...)
     * @see CommandQueue#bufferRead(Buffer, float[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If the target array is too small,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support reading.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to read from the buffer.
     * @author EΣrie
     */
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferReadErrors(CL10.clEnqueueReadBuffer(commandQueue.commandQueue, this.handle, blocking, offset, target, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read data from the buffer and write it to a double array.</p>
     * @param stack MemoryStack
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param target Array where data from the OpenCL buffer will be written to.
     * @param blocking Is this a blocking operation?
     * @param offset Where to start the reading.
     * @param events What this operation depends on.
     * @return Event of the read operation.
     * @apiNote Do not call directly, only call from {@link CommandQueue}
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, double[], long, boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, long, double[], long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, double[], boolean, long...)
     * @see CommandQueue#bufferRead(MemoryStack, Buffer, double[], long...)
     * @see CommandQueue#bufferRead(Buffer, double[], long, boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, double[], boolean, long...)
     * @see CommandQueue#bufferRead(Buffer, long, double[], long...)
     * @see CommandQueue#bufferRead(Buffer, double[], long...)
     * @implNote This operation is enqueued on the OpenCL {@link CommandQueue}
     * and ran when said queue is flushed with {@link CommandQueue.Event#execute()}
     * @throws NullPointerException If stack, data or commandQueue is null.
     * @throws IllegalArgumentException If the target array is too small,
     * the commandQueue has already been closed, or when there has been a negative value passed in events.
     * @throws IllegalStateException When the buffer does not support reading.
     * @throws BufferError Either when: this buffer is an invalid memory object, one or more events is invalid, or when
     * the sub-buffer offset is misaligned.
     * @throws OutOfMemoryError When there is not enough memory available to read from the buffer.
     * @author EΣrie
     */
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, double @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 8;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer dependencies;
            if (events != null && events.length > 0) {
                dependencies = substack.mallocPointer(events.length);
                dependencies.put(events);
                dependencies.rewind();
            } else {
                dependencies = null;
            }
            PointerBuffer event = substack.mallocPointer(1);
            if (glBuffer != null)
                acquireGLObjects(substack, commandQueue, dependencies, event);
            checkBufferReadErrors(CL10.clEnqueueReadBuffer(commandQueue.commandQueue, this.handle, blocking, offset, target, glBuffer == null ? dependencies : dependencies.getPointerBuffer(1), event));
            if (events != null && glBuffer == null)
                for (long dependency : events)
                    CL10.clReleaseEvent(dependency);
            else if (glBuffer != null)
                releaseGLObjects(commandQueue, dependencies, event);
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Closes the buffer and frees the memory.</p>
     * <p>The buffer will be removed from the reference graph.</p>
     * @implNote If the buffer has any children, they will be closed as well.
     * @see SmartPointer#close()
     * @author EΣrie
     */
    @Override
    public void close() {
        super.close();
        for(Buffer child : children) {
            if (child.isClosed())
                child.close();
        }
        CL10.clReleaseMemObject(handle);
    }

    /**
     * @return Is this a GL shared buffer or not?
     */
    public boolean isGLObject() {
        return glBuffer != null;
    }

    /**
     * <p><i>Internal Function</i></p>
     * <p>Used to acquire GL objects to prevent data races between OpenGL and OpenCL.</p>
     * @param substack MemoryStack
     * @param commandQueue CommandQueue
     * @param dependencies dependent events, can be null
     * @param event event of the acquisition
     * @param events same as dependencies, just in array format for ease of use.
     * @apiNote This is an internal function, do not use it yourself and <b>DO NOT</b> MIXIN OR ASM THIS FUNCTION OUT,
     * IT WILL CAUSE ANY POTENTIAL USES OF COMPUTE IN RENDERING TO CRASH!!!
     * @author EΣrie
     */
    private void acquireGLObjects(MemoryStack substack, CommandQueue commandQueue,
                                  @Nullable PointerBuffer dependencies,
                                  PointerBuffer event, long... events) {
        CL10GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, dependencies, event);
        if (dependencies != null) {
            for (long dependency : events)
                CL10.clReleaseEvent(dependency);
            dependencies.put(0, event.get(0));
        } else {
            dependencies = substack.mallocPointer(1);
            dependencies.put(0, event.get(0));
        }
        dependencies.rewind();
    }

    /**
     * <p><i>Internal Function</i></p>
     * <p>Used to release GL objects to prevent data races between OpenGL and OpenCL.</p>
     * @param commandQueue CommandQueue
     * @param dependencies dependent events
     * @param event buffer with the event of the previous operation.
     * @apiNote This is an internal function, do not use it yourself and <b>DO NOT</b> MIXIN OR ASM THIS FUNCTION OUT,
     * IT WILL CAUSE ANY POTENTIAL USES OF COMPUTE IN RENDERING TO CRASH!!!
     * @author EΣrie
     */
    private void releaseGLObjects(CommandQueue commandQueue, PointerBuffer dependencies, PointerBuffer event) {
        dependencies.put(0, event.get(0)).rewind();
        CL10GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, handle, dependencies, event);
        CL10.clReleaseEvent(dependencies.get(0));
    }

    /**
     * <p><i>Internal Function</i></p>
     * <p>Error check</p>
     * @param err error code
     * @author EΣrie
     */
    private static void checkBufferWriteErrors(int err) {
        switch (err) {
            case CL10.CL_INVALID_MEM_OBJECT -> throw new BufferError("Buffer is not a valid memory object.");
            case CL10.CL_INVALID_EVENT_WAIT_LIST -> throw new BufferError("One or more events in wait list is invalid.");
            case CL11.CL_MISALIGNED_SUB_BUFFER_OFFSET -> throw new BufferError("Misaligned subbuffer offset");
            case CL11.CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST -> throw new IllegalArgumentException("Negative event value in wait list for blocking buffer write operation.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to write to buffer.");
        }
    }

    /**
     * <p><i>Internal Function</i></p>
     * <p>Error check</p>
     * @param err error code
     * @author EΣrie
     */
    private static void checkBufferReadErrors(int err) {
        switch (err) {
            case CL10.CL_INVALID_MEM_OBJECT -> throw new BufferError("Buffer is not a valid memory object.");
            case CL10.CL_INVALID_VALUE -> throw new ArrayIndexOutOfBoundsException("Attempted to read buffer out of bounds.");
            case CL11.CL_MISALIGNED_SUB_BUFFER_OFFSET -> throw new BufferError("Misaligned subbuffer offset");
            case CL11.CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST -> throw new IllegalArgumentException("Negative event value in wait list for blocking buffer write operation.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to write to buffer.");
        }
    }

    /**
     * <p><i>Internal function</i></p>
     * @param buffer NIO Buffer
     * @return Size of a single buffer element.
     * @throws IllegalArgumentException If the buffer is not one of: ByteBuffer, ShortBuffer,
     * IntBuffer, FloatBuffer, DoubleBuffer.
     */
    private static int bufferElementSize(java.nio.Buffer buffer) throws IllegalArgumentException {
        return switch (buffer) {
            case ByteBuffer ignored -> Byte.BYTES;
            case ShortBuffer ignored -> Short.BYTES;
            case IntBuffer ignored -> Integer.BYTES;
            case FloatBuffer ignored -> Float.BYTES;
            case DoubleBuffer ignored -> Double.BYTES;
            default -> throw new IllegalArgumentException("Wrong buffer type.");
        };
    }
}

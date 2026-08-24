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

public class Buffer extends SmartPointer {

    private final @Nullable Buffer parent;
    private final List<Buffer> children = new ReferenceArrayList<>();
    public final long handle;
    public final long size;
    private final Set<BufferFlags> flags = new ObjectArraySet<>();
    private final @Nullable GLBuffer glBuffer;
    public final boolean canRead;
    public final boolean canWrite;

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

    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size,
                "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, double @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 8;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size,
                "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, short @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 2;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, byte @NonNull [] data, boolean blocking, long offset, long... events) {
        final int sizeof = 1;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.length > 0, "Attempted to write data of size 0.");
        if (glBuffer == null) Preconditions.checkArgument(offset + ((long) data.length * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(canWrite, "Attempted to write to read-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

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
        }
    }

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
        }
    }

    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, short @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 2;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, double @NonNull [] target, boolean blocking, long offset, long... events) {
        final int sizeof = 8;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkArgument(target.length >= sizeof, "Attempted to write to an array that is too small.");
        Preconditions.checkState(canRead, "Attempted to read from a write-only or no-access buffer");
        Preconditions.checkArgument(!commandQueue.isClosed());

        try (MemoryStack substack = stack.push()) {
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
        }
    }

    @Override
    public void close() {
        super.close();
        for(Buffer child : children) {
            if (child.isClosed())
                child.close();
        }
        CL10.clReleaseMemObject(handle);
    }

    public boolean isGLObject() {
        return glBuffer != null;
    }

    public void acquireGLObjects(MemoryStack substack, CommandQueue commandQueue, PointerBuffer dependencies, PointerBuffer event, long... events) {
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

    public void releaseGLObjects(CommandQueue commandQueue, PointerBuffer dependencies, PointerBuffer event) {
        dependencies.put(0, event.get(0)).rewind();
        CL10GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, handle, dependencies, event);
        CL10.clReleaseEvent(dependencies.get(0));
    }

    private static void checkBufferWriteErrors(int err) {
        switch (err) {
            case CL10.CL_INVALID_MEM_OBJECT -> throw new BufferError("Buffer is not a valid memory object.");
            case CL10.CL_INVALID_EVENT_WAIT_LIST -> throw new BufferError("One or more events in wait list is invalid.");
            case CL11.CL_MISALIGNED_SUB_BUFFER_OFFSET -> throw new BufferError("Misaligned subbuffer offset");
            case CL11.CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST -> throw new IllegalArgumentException("Negative event value in wait list for blocking buffer write operation.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to write to buffer.");
        }
    }

    private static void checkBufferReadErrors(int err) {
        switch (err) {
            case CL10.CL_INVALID_MEM_OBJECT -> throw new BufferError("Buffer is not a valid memory object.");
            case CL10.CL_INVALID_VALUE -> throw new ArrayIndexOutOfBoundsException("Attempted to read buffer out of bounds.");
            case CL11.CL_MISALIGNED_SUB_BUFFER_OFFSET -> throw new BufferError("Misaligned subbuffer offset");
            case CL11.CL_EXEC_STATUS_ERROR_FOR_EVENTS_IN_WAIT_LIST -> throw new IllegalArgumentException("Negative event value in wait list for blocking buffer write operation.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to write to buffer.");
        }
    }

    private static int bufferElementSize(java.nio.Buffer buffer) {
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

package com.cleanroommc.cleanroom.compute.buffers;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.errors.BufferError;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL11;
import org.lwjgl.opencl.CLBufferRegion;
import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.io.IOException;
import java.nio.*;
import java.util.List;

public class Buffer implements Closeable {

    private final @Nullable Buffer parent;
    private final List<Buffer> children = new ReferenceArrayList<>();
    private final long handle;
    public final long size;
    public final BufferFlags flags;
    private boolean isClosed = false;

    public Buffer(@NonNull MemoryStack stack, long size, @NonNull BufferFlags flags) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkArgument(size != 0, "Size can not be equal to 0.");
        Preconditions.checkNotNull(flags);
        this.parent = null;
        this.size = size;
        this.flags = flags;
        try (MemoryStack substack = stack.push()) {
            IntBuffer err = substack.mallocInt(1);
            handle = CL10.clCreateBuffer(Compute.instance().context, flags.flags, size, err);
            switch (err.get(0)) {
                case CL10.CL_INVALID_CONTEXT -> throw new IllegalStateException("Can't create buffer, invalid context. This should not hapen. Something is seriously wrong.");
                case CL10.CL_INVALID_VALUE -> throw new IllegalArgumentException("Can't create buffer, invalid provided flags.");
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }
    }

    public Buffer(@NonNull MemoryStack stack, @NonNull Buffer parent, long offset, long size, @NonNull BufferFlags flags) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(parent);
        Preconditions.checkArgument(offset + size <= parent.size, "Subbuffer goes outside parent buffer.");
        Preconditions.checkArgument(!parent.flags.isConflicting(flags), "Conflict between buffer and subbuffer.");
        Preconditions.checkArgument(size != 0, "Size can not be equal to 0.");
        Preconditions.checkNotNull(flags);
        this.parent = parent;
        this.size = size;
        this.flags = flags;
        try (MemoryStack substack = stack.push()) {
            IntBuffer err = substack.mallocInt(1);
            ByteBuffer region = substack.malloc(CLBufferRegion.SIZE);
            try (CLBufferRegion tmp = new CLBufferRegion(region)) {
                tmp.set(offset, size);
            }
            handle = CL11.clCreateSubBuffer(parent.handle, flags.flags, CL11.CL_BUFFER_CREATE_TYPE_REGION, region, err);
            switch (err.get(0)) { // All other errors should be eliminated by preconditions
                case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new BufferError("Can't create buffer, allocation failed.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create a buffer.");
            }
        }
        this.parent.children.add(this);
    }

    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull ByteBuffer data, boolean blocking, long offset, long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.remaining() > 0, "Attempted to write data of size 0.");
        Preconditions.checkArgument(offset + data.remaining() <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(flags.canWrite, "Attempted to write to read-only or no-access buffer");

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
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue, handle, blocking, offset, data, dependencies, event));
            return event.get(0);
        }
    }

    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull ShortBuffer data, boolean blocking, long offset, long... events) {
        final int sizeof = 2;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.remaining() > 0, "Attempted to write data of size 0.");
        Preconditions.checkArgument(offset + ((long) data.remaining() * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(flags.canWrite, "Attempted to write to read-only or no-access buffer");

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
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue, handle, blocking, offset, data, dependencies, event));
            return event.get(0);
        }
    }

    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull IntBuffer data, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.remaining() > 0, "Attempted to write data of size 0.");
        Preconditions.checkArgument(offset + ((long) data.remaining() * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(flags.canWrite, "Attempted to write to read-only or no-access buffer");

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
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue, handle, blocking, offset, data, dependencies, event));
            return event.get(0);
        }
    }

    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull FloatBuffer data, boolean blocking, long offset, long... events) {
        final int sizeof = 4;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.remaining() > 0, "Attempted to write data of size 0.");
        Preconditions.checkArgument(offset + ((long) data.remaining() * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(flags.canWrite, "Attempted to write to read-only or no-access buffer");

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
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue, handle, blocking, offset, data, dependencies, event));
            return event.get(0);
        }
    }

    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull DoubleBuffer data, boolean blocking, long offset, long... events) {
        final int sizeof = 8;

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkArgument(data.remaining() > 0, "Attempted to write data of size 0.");
        Preconditions.checkArgument(offset + ((long) data.remaining() * sizeof) <= size, "Attempted to write more data than the buffer can hold.");
        Preconditions.checkState(flags.canWrite, "Attempted to write to read-only or no-access buffer");

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
            checkBufferWriteErrors(CL10.clEnqueueWriteBuffer(commandQueue, handle, blocking, offset, data, dependencies, event));
            return event.get(0);
        }
    }

    @Override
    public void close() throws IOException {
        this.isClosed = true;
        for(Buffer child : children) {
            if (!child.isClosed)
                child.close();
        }
        CL10.clReleaseMemObject(handle);
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
}

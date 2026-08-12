package com.cleanroommc.cleanroom.compute.cmd;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.buffers.Buffer;
import com.cleanroommc.cleanroom.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.cleanroom.compute.kernels.Kernel;
import com.cleanroommc.cleanroom.compute.kernels.params.KernelParameterList;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.system.MemoryStack;

import java.io.Closeable;
import java.io.IOException;
import java.nio.*;

public class CommandQueue implements Closeable {

    public final long commandQueue;
    private final long device;

    public CommandQueue(long device) {
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

    //</editor-fold>

    //<editor-fold desc="Buffer Write">

    //<editor-fold desc="Buffer Write Float">

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final @NonNull FloatBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final @NonNull FloatBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull FloatBuffer data,
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

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final float @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final float @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

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

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull DoubleBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final @NonNull DoubleBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull DoubleBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull DoubleBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull DoubleBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final @NonNull DoubleBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull DoubleBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull DoubleBuffer data,
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

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final double @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final double @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

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

    //<editor-fold desc="Buffer Write Byte">

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull ByteBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final @NonNull ByteBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull ByteBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull ByteBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull ByteBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final @NonNull ByteBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull ByteBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull ByteBuffer data,
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

    //<editor-fold desc="Buffer Write Short">

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull ShortBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final @NonNull ShortBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull ShortBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull ShortBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull ShortBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final @NonNull ShortBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull ShortBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull ShortBuffer data,
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

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final short @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final short @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

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

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull IntBuffer data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final @NonNull IntBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull IntBuffer data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final @NonNull IntBuffer data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull IntBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final long offset,
                             final @NonNull IntBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull IntBuffer data,
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

    public Event bufferWrite(@NonNull Buffer buffer,
                             final @NonNull IntBuffer data,
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

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long offset,
                             final boolean blocking,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final long offset,
                             final int @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, offset, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final boolean blocking, final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, blocking, 0, events), stack);
    }

    public Event bufferWrite(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                             final int @NonNull [] data,
                             final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(data);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.write(stack, commandQueue, data, true, 0, events), stack);
    }

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

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull FloatBuffer target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            @NonNull FloatBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull FloatBuffer target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull FloatBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            float @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            float @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull FloatBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            @NonNull FloatBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull FloatBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull FloatBuffer target,
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

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull DoubleBuffer target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            @NonNull DoubleBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull DoubleBuffer target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull DoubleBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            double @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            double @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull DoubleBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            @NonNull DoubleBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull DoubleBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull DoubleBuffer target,
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

    //<editor-fold desc="Buffer Read Byte">

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull ByteBuffer target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            @NonNull ByteBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull ByteBuffer target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull ByteBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull ByteBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            @NonNull ByteBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull ByteBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull ByteBuffer target,
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

    //<editor-fold desc="Buffer Read Short">

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull ShortBuffer target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            @NonNull ShortBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull ShortBuffer target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull ShortBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            short @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            short @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull ShortBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            @NonNull ShortBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull ShortBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull ShortBuffer target,
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

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull IntBuffer target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            @NonNull IntBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull IntBuffer target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            @NonNull IntBuffer target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long offset,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            final long offset,
                            int @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, offset, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final boolean blocking,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, blocking, 0, events), stack);
    }

    public Event bufferRead(@NonNull MemoryStack stack, @NonNull Buffer buffer,
                            int @NonNull [] target,
                            final long... events) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(target);
        Preconditions.checkNotNull(buffer);
        return new Event(buffer.read(stack, this.commandQueue, target, true, 0, events), stack);
    }

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull IntBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            final long offset,
                            @NonNull IntBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull IntBuffer target,
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

    public Event bufferRead(@NonNull Buffer buffer,
                            @NonNull IntBuffer target,
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

    @Override
    public void close() throws IOException {
        CL20.clReleaseCommandQueue(commandQueue);
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

        Event(long eventID, @NonNull MemoryStack stack) {
            this.eventID = eventID;
            this.stack = Preconditions.checkNotNull(stack);
        }

        public Event next(@NonNull Kernel kernel,
                          final @NonNull KernelParameterList arguments,
                          final long @Nullable [] workGroupOffsets,
                          final long @NonNull [] workGroupSizes,
                          final Event... dependencies) {
            Preconditions.checkNotNull(kernel);
            Preconditions.checkNotNull(arguments);
            Preconditions.checkNotNull(workGroupSizes);

            ensureChainable();
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

        //<editor-fold desc="Buffer Write">

        //<editor-fold desc="Buffer Write Float">

        public Event write(@NonNull Buffer buffer,
                           final @NonNull FloatBuffer data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final @NonNull FloatBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull FloatBuffer data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull FloatBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final float @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final float @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final float @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final float @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write Double">

        public Event write(@NonNull Buffer buffer,
                           final @NonNull DoubleBuffer data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final @NonNull DoubleBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull DoubleBuffer data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull DoubleBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final double @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final double @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final double @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final double @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write Byte">

        public Event write(@NonNull Buffer buffer,
                           final @NonNull ByteBuffer data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final @NonNull ByteBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull ByteBuffer data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull ByteBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write Short">

        public Event write(@NonNull Buffer buffer,
                           final @NonNull ShortBuffer data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final @NonNull ShortBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull ShortBuffer data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull ShortBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final short @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final short @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final short @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final short @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Write Int">

        public Event write(@NonNull Buffer buffer,
                           final @NonNull IntBuffer data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final @NonNull IntBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull IntBuffer data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final @NonNull IntBuffer data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final int @NonNull [] data,
                           final long offset,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final long offset,
                           final int @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, offset, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final int @NonNull [] data,
                           final boolean blocking,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event write(@NonNull Buffer buffer,
                           final int @NonNull [] data,
                           final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferWrite(stack, buffer, data, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>

        //</editor-fold>

        //<editor-fold desc="Buffer Read">

        //<editor-fold desc="Buffer Read Float">

        public Event read(@NonNull Buffer buffer,
                          @NonNull FloatBuffer target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          @NonNull FloatBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull FloatBuffer target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull FloatBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          float @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          float @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          float @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          float @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read Double">

        public Event read(@NonNull Buffer buffer,
                          @NonNull DoubleBuffer target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          @NonNull DoubleBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull DoubleBuffer target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull DoubleBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          double @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          double @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          double @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          double @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read Byte">

        public Event read(@NonNull Buffer buffer,
                          @NonNull ByteBuffer target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          @NonNull ByteBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull ByteBuffer target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull ByteBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read Short">

        public Event read(@NonNull Buffer buffer,
                          @NonNull ShortBuffer target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          @NonNull ShortBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull ShortBuffer target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull ShortBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          short @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          short @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          short @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          short @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>
        //<editor-fold desc="Buffer Read Int">

        public Event read(@NonNull Buffer buffer,
                          @NonNull IntBuffer target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          @NonNull IntBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull IntBuffer target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          @NonNull IntBuffer target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          int @NonNull [] target,
                          final long offset,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, offset, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          final long offset,
                          int @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, offset, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          int @NonNull [] target,
                          final boolean blocking,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, blocking, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        public Event read(@NonNull Buffer buffer,
                          int @NonNull [] target,
                          final Event... dependencies) {
            ensureChainable();
            long[] dependencyIDs = dependencyIDs(dependencies);
            Event next;

            try {
                next = bufferRead(stack, buffer, target, dependencyIDs);
            } finally {
                releaseDependencies(dependencies);
            }

            return transferOwnership(next);
        }

        //</editor-fold>

        //</editor-fold>

        /**
         * Flushes the queue and ends this Event chain. If the stack was created
         * internally by CommandQueue, its active frame is closed here.
         * A caller-supplied MemoryStack is never closed by Event.
         */
        public void execute() {
            ensureChainable();

            try {
                CL10.clFlush(commandQueue);
            } finally {
                chainable = false;
                releaseOwnedStack();
            }
        }

        /**
         * Transfers ownership of the current chain stack to the next Event.
         */
        private Event transferOwnership(Event next) {
            Preconditions.checkNotNull(next);
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

        private void ensureChainable() {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
        }
    }
}

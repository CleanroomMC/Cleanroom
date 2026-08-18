package com.cleanroommc.cleanroom.compute.pipes;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.errors.PipeError;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;

import java.io.Closeable;
import java.io.IOException;

public class Pipe implements Closeable {

    public final long handle;
    public final int capacity;
    public final int packetSize;

    public Pipe(int capacity, PipeField @NonNull ... schema) {
        Preconditions.checkNotNull(schema);
        Preconditions.checkArgument(schema.length > 0);

        int size = 0;
        for (PipeField field : schema)
            size += field.sizeof;

        this(capacity, size);
    }

    public Pipe(int capacity, int packetSize) {
        Preconditions.checkState(Compute.instance().supportsPipes, "Pipes are not supported.");
        Preconditions.checkArgument(capacity > 0);
        Preconditions.checkArgument(packetSize > 0);

        this.capacity = capacity;
        this.packetSize = packetSize;

        int[] err = new int[1];

        this.handle = CL20.clCreatePipe(Compute.instance().context,
                CL20.CL_MEM_READ_WRITE | CL20.CL_MEM_HOST_NO_ACCESS,
                packetSize, capacity, null, err);

        switch(err[0]) {
            case CL20.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new PipeError("Failed to allocate pipe.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL pipe.");
        }

    }

    @Override
    public void close() throws IOException {
        CL20.clReleaseMemObject(handle);
    }
}

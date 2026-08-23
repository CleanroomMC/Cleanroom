package com.cleanroommc.compute.pipes;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.errors.PipeError;
import com.cleanroommc.compute.types.OpenCLType;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;

import java.io.IOException;

public class Pipe extends SmartPointer {

    public final long handle;
    public final int capacity;
    public final int packetSize;

    public Pipe(int capacity, OpenCLType @NonNull ... schema) {
        Preconditions.checkNotNull(schema);
        Preconditions.checkArgument(schema.length > 0);

        int size = 0;
        for (OpenCLType field : schema)
            size += field.sizeof();

        this(capacity, size);
    }

    public Pipe(int capacity, int packetSize) {
        super();
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
    public void close() {
        super.close();
        CL20.clReleaseMemObject(handle);
    }
}

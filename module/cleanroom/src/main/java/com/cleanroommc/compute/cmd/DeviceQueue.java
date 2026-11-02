package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.Device;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.compute.smrtptr.GarbageCollector;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.google.common.base.Preconditions;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.system.MemoryUtil;

import java.lang.ref.Cleaner;

/**
 * DeviceQueue: a command queue used on-device by CL Kernels.
 * @author EΣrie
 * @see com.cleanroommc.compute.kernels.params.CommandQueueParameter
 */
public class DeviceQueue extends SmartPointer {

    private final Cleaner.Cleanable cleanable;

    public final long commandQueue;
    public final long device;

    /**
     * Usually done with a method inside {@link CommandQueue} to ensure device integrity.
     * @see CommandQueue#getDeviceQueue()
     * @param device The device to create the queue on.
     */
    public DeviceQueue(Device device) {
        Preconditions.checkNotNull(device);
        Preconditions.checkState(device.preferredDeviceQueueSize() != -1 && device.maxDeviceQueueSize() != -1, "Device does not support device queues.");
        this.device = device.handle();

        long[] properties = new long[] {
                CL20.CL_QUEUE_PROPERTIES,
                CL20.CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE
                        | CL20.CL_QUEUE_ON_DEVICE
                        | CL20.CL_QUEUE_ON_DEVICE_DEFAULT,
                CL20.CL_QUEUE_SIZE,
                device.preferredDeviceQueueSize(),
                MemoryUtil.NULL
        };
        int[] err = new int[1];
        final long handle = CL20.clCreateCommandQueueWithProperties(Compute.instance().context, device.handle(), properties, err);
        switch(err[0]) {
            case CL10.CL_INVALID_CONTEXT -> throw new RuntimeException("Invalid OpenCL context.");
            case CL10.CL_INVALID_DEVICE -> throw new UnavaliableDeviceError("Out of context device provided to command queue.");
            case CL10.CL_INVALID_VALUE -> throw new RuntimeException("Invalid value provided as command queue properties.");
            case CL10.CL_INVALID_QUEUE_PROPERTIES -> throw new RuntimeException("Queue properties unsupported by device");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL command queue.");
        }
        this.commandQueue = handle;
        this.cleanable = GarbageCollector.INSTANCE.cleaner.register(this,
                GarbageCollector.INSTANCE.deletionTask(() -> CL20.clReleaseCommandQueue(handle))
        );
    }

    @Override
    public void close() {
        if (isClosed())
            return;
        super.close();
        cleanable.clean();
    }
}

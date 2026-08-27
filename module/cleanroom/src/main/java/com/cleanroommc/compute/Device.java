package com.cleanroommc.compute;

import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CLCapabilities;

/**
 * [<i>Internal Class</i>] OpenCL Device
 * @param handle the handle to the device
 * @param capabilities the capabilities of the device
 * @param maxWorkItemSizes the maximum work item sizes supported by the device
 * @param supportsImages whether the device supports images
 * @param supportsMipmaps whether the device supports mipmaps
 * @param supportsPipes whether the device supports pipes
 * @author EΣrie
 */
public record Device(long handle,
                     CLCapabilities capabilities,
                     long[] maxWorkItemSizes,
                     boolean supportsImages,
                     boolean supportsMipmaps,
                     boolean supportsPipes) implements Comparable<Device> {
    @Override
    public int compareTo(@NonNull Device o) {
        return (int)(handle - o.handle);
    }
}

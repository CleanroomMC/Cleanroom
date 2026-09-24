package com.cleanroommc.compute;

import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CLCapabilities;

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

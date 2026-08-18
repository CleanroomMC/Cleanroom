package com.cleanroommc.cleanroom.compute;

import org.lwjgl.opencl.CLCapabilities;

public record Device(long handle,
                     CLCapabilities capabilities,
                     long[] maxWorkItemSizes,
                     boolean supportsImages,
                     boolean supportsMipmaps,
                     boolean supportsPipes) {
}

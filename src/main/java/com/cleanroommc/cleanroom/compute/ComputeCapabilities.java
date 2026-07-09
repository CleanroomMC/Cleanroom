package com.cleanroommc.cleanroom.compute;

import org.lwjgl.opencl.CLCapabilities;

public class ComputeCapabilities {
    static CLCapabilities PLATFORM_CAPABILITIES,
            DEVICE_CAPABILITIES;

    public static CLCapabilities getPlatformCapabilities() {
        return PLATFORM_CAPABILITIES;
    }

    public static CLCapabilities getDeviceCapabilities() {
        return DEVICE_CAPABILITIES;
    }
}

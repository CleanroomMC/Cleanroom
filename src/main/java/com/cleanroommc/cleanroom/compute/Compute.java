package com.cleanroommc.cleanroom.compute;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opencl.CLCapabilities;

public class Compute {
    static CLCapabilities PLATFORM_CAPABILITIES,
            DEVICE_CAPABILITIES;

    static Logger LOGGER;

    public static CLCapabilities getPlatformCapabilities() {
        return PLATFORM_CAPABILITIES;
    }

    public static CLCapabilities getDeviceCapabilities() {
        return DEVICE_CAPABILITIES;
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}

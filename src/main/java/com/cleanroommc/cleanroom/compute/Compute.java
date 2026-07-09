package com.cleanroommc.cleanroom.compute;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opencl.CLCapabilities;

public class Compute {

    private static Compute INSTANCE;

    public final CLCapabilities PLATFORM_CAPABILITIES,
            DEVICE_CAPABILITIES;

    public final Logger LOGGER;

    public final long context;

    private Compute(Logger log, CLCapabilities platform, CLCapabilities device, long context) {
        this.LOGGER = log;
        this.PLATFORM_CAPABILITIES = platform;
        this.DEVICE_CAPABILITIES = device;
        this.context = context;
    }

    public static Compute instance() {
        return INSTANCE;
    }

    static void init(Logger log, CLCapabilities platform, CLCapabilities device, long context) {
        INSTANCE = new Compute(log, platform, device, context);
    }
}

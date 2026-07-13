package com.cleanroommc.cleanroom.compute;

import net.lenni0451.reflect.exceptions.ConstructorInvocationException;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opencl.CLCapabilities;

public class Compute {

    private static Compute INSTANCE = null;

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
        if (INSTANCE != null) {
            throw new ConstructorInvocationException("Second attempt at invoking singleton constructor. ");
        }
        INSTANCE = new Compute(log, platform, device, context);
    }
}

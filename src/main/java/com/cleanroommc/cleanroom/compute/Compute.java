package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.cmd.CommandQueueDispatch;
import com.cleanroommc.cleanroom.compute.programs.ComputeProgram;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.lenni0451.reflect.exceptions.ConstructorInvocationException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Map;

public class Compute {

    private static Compute INSTANCE = null;

    public final CLCapabilities PLATFORM_CAPABILITIES,
            DEVICE_CAPABILITIES;

    public final Logger LOGGER;

    public final long context;
    public final long[] devices;
    public final Map<ResourceLocation, Long> libraries = new Object2ObjectAVLTreeMap<>();
    public final Map<ResourceLocation, ComputeProgram> programs = new Object2ObjectAVLTreeMap<>();
    public final CommandQueueDispatch queueDispatch = new CommandQueueDispatch();

    private Compute(Logger log, CLCapabilities platform, CLCapabilities device, long context, long... devices) {
        this.LOGGER = log;
        this.PLATFORM_CAPABILITIES = platform;
        this.DEVICE_CAPABILITIES = device;
        this.context = context;
        this.devices = devices;
    }

    public static Compute instance() {
        return INSTANCE;
    }

    public long getOrCreateLibrary(ResourceLocation rl, MemoryStack stack) {
        if (libraries.containsKey(rl)) {
            return libraries.get(rl);
        }
        String src = MinecraftResourceUtils.readText(new ResourceLocation(rl.getNamespace(),
                        "compute/" + rl.getPath()),
                MinecraftResourceUtils.NewLineType.BACK_SLASH_N);
        IntBuffer err_code = stack.mallocInt(1);
        long program = CL10.clCreateProgramWithSource(Compute.instance().context, src, err_code);
        switch(err_code.get(0)) {
            case CL10.CL_INVALID_VALUE -> throw new NullPointerException(String.format("Source code of %s is null. ", rl));
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL program.");
        }
        libraries.put(rl, program);
        return program;
    }

    static void init(Logger log, CLCapabilities platform, CLCapabilities device, long context, long... devices) {
        if (INSTANCE != null) {
            throw new ConstructorInvocationException("Second attempt at invoking singleton constructor. ");
        }
        INSTANCE = new Compute(log, platform, device, context, devices);
    }
}

package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.cmd.CommandQueueDispatch;
import com.cleanroommc.cleanroom.compute.programs.ComputeProgram;
import com.cleanroommc.cleanroom.compute.programs.ProgramCacheIntegrityTable;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import net.lenni0451.reflect.exceptions.ConstructorInvocationException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;

public class Compute {

    private static Compute INSTANCE = null;

    public final CLCapabilities PLATFORM_CAPABILITIES;

    public final Logger LOGGER;

    public final long context;
    public final Device[] devices;
    public final Map<ResourceLocation, Long> libraries = new Object2ObjectAVLTreeMap<>();
    public final Map<ResourceLocation, ComputeProgram> programs = new Object2ObjectAVLTreeMap<>();
    public final CommandQueueDispatch queueDispatch = new CommandQueueDispatch();

    public final boolean supportsImages;
    public final boolean supportsMipmaps;
    public final boolean supportsPipes;

    private final ProgramCacheIntegrityTable programCacheIntegrityTable = new ProgramCacheIntegrityTable();

    private Compute(Logger log, CLCapabilities platformCapabilities, long context, Device[] devices) {
        this.LOGGER = log;
        this.PLATFORM_CAPABILITIES = platformCapabilities;
        this.context = context;
        this.devices = devices;

        Arrays.sort(devices);

        boolean supportsImages = false;
        boolean supportsMipmaps = false;
        boolean supportsPipes = false;

        for (Device device : devices) {
            if (device.supportsImages()) supportsImages = true;
            if (device.supportsMipmaps()) supportsMipmaps = true;
            if (device.supportsPipes()) supportsPipes = true;
            if (supportsImages && supportsMipmaps && supportsPipes)
                break;
        }

        this.supportsImages = supportsImages;
        this.supportsMipmaps = supportsMipmaps;
        this.supportsPipes = supportsPipes;
    }

    public static Compute instance() {
        return INSTANCE;
    }

    public void registerProgram(ResourceLocation location) {
        programs.put(location, new ComputeProgram(location));
    }

    void compilePrograms() {
        for (ComputeProgram program : programs.values()) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                program.compile(programCacheIntegrityTable, stack);
            }
        }
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

    public Device getDevice(long handle) {
        if (devices[0].handle() == handle)
            return devices[0];
        int div = 2;
        int idx = Math.clamp(devices.length / div, 1, devices.length - 1);
        do {
            if (devices[idx].handle() == handle)
                return devices[idx];
            div += 2;
            if (devices[idx].handle() < handle)
                idx = Math.clamp(idx - (devices.length / div), 1, devices.length - 1);
            else
                idx = Math.clamp(idx + (devices.length / div), 1, devices.length - 1);
        } while (div <= devices.length);
        throw new IllegalArgumentException("Device not present.");
    }

    static void init(Logger log, CLCapabilities platform, long context, Device... devices) {
        if (INSTANCE != null) {
            throw new ConstructorInvocationException("Second attempt at invoking singleton constructor. ");
        }
        INSTANCE = new Compute(log, platform, context, devices);
    }

    public static boolean isAvailable() {
        return INSTANCE != null;
    }
}

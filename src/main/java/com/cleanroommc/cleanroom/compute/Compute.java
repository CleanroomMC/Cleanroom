package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.cmd.CommandQueueDispatch;
import com.cleanroommc.cleanroom.compute.programs.ComputeProgram;
import com.cleanroommc.cleanroom.compute.programs.ProgramCacheIntegrityTable;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.lenni0451.reflect.exceptions.ConstructorInvocationException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class Compute {

    private static Compute INSTANCE = null;

    public final CLCapabilities PLATFORM_CAPABILITIES;

    public final Logger LOGGER;

    public final long context;
    public final Device[] devices;
    public final MutableGraph<ResourceLocation> dependencyGraph = GraphBuilder.directed().build();
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

    public Long[] getOrCreateLibraries(ResourceLocation rl, MemoryStack stack) {
        Set<Long> found = new LongArraySet();
        if (libraries.containsKey(rl)) {
            Traverser<ResourceLocation> dependencies = Traverser.forGraph(dependencyGraph);
            dependencies.breadthFirst(rl).forEach(dep -> found.add(libraries.get(dep)));
            found.add(libraries.get(rl));
            return found.toArray(new Long[0]);
        }
        Stack<ResourceLocation> browsable = new ReferenceArrayList<>();
        browsable.push(rl);
        while (!browsable.isEmpty()) {
            ResourceLocation curr = browsable.pop();
            String src = MinecraftResourceUtils.readText(new ResourceLocation(curr.getNamespace(),
                            "compute/" + curr.getPath()),
                    MinecraftResourceUtils.NewLineType.BACK_SLASH_N);
            IntBuffer err_code = stack.mallocInt(1);
            long program = CL10.clCreateProgramWithSource(Compute.instance().context, src, err_code);
            switch(err_code.get(0)) {
                case CL10.CL_INVALID_VALUE -> throw new NullPointerException(String.format("Source code of %s is null. ", curr));
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL program.");
            }
            libraries.put(curr, program);
            dependencyGraph.addNode(curr);
            Set<ResourceLocation> dependencies = ComputeProgram.getHeadersFromFile(src, curr);
            for (ResourceLocation dep : dependencies) {
                browsable.push(dep);
                dependencyGraph.putEdge(curr, dep);
            }
            found.add(program);
        }
        return found.toArray(new Long[0]);
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

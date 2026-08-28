package com.cleanroommc.compute;

import com.cleanroommc.compute.cmd.CommandQueueDispatch;
import com.cleanroommc.compute.programs.ComputeProgram;
import com.cleanroommc.compute.programs.ProgramCacheIntegrityTable;
import com.cleanroommc.kirino.utils.MinecraftResourceUtils;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.Traverser;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

/**
 * Hub of the API.
 * @author EΣrie
 * @apiNote Singleton.
 */
public class Compute {

    private static @Nullable Compute INSTANCE = null;

    /**
     * Platform capabilities.
     */
    public final CLCapabilities PLATFORM_CAPABILITIES;

    /**
     * Logging.
     */
    public final Logger LOGGER;

    /**
     * The OpenCL context.
     */
    public final long context;
    /**
     * All devices available to the platform.
     */
    public final Device[] devices;
    /**
     * How all the headers depend on eachother.
     */
    public final MutableGraph<ResourceLocation> dependencyGraph = GraphBuilder.directed().build();
    /**
     * Cache of all the headers.
     */
    public final Map<ResourceLocation, Long> libraries = new Object2ObjectAVLTreeMap<>();
    /**
     * All programs.
     */
    public final Map<ResourceLocation, ComputeProgram> programs = new Object2ObjectAVLTreeMap<>();
    /**
     * Used for dispatching queues.
     */
    public final CommandQueueDispatch queueDispatch = new CommandQueueDispatch();

    /**
     * Is there a device that supports images?
     */
    public final boolean supportsImages;
    /**
     * Is there a device that supports mipmaps?
     */
    public final boolean supportsMipmaps;
    /**
     * Is there a device that supports pipes?
     */
    public final boolean supportsPipes;

    /**
     * Is GL sharing supported?
     */
    public final boolean glSharing;

    /**
     * Unused
     */
    private final ProgramCacheIntegrityTable programCacheIntegrityTable = new ProgramCacheIntegrityTable();

    /**
     * Initialize Compute.
     * @param log logger
     * @param platformCapabilities platform capabilities
     * @param context OpenCL context
     * @param devices all devices
     * @param isClient is this on the client (if yes enable gl sharing)
     * @author EΣrie
     */
    private Compute(Logger log, CLCapabilities platformCapabilities, long context, Device[] devices, boolean isClient) {
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

        this.glSharing = isClient;
    }

    /**
     * @return Singleton instance.
     * @author EΣrie
     */
    @SuppressWarnings("DataFlowIssue")
    public static Compute instance() {
        return INSTANCE;
    }

    /**
     * Register a program for compilation.
     * @param location Where is the program in the "assets/modid/compute/" subfolder?
     * @author EΣrie
     */
    public void registerProgram(ResourceLocation location) {
        programs.put(location, new ComputeProgram(location));
    }

    /**
     * Compile all programs.
     * @author EΣrie
     */
    void compilePrograms() {
        for (ComputeProgram program : programs.values()) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                program.compile(programCacheIntegrityTable, stack);
            }
        }
    }

    /**
     * Reads headers.
     * @param rl start header
     * @param stack MemoryStack for temporary variables.
     * @return OpenCL handles to all headers.
     * @apiNote This caches headers, then puts them into a dependency graph to account for headers that include headers.
     * @author EΣrie
     */
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

    /**
     * Get a device by its handle.
     * @param handle device handle
     * @return {@link Device}
     * @author EΣrie
     */
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

    /**
     * Initialize Compute.
     * @param log Logger
     * @param platform Platform capabilities
     * @param context OpenCL context
     * @param isClient Is this on the client?
     * @param devices All devices
     * @apiNote Initialise the singleton.
     * @author EΣrie
     */
    static void init(Logger log, CLCapabilities platform, long context, boolean isClient, Device... devices) {
        if (INSTANCE != null) {
            throw new RuntimeException("Second attempt at invoking singleton constructor. ");
        }
        INSTANCE = new Compute(log, platform, context, devices, isClient);
    }

    /**
     * @return Is OpenCL available
     * @author EΣrie
     */
    public static boolean isAvailable() {
        return INSTANCE != null;
    }
}

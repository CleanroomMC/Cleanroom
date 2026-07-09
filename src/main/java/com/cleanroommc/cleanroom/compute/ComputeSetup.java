package com.cleanroommc.cleanroom.compute;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CLCapabilities;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

public class ComputeSetup {
    public static void initOpenCL(Logger LOGGER) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            CL.create();
            IntBuffer numPlatforms = stack.mallocInt(1);
            PointerBuffer platforms;
            CL10.clGetPlatformIDs(null, numPlatforms);
            platforms = stack.mallocPointer(numPlatforms.get(0));
            CL10.clGetPlatformIDs(platforms, numPlatforms);
            PriorityQueue<Platform> platformSort = new ObjectHeapPriorityQueue<>(numPlatforms.get(0));
            for (int i = 0; i < numPlatforms.get(0); i++) {
                String name = getPlatformAttribute(CL10.CL_PLATFORM_NAME, platforms.get(i), stack);
                String version = getPlatformAttribute(CL10.CL_PLATFORM_VERSION, platforms.get(i), stack);
                platformSort.enqueue(new Platform(platforms.get(i),
                        name,
                        Integer.parseInt(String.valueOf(version.charAt(7))),
                        Integer.parseInt(String.valueOf(version.charAt(9))),
                        name.contains("CUDA") || version.contains("CUDA")
                ));
            }
            Platform platform = platformSort.first();
            LOGGER.info("Selected OpenCL platform: {}", platform.name);
            ComputeCapabilities.PLATFORM_CAPABILITIES = CL.createPlatformCapabilities(platform.pointer);
            PointerBuffer device = stack.mallocPointer(1);
            CL10.clGetDeviceIDs(platform.pointer, CL10.CL_DEVICE_TYPE_DEFAULT, device, new int[]{1});
            ComputeCapabilities.DEVICE_CAPABILITIES = CL.createDeviceCapabilities(device.get(0), ComputeCapabilities.PLATFORM_CAPABILITIES);
            //attributeSize.free();
            //platforms.free();
            Runtime.getRuntime().addShutdownHook(new Thread(CL::destroy));
        }
    }

    private static @NonNull String getPlatformAttribute(int attribute, long platform, @NonNull MemoryStack stack) {
        PointerBuffer attributeSize = stack.mallocPointer(1);
        CL10.clGetPlatformInfo(platform, attribute, (ByteBuffer) null, attributeSize);
        ByteBuffer info = stack.malloc((int) attributeSize.get(0));
        CL10.clGetPlatformInfo(platform, attribute, info, attributeSize);
        return StandardCharsets.US_ASCII.decode(info).toString();
    }

    private record Platform(long pointer, String name, int versionMajor, int versionMinor, boolean cuda) implements Comparable<Platform> {

        @Override
        public int compareTo(ComputeSetup.@NonNull Platform o) {
            return ((versionMajor*10) + versionMinor + (cuda ? 1 : 0)) - ((o.versionMajor*10) + o.versionMinor + (o.cuda ? 1 : 0));
        }
    }
}

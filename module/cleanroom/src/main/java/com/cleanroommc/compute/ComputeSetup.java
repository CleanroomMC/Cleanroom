package com.cleanroommc.compute;

import com.cleanroommc.compute.errors.InvalidPlatformError;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.lwjgl.LWJGLException;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;
import org.lwjgl.opengl.CGL;
import org.lwjgl.opengl.GLX;
import org.lwjgl.opengl.GLX12;
import org.lwjgl.opengl.WGL;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

public class ComputeSetup {
    public static void initOpenCL(Logger LOGGER, boolean client) throws LWJGLException {
        LOGGER.info("Initializing OpenCL");
        try (MemoryStack stack = MemoryStack.stackPush()) {
            CL.create();
            LOGGER.info("Selecting OpenCL Platform");
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
            CLCapabilities platformCapabilities = CL.createPlatformCapabilities(platform.pointer);
            LOGGER.info("Selected OpenCL platform: {}", platform.name);
            LOGGER.info("Selecting OpenCL Devices");
            PointerBuffer devices;
            if (platform.cuda) {
                IntBuffer deviceCount = stack.mallocInt(1);
                CL10.clGetDeviceIDs(platform.pointer, CL10.CL_DEVICE_TYPE_GPU, null, deviceCount);
                devices = stack.mallocPointer(deviceCount.get(0));
                deviceCount.rewind();
                CL10.clGetDeviceIDs(platform.pointer, CL10.CL_DEVICE_TYPE_GPU, devices, deviceCount);
            } else {
                devices = stack.mallocPointer(1);
                CL10.clGetDeviceIDs(platform.pointer, CL10.CL_DEVICE_TYPE_DEFAULT, devices, new int[]{1});
            }
            devices.rewind();
            LOGGER.info("Creating OpenCL Context");
            IntBuffer erret = stack.mallocInt(1);
            PointerBuffer properties = stack.mallocPointer(3 + (client ? 4 : 0));
            properties.put(CL10.CL_CONTEXT_PLATFORM);
            properties.put(platform.pointer);
            if (client) {
                LOGGER.info("Attaching OpenGL Context");
                appendGLContexts(properties);
            }
            properties.put(0);
            properties.rewind();
            final long ctx = CL10.clCreateContext(properties, devices, null, 0, erret);
            if (ctx < 0) {
                switch(erret.get(0)) {
                    case CL10.CL_INVALID_PLATFORM -> throw new InvalidPlatformError(String.format("Platform %s is invalid.", platform.name));
                    case CL10.CL_DEVICE_NOT_AVAILABLE -> throw new UnavaliableDeviceError("Device is not available.");
                    case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL context.");
                }
            }
            Device[] deviceArray = new Device[devices.capacity()];
            for (int i = 0; i < deviceArray.length; i++) {
                long deviceHandle = devices.get(i);
                CLCapabilities deviceCapabilities = CL.createDeviceCapabilities(deviceHandle, platformCapabilities);

                IntBuffer imageSupport = stack.mallocInt(1);
                CL10.clGetDeviceInfo(deviceHandle, CL10.CL_DEVICE_IMAGE_SUPPORT, imageSupport, (PointerBuffer) null);

                PointerBuffer len = stack.mallocPointer(1);
                CL10.clGetDeviceInfo(deviceHandle, CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES, (PointerBuffer) null, len);
                long[] maxWorkItemSizes = new long[(int) len.get(0)];
                CL10.clGetDeviceInfo(deviceHandle, CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES, maxWorkItemSizes, len);

                deviceArray[i] = new Device(deviceHandle,
                        deviceCapabilities,
                        maxWorkItemSizes,
                        imageSupport.get(0) != 0,
                        deviceCapabilities.cl_khr_mipmap_image,
                        deviceCapabilities.OpenCL20);
            }
            Compute.init(
                    LOGGER,
                    platformCapabilities,
                    ctx, client,
                    deviceArray
            );
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                CL10.clReleaseContext(ctx);
                CL.destroy();
            }));
        }
        LOGGER.info("OpenCL Setup completed successfully");
    }

    private static @NonNull String getPlatformAttribute(int attribute, long platform, @NonNull MemoryStack stack) {
        PointerBuffer attributeSize = stack.mallocPointer(1);
        CL10.clGetPlatformInfo(platform, attribute, (ByteBuffer) null, attributeSize);
        ByteBuffer info = stack.malloc((int) attributeSize.get(0));
        CL10.clGetPlatformInfo(platform, attribute, info, attributeSize);
        return StandardCharsets.US_ASCII.decode(info).toString();
    }

    @SideOnly(Side.CLIENT)
    private static void appendGLContexts(PointerBuffer properties) {
        long ctx;
        long display;
        switch (org.lwjgl.system.Platform.get()) {
            case WINDOWS:
                ctx = WGL.wglGetCurrentContext(null);
                if (ctx == 0) {
                    throw new IllegalStateException("Can't acquire WGL context.");
                }
                display = WGL.wglGetCurrentDC();
                if (display == 0) {
                    throw new IllegalStateException("Can't acquire WGL display.");
                }
                properties.put(KHRGLSharing.CL_GL_CONTEXT_KHR);
                properties.put(ctx);
                properties.put(KHRGLSharing.CL_WGL_HDC_KHR);
                properties.put(display);
                return;
            case LINUX, FREEBSD:
                ctx = GLX.glXGetCurrentContext();
                if (ctx == 0) {
                    throw new IllegalStateException("Can't acquire GLX context.");
                }
                display = GLX12.glXGetCurrentDisplay();
                if (display == 0) {
                    throw new IllegalStateException("Can't acquire GLX display.");
                }
                properties.put(KHRGLSharing.CL_GL_CONTEXT_KHR);
                properties.put(ctx);
                properties.put(KHRGLSharing.CL_GLX_DISPLAY_KHR);
                properties.put(display);
                return;
            case MACOSX:
                ctx = CGL.CGLGetCurrentContext();
                if (ctx == 0) {
                    throw new IllegalStateException("Can't acquire GLX context.");
                }
                display = CGL.CGLGetShareGroup(ctx);
                if (display == 0) {
                    throw new IllegalStateException("Can't acquire GLX share group.");
                }
                properties.put(APPLEGLSharing.CL_CONTEXT_PROPERTY_USE_CGL_SHAREGROUP_APPLE);
                properties.put(display);
        }
    }

    private record Platform(long pointer, String name, int versionMajor, int versionMinor, boolean cuda) implements Comparable<Platform> {

        @Override
        public int compareTo(ComputeSetup.@NonNull Platform o) {
            return ((versionMajor*10) + versionMinor + (cuda ? 1 : 0)) - ((o.versionMajor*10) + o.versionMinor + (o.cuda ? 1 : 0));
        }
    }
}

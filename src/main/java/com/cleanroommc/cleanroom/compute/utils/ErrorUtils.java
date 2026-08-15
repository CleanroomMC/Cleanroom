package com.cleanroommc.cleanroom.compute.utils;

import com.cleanroommc.cleanroom.compute.errors.ImageError;
import com.cleanroommc.cleanroom.compute.errors.KernelError;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL10GL;
import org.lwjgl.opencl.CL12;


public record ErrorUtils() {
    public static <T> void handleKernelParamError(int err, int index, T value) {
        switch (err) {
            case CL10.CL_INVALID_KERNEL -> throw new KernelError("Invalid kernel.");
            case CL10.CL_INVALID_ARG_INDEX -> throw new KernelError(String.format("Invalid kernel argument index %d", index));
            case CL10.CL_INVALID_ARG_SIZE -> throw new IllegalArgumentException(String.format("Invalid size of argument %d=%s", index, value));
            case CL10.CL_INVALID_ARG_VALUE -> throw new KernelError(String.format("Invalid kernel argument value %s.", value));
            case CL10.CL_INVALID_MEM_OBJECT -> throw new KernelError("Invalid memory object for argument.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources to set kernel parameter.");
        }
    }

    public static void handleEnqueueFillImageError(int err) {
        switch (err) {
            case CL10.CL_INVALID_MEM_OBJECT -> throw new ImageError("Image is not a valid memory object.");
            case CL10GL.CL_INVALID_MIP_LEVEL -> throw new ImageError("Provided mipmap level is invalid.");
            case CL10.CL_INVALID_EVENT_WAIT_LIST -> throw new ImageError("One or more events provided as dependencies is invalid.");
            case CL12.CL_INVALID_IMAGE_SIZE -> throw new ImageError("Image size exceeds the maximum size of the device.");
            case CL12.CL_IMAGE_FORMAT_NOT_SUPPORTED -> throw new ImageError("Image format unsupported by device.");
            case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new ImageError("Failed to allocate memory for image fill operation");
            case CL10.CL_INVALID_OPERATION -> throw new ImageError("Device associated with command queue does not support images.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources to fill image.");
        }
    }
    public static void handleEnqueueCopyImageError(int err) {
        switch (err) {
            case CL10.CL_INVALID_MEM_OBJECT -> throw new ImageError("Image is not a valid memory object.");
            case CL10GL.CL_INVALID_MIP_LEVEL -> throw new ImageError("Provided mipmap level is invalid.");
            case CL10.CL_INVALID_EVENT_WAIT_LIST -> throw new ImageError("One or more events provided as dependencies is invalid.");
            case CL12.CL_INVALID_IMAGE_SIZE -> throw new ImageError("Image size exceeds the maximum size of the device.");
            case CL12.CL_IMAGE_FORMAT_NOT_SUPPORTED -> throw new ImageError("Image format unsupported by device.");
            case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new ImageError("Failed to allocate memory for image fill operation");
            case CL10.CL_INVALID_OPERATION -> throw new ImageError("Device associated with command queue does not support images.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources to fill image.");
            case CL12.CL_MEM_COPY_OVERLAP -> throw new ImageError("Copy regions overlapping.");
        }
    }
}

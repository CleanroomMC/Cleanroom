package com.cleanroommc.compute.images.samplers;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.errors.ImageError;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.KHRMipmapImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.Closeable;
import java.io.IOException;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

public record Sampler(boolean normalized,
                      @NonNull AddressingMode addressingMode,
                      @NonNull FilteringMode filteringMode,
                      @Nullable FilteringMode mipmapFilteringMode,
                      float levelOfDetailMinimum,
                      float levelOfDetailMaximum,
                      long handle) implements Closeable {

    public Sampler(boolean normalized,
                   @NonNull AddressingMode addressingMode,
                   @NonNull FilteringMode filteringMode,
                   @Nullable FilteringMode mipmapFilteringMode,
                   float levelOfDetailMinimum,
                   float levelOfDetailMaximum) {
        this(normalized, addressingMode, filteringMode, mipmapFilteringMode, levelOfDetailMinimum, levelOfDetailMaximum, createSampler(
                normalized, addressingMode, filteringMode, mipmapFilteringMode, levelOfDetailMinimum, levelOfDetailMaximum
        ));
    }

    @Override
    public void close() throws IOException {
        CL20.clReleaseSampler(handle);
    }

    private static long createSampler(boolean normalized,
                               @NonNull AddressingMode addressingMode,
                               @NonNull FilteringMode filteringMode,
                               @Nullable FilteringMode mipmapFilteringMode,
                               float levelOfDetailMinimum,
                               float levelOfDetailMaximum) {
        try (MemoryStack stack = MemoryStack.create(); MemoryStack substack = stack.push()) {
            LongBuffer buf = substack.callocLong(mipmapFilteringMode != null ? 6 : 3);
            buf.put(CL20.CL_SAMPLER_NORMALIZED_COORDS).put(normalized ? CL10.CL_TRUE : CL10.CL_FALSE);
            buf.put(CL20.CL_SAMPLER_ADDRESSING_MODE).put(addressingMode.addressingMode);
            buf.put(CL20.CL_SAMPLER_FILTER_MODE).put(filteringMode.filteringMode);
            if (mipmapFilteringMode != null) {
                buf.put(KHRMipmapImage.CL_SAMPLER_MIP_FILTER_MODE_KHR).put(mipmapFilteringMode.filteringMode);
                buf.put(KHRMipmapImage.CL_SAMPLER_LOD_MIN_KHR).put(Float.floatToRawIntBits(levelOfDetailMinimum));
                buf.put(KHRMipmapImage.CL_SAMPLER_LOD_MAX_KHR).put(Float.floatToRawIntBits(levelOfDetailMaximum));
            }
            buf.rewind();
            IntBuffer err = substack.mallocInt(1);
            long res = CL20.clCreateSamplerWithProperties(Compute.instance().context, buf, err);
            switch (err.get(0)) {
                case CL10.CL_INVALID_VALUE -> throw new ImageError("Unsupported sampler property.");
                case CL10.CL_INVALID_OPERATION -> throw new ImageError("Images not supported by any device.");
                case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources to create sampler.");
            }
            return res;
        }
    }
}

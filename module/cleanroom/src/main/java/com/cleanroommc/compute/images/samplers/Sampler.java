package com.cleanroommc.compute.images.samplers;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.errors.ImageError;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.KHRMipmapImage;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

public final class Sampler extends SmartPointer {

    public final boolean normalized;
    public final @NonNull AddressingMode addressingMode;
    public final @NonNull FilteringMode filteringMode;
    public final @Nullable FilteringMode mipmapFilteringMode;
    public final float levelOfDetailMinimum;
    public final float levelOfDetailMaximum;
    public final long handle;

    public Sampler(boolean normalized,
                   @NonNull AddressingMode addressingMode,
                   @NonNull FilteringMode filteringMode,
                   @Nullable FilteringMode mipmapFilteringMode,
                   float levelOfDetailMinimum,
                   float levelOfDetailMaximum) {
        super();
        this.normalized = normalized;
        this.addressingMode = addressingMode;
        this.filteringMode = filteringMode;
        this.mipmapFilteringMode = mipmapFilteringMode;
        this.levelOfDetailMinimum = levelOfDetailMinimum;
        this.levelOfDetailMaximum = levelOfDetailMaximum;
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
            this.handle = res;
        }
    }

    @Override
    public void close() {
        super.close();
        CL20.clReleaseSampler(handle);
    }
}

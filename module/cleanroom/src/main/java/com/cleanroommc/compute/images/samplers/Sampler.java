package com.cleanroommc.compute.images.samplers;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.errors.ImageError;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL20;
import org.lwjgl.opencl.KHRMipmapImage;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * OpenCL sampler.
 * @apiNote I don't know why you would use that. There might be a reason, but I don't know it.
 * Unless you really need to use samplers created on the host side, you should create them directly in CL code.
 * @author EΣrie
 */
public final class Sampler extends SmartPointer {

    /**
     * Is coordinate [0;0] in the centre.
     */
    public final boolean normalized;
    /**
     * {@link AddressingMode} of the sampled image.
     */
    public final @NonNull AddressingMode addressingMode;
    /**
     * {@link FilteringMode} of the sampled image.
     */
    public final @NonNull FilteringMode filteringMode;
    /**
     * {@link FilteringMode} of the mipmaps of the sampled image.
     * @apiNote If sampling the mipmaps is not necessary, set to null.
     */
    public final @Nullable FilteringMode mipmapFilteringMode;
    /**
     * Minimum LOD.
     * @apiNote If sampling the mipmaps is not necessary, this stays unused, set it to something like 0 or -1.
     */
    public final float levelOfDetailMinimum;
    /**
     * Maximum LOD.
     * @apiNote If sampling the mipmaps is not necessary, this stays unused, set it to something like 0 or -1.
     */
    public final float levelOfDetailMaximum;
    /**
     * OpenCL sampler handle.
     */
    public final long handle;

    /**
     * <p>Create a sampler.</p>
     * <p>For more details on the parameters, look to the documentation of the variables.</p>
     * @param normalized Is coordinate [0;0] in the centre.
     * @param addressingMode {@link AddressingMode} of the sampled image.
     * @param filteringMode {@link FilteringMode} of the sampled image.
     * @param mipmapFilteringMode {@link FilteringMode} of the mipmaps of the sampled image.
     *                                                 See: {@link #mipmapFilteringMode}.
     * @param levelOfDetailMinimum Minimum LOD. See: {@link #levelOfDetailMinimum}.
     * @param levelOfDetailMaximum Maximum LOD. See: {@link #levelOfDetailMaximum}.
     * @author EΣrie
     */
    public Sampler(boolean normalized,
                   @NonNull AddressingMode addressingMode,
                   @NonNull FilteringMode filteringMode,
                   @Nullable FilteringMode mipmapFilteringMode,
                   float levelOfDetailMinimum,
                   float levelOfDetailMaximum) {
        Preconditions.checkNotNull(filteringMode);
        Preconditions.checkNotNull(addressingMode);
        Preconditions.checkState(mipmapFilteringMode == null || levelOfDetailMinimum <= levelOfDetailMaximum);
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

    /**
     * Closes the sampler and releases memory.
     * @author EΣrie
     */
    @Override
    public void close() {
        super.close();
        CL20.clReleaseSampler(handle);
    }
}

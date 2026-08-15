package com.cleanroommc.cleanroom.compute.images;

import com.cleanroommc.cleanroom.compute.Compute;
import com.cleanroommc.cleanroom.compute.buffers.BufferFlags;
import com.cleanroommc.cleanroom.compute.errors.ImageError;
import com.google.common.base.Preconditions;
import org.joml.Vector2L;
import org.joml.Vector3L;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLImageDesc;
import org.lwjgl.opencl.CLImageFormat;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public sealed abstract class Image<CT> permits Image1D, Image2D, Image3D {

    public final long handle;
    protected final Vector3L size;
    public final int mipmaps;

    private Image(@NonNull MemoryStack stack,
                    @NonNull BufferFlags memoryFlags,
                    @NonNull CLImageFormat format,
                    @NonNull CLImageDesc descriptor,
                    @Nullable ByteBuffer hostMemory) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(memoryFlags);
        Preconditions.checkNotNull(format);
        Preconditions.checkNotNull(descriptor);

        this.size = new Vector3L((int) descriptor.image_width(),
                (int) descriptor.image_height(),
                (int) descriptor.image_depth()
        );
        this.mipmaps = descriptor.num_mip_levels();

        IntBuffer err = stack.mallocInt(1);
        this.handle = CL12.clCreateImage(
                Compute.instance().context,
                memoryFlags.flags | (hostMemory != null ? CL10.CL_MEM_COPY_HOST_PTR : 0),
                format,
                descriptor,
                hostMemory,
                err
        );
        switch (err.get(0)) {
            case CL12.CL_INVALID_IMAGE_FORMAT_DESCRIPTOR -> throw new ImageError(String.format("Image format descriptor CLImageFormat[order=%s, type=%s] is invalid.",
                    ChannelOrder.findFromOpenCL(format.image_channel_data_type()),
                    ChannelType.findFromOpenCL(format.image_channel_data_type()))
            );
            case CL12.CL_INVALID_IMAGE_DESCRIPTOR -> throw new ImageError(String.format("Image descriptor CLImageDesc[type=%s, width=%d, height=%d, depth=%d, array_size=%d, row_pitch=%d, slice_pitch=%d, mip_levels=%d, samples=%d] is invalid.",
                    imageTypeName(descriptor.image_type()),
                    descriptor.image_width(),
                    descriptor.image_height(),
                    descriptor.image_depth(),
                    descriptor.image_array_size(),
                    descriptor.image_row_pitch(),
                    descriptor.image_slice_pitch(),
                    descriptor.num_mip_levels(),
                    descriptor.num_samples()
            ));
            case CL10.CL_INVALID_VALUE -> throw new ImageError("Host memory pointer specified for image created from memory object.");
            case CL12.CL_INVALID_IMAGE_SIZE -> throw new ImageError("Image dimensions exceed maximum permitted dimensions of the device.");
            case CL12.CL_IMAGE_FORMAT_NOT_SUPPORTED -> throw new ImageError("Unsupported image format.");
            case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new ImageError("Failed to allocate memory for image.");
            case CL10.CL_INVALID_OPERATION -> throw new ImageError("None of the devices support images.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL image.");
        }
    }

    protected Image(@NonNull Workaround workaround) {
        this(workaround.stack(), workaround.memoryFlags(), workaround.format(), workaround.descriptor(), workaround.hostMemory());
    }

    public final long width() {
        return this.size.x;
    }

    public final long height() {
        return this.size.y;
    }

    public final long depth() {
        return this.size.z;
    }

    public abstract <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, long commandQueue, @NonNull B color, @NonNull CT from, @NonNull CT size, int mipmap, long... dependencies);
    public abstract long fill(@NonNull MemoryStack stack, long commandQueue, int @NonNull [] color, @NonNull CT from, @NonNull CT size, int mipmap, long... dependencies);
    public abstract long fill(@NonNull MemoryStack stack, long commandQueue, float @NonNull [] color, @NonNull CT from, @NonNull CT size, int mipmap, long... dependencies);
    public final <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, long commandQueue, @NonNull B color, @NonNull CT from, @NonNull CT size, long... dependencies) {
        return this.fill(stack, commandQueue, color, from, size, 0, dependencies);
    }
    public final long fill(@NonNull MemoryStack stack, long commandQueue, int @NonNull [] color, @NonNull CT from, @NonNull CT size, long... dependencies) {
        return this.fill(stack, commandQueue, color, from, size, 0, dependencies);
    }
    public final long fill(@NonNull MemoryStack stack, long commandQueue, float @NonNull [] color, @NonNull CT from, @NonNull CT size, long... dependencies) {
        return this.fill(stack, commandQueue, color, from, size, 0, dependencies);
    }

    public abstract <CT2> long copy(@NonNull MemoryStack stack, long commandQueue, @NonNull Image<CT2> destination,
                                    @NonNull CT from, int fromMipmap, @NonNull CT2 to, int toMipmap,
                                    @NonNull CT2 size, long... dependencies);

    public abstract <B extends java.nio.Buffer> long read(@NonNull MemoryStack stack, long commandQueue,
                                                          @NonNull CT from, int mipmap, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          boolean blocking, long... dependencies);
    public abstract long read(@NonNull MemoryStack stack, long commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, short @NonNull [] array,
                              boolean blocking, long... dependencies);
    public abstract long read(@NonNull MemoryStack stack, long commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, int @NonNull [] array,
                              boolean blocking, long... dependencies);
    public abstract long read(@NonNull MemoryStack stack, long commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, float @NonNull [] array,
                              boolean blocking, long... dependencies);
    public abstract long read(@NonNull MemoryStack stack, long commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, double @NonNull [] array,
                              boolean blocking, long... dependencies);
    public final <B extends java.nio.Buffer> long read(@NonNull MemoryStack stack, long commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, B buffer,
                                                       boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, buffer, blocking, dependencies);
    }
    public final long read(@NonNull MemoryStack stack, long commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, short @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    public final long read(@NonNull MemoryStack stack, long commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, int @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    public final long read(@NonNull MemoryStack stack, long commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, float @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    public final long read(@NonNull MemoryStack stack, long commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, double @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    public abstract <B extends java.nio.Buffer> long write(@NonNull MemoryStack stack, long commandQueue,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, @NonNull B buffer,
                                                           boolean blocking, long... dependencies);
    public abstract long write(@NonNull MemoryStack stack, long commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, short @NonNull [] array,
                               boolean blocking, long... dependencies);
    public abstract long write(@NonNull MemoryStack stack, long commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, int @NonNull [] array,
                               boolean blocking, long... dependencies);
    public abstract long write(@NonNull MemoryStack stack, long commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, float @NonNull [] array,
                               boolean blocking, long... dependencies);
    public abstract long write(@NonNull MemoryStack stack, long commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, double @NonNull [] array,
                               boolean blocking, long... dependencies);
    public final <B extends java.nio.Buffer> long write(@NonNull MemoryStack stack, long commandQueue,
                                                        @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, B buffer,
                                                       boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, buffer, blocking, dependencies);
    }
    public final long write(@NonNull MemoryStack stack, long commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, short @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    public final long write(@NonNull MemoryStack stack, long commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, int @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    public final long write(@NonNull MemoryStack stack, long commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, float @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    public final long write(@NonNull MemoryStack stack, long commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, double @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    protected static @NonNull String imageTypeName(final long val) {
        return switch((int) val) {
            case CL12.CL_MEM_OBJECT_IMAGE1D -> "image1d";
            case CL12.CL_MEM_OBJECT_IMAGE2D -> "image2d";
            case CL12.CL_MEM_OBJECT_IMAGE3D -> "image3d";
            case CL12.CL_MEM_OBJECT_IMAGE1D_BUFFER -> "image1d_buffer";
            case CL12.CL_MEM_OBJECT_IMAGE1D_ARRAY -> "image1d_array";
            case CL12.CL_MEM_OBJECT_IMAGE2D_ARRAY -> "image2d_array";
            default -> "invalid";
        };
    }

    protected static <CT2> @NonNull PointerBuffer getCoordinates(@NonNull MemoryStack stack, @NonNull CT2 vector, int mipmap) {
        return switch(vector) {
            case Long l -> stack.mallocPointer(3).put(l).put(mipmap).put(0).rewind();
            case Vector2L v2 -> stack.mallocPointer(3).put(v2.x).put(v2.y).put(mipmap).rewind();
            case Vector3L v3 -> {
                PointerBuffer buffer = stack.mallocPointer(mipmap == 0 ? 3 : 4).put(v3.x).put(v3.y).put(v3.z);
                if (mipmap != 0)
                    buffer.put(mipmap);
                yield buffer.rewind();
            }
            default -> throw new IllegalArgumentException("Provided parameter is not allowed.");
        };
    }

    protected static <CT2> @NonNull PointerBuffer getRegion(@NonNull MemoryStack stack, @NonNull CT2 region) {
        return switch(region) {
            case Long l -> stack.mallocPointer(3).put(l).put(1).put(1).rewind();
            case Vector2L v2 -> stack.mallocPointer(3).put(v2.x).put(v2.y).put(1).rewind();
            case Vector3L v3 -> stack.mallocPointer(3).put(v3.x).put(v3.y).put(v3.z);
            default -> throw new IllegalArgumentException("Provided parameter is not allowed.");
        };
    }

    protected record Workaround(@NonNull MemoryStack stack,
                              @NonNull BufferFlags memoryFlags,
                              @NonNull CLImageFormat format,
                              @NonNull CLImageDesc descriptor,
                              @Nullable ByteBuffer hostMemory) {}
}

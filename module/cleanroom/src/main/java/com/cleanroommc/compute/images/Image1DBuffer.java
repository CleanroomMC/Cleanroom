package com.cleanroommc.compute.images;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.cleanroom.compute.errors.ImageError;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLImageDesc;
import org.lwjgl.opencl.CLImageFormat;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

public final class Image1DBuffer extends Image1D {

    public final Buffer parent;

    public Image1DBuffer(@NonNull MemoryStack stack,
                         @NonNull BufferFlags memoryFlags,
                         final long size,
                         final Buffer buffer,
                         final @NonNull ChannelType channelType,
                         final @NonNull ChannelOrder channelOrder,
                         @Nullable ByteBuffer hostMemory) {
        super(workaround(stack, memoryFlags, size, buffer, channelType, channelOrder, hostMemory));
        this.parent = buffer;
    }

    @Override
    public <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, long commandQueue, @NonNull B color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.fill(stack, commandQueue, color, from, size, mipmap, dependencies);
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, int @NonNull [] color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.fill(stack, commandQueue, color, from, size, mipmap, dependencies);
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, float @NonNull [] color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.fill(stack, commandQueue, color, from, size, mipmap, dependencies);
    }

    @Override
    public <CT2> long copy(@NonNull MemoryStack stack, long commandQueue, @NonNull Image<CT2> destination, @NonNull Long from, int fromMipmap, @NonNull CT2 to, int toMipmap, @NonNull CT2 size, long... dependencies) {
        return super.copy(stack, commandQueue, destination, from, fromMipmap, to, toMipmap, size, dependencies);
    }

    @Override
    public <B extends java.nio.Buffer> long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies);
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public <B extends java.nio.Buffer> long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies);
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    private static Workaround workaround(@NonNull MemoryStack stack,
                                         @NonNull BufferFlags memoryFlags,
                                         final long size,
                                         final Buffer buffer,
                                         final @NonNull ChannelType channelType,
                                         final @NonNull ChannelOrder channelOrder,
                                         @Nullable ByteBuffer hostMemory) {
        try (MemoryStack substack = stack.push()) {
            ByteBuffer container = substack.calloc(CLImageFormat.SIZEOF + CLImageDesc.SIZEOF);
            try (CLImageFormat format = new CLImageFormat(container.slice(0, CLImageFormat.SIZEOF))) {
                try (CLImageDesc descriptor = new CLImageDesc(container.slice(CLImageFormat.SIZEOF, CLImageDesc.SIZEOF))) {
                    format.image_channel_data_type(channelType.type).image_channel_order(channelOrder.order);
                    descriptor.image_type(CL12.CL_MEM_OBJECT_IMAGE1D).image_width(size)
                            .image_row_pitch(hostMemory != null ? size * channelType.sizeof(channelOrder) : 0)
                            .mem_object(buffer.handle);
                    if (hostMemory != null && hostMemory.remaining() < descriptor.image_row_pitch())
                        throw new ImageError(String.format("Image size %d too large for host memory %d.",
                                descriptor.image_row_pitch(), hostMemory.remaining()));
                    return new Workaround(stack, memoryFlags, format, descriptor, hostMemory);
                }
            }
        }
    }
}

package com.cleanroommc.cleanroom.compute.images;

import com.cleanroommc.cleanroom.compute.buffers.BufferFlags;
import com.cleanroommc.cleanroom.compute.errors.ImageError;
import com.cleanroommc.cleanroom.compute.utils.ErrorUtils;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLImageDesc;
import org.lwjgl.opencl.CLImageFormat;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleEnqueueCopyImageError;
import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleEnqueueReadWriteImageError;

public final class Image1D extends Image<Long> {

    public Image1D(@NonNull MemoryStack stack,
                   @NonNull BufferFlags memoryFlags,
                   final long size,
                   final int mipMaps,
                   final @NonNull ChannelType channelType,
                   final @NonNull ChannelOrder channelOrder,
                   @Nullable ByteBuffer hostMemory) {
        super(workaround(stack, memoryFlags, size, mipMaps, channelType, channelOrder, hostMemory));
    }

    @Override
    public <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, long commandQueue, @NonNull B color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(color instanceof ByteBuffer
                                || color instanceof IntBuffer
                                || color instanceof FloatBuffer);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap , dependencies);
            ErrorUtils.handleEnqueueFillImageError(switch (color) {
                case ByteBuffer bb -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        bb,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null,
                        coordinates.slice(bufLen - 1, 1)
                );
                case IntBuffer ib -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        ib,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null,
                        coordinates.slice(bufLen - 1, 1)
                );
                case FloatBuffer fb -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        fb,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null,
                        coordinates.slice(bufLen - 1, 1)
                );
                default -> throw new ImageError("How?");
            });
            return coordinates.get(bufLen - 1);
        }
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, int @NonNull [] color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(color.length == 1);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue,
                    this.handle,
                    color,
                    coordinates.slice(0,3),
                    coordinates.slice(3,3),
                    bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null,
                    coordinates.slice(bufLen - 1, 1)
            ));
            return coordinates.get(bufLen - 1);
        }
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, float @NonNull [] color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(color.length == 1);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue,
                    this.handle,
                    color,
                    coordinates.slice(0,3),
                    coordinates.slice(3,3),
                    bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null,
                    coordinates.slice(bufLen - 1, 1)
            ));
            return coordinates.get(bufLen - 1);
        }
    }

    @Override
    public <CT2> long copy(@NonNull MemoryStack stack, long commandQueue, @NonNull Image<CT2> destination,
                           @NonNull Long from, int fromMipmap, @NonNull CT2 to, int toMipmap,
                           @NonNull CT2 size, long... dependencies) {
        try (MemoryStack substack = stack.push()) {
            PointerBuffer fromBuf = substack.mallocPointer(3);
            fromBuf.put(from);
            fromBuf.put(fromMipmap);
            fromBuf.put(0);
            fromBuf.rewind();
            PointerBuffer deps = null;
            if (dependencies != null && dependencies.length > 0) {
                deps = substack.mallocPointer(dependencies.length);
                deps.put(dependencies);
                deps.rewind();
            }
            PointerBuffer ev = stack.mallocPointer(1);
            handleEnqueueCopyImageError(CL12.clEnqueueCopyImage(commandQueue,
                    this.handle, destination.handle,
                    fromBuf, getCoordinates(substack, to, toMipmap),
                    getRegion(substack, size),
                    deps, ev
            ));
            return ev.get(0);
        }
    }

    @Override
    public <B extends Buffer> long read(@NonNull MemoryStack stack, long commandQueue,
                                        @NonNull Long from, int mipmap, @NonNull Long size,
                                        long rowPitch, long slicePitch,
                                        @NonNull B buffer, boolean blocking, long... dependencies) {
        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(switch (buffer) {
                case ByteBuffer bb -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, bb, waitList, ev);
                case ShortBuffer sb -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, sb, waitList, ev);
                case IntBuffer ib -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, ib, waitList, ev);
                case FloatBuffer fb -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, fb, waitList, ev);
                case DoubleBuffer db -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, db, waitList, ev);
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            return ev.get(0);
        }
    }

    @Override
    public <B extends Buffer> long write(@NonNull MemoryStack stack, long commandQueue,
                                         @NonNull Long from, int mipmap, @NonNull Long size,
                                         long rowPitch, long slicePitch,
                                         @NonNull B buffer, boolean blocking, long... dependencies) {
        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(switch (buffer) {
                case ByteBuffer bb -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, bb, waitList, ev);
                case ShortBuffer sb -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, sb, waitList, ev);
                case IntBuffer ib -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, ib, waitList, ev);
                case FloatBuffer fb -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, fb, waitList, ev);
                case DoubleBuffer db -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, db, waitList, ev);
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            return ev.get(0);
        }
    }

    private static PointerBuffer makeParameterBuffer(@NonNull MemoryStack stack, long from, long region, int mipmap, long... dependencies) {
        final long[] FILLER = new long[]{1,1};
        int bufLen = 7;
        if (dependencies != null && dependencies.length > 0)
            bufLen += dependencies.length;
        PointerBuffer coordinates = stack.mallocPointer(bufLen);
        coordinates.put(from);
        coordinates.put(mipmap);
        coordinates.put(0);
        coordinates.put(region);
        coordinates.put(FILLER);
        if (dependencies != null && dependencies.length > 0)
            coordinates.put(dependencies);
        return coordinates.rewind();
    }

    private static Workaround workaround(@NonNull MemoryStack stack,
                                         @NonNull BufferFlags memoryFlags,
                                         final long size,
                                         final int mipmaps,
                                         final @NonNull ChannelType channelType,
                                         final @NonNull ChannelOrder channelOrder,
                                         @Nullable ByteBuffer hostMemory) {
        try (MemoryStack substack = stack.push()) {
            ByteBuffer container = substack.malloc(CLImageFormat.SIZEOF + CLImageDesc.SIZEOF);
            try (CLImageFormat format = new CLImageFormat(container.slice(0, CLImageFormat.SIZEOF))) {
                try (CLImageDesc descriptor = new CLImageDesc(container.slice(CLImageFormat.SIZEOF, CLImageDesc.SIZEOF))) {
                    format.image_channel_data_type(channelType.type).image_channel_order(channelOrder.order);
                    descriptor.image_type(CL12.CL_MEM_OBJECT_IMAGE1D).image_width(size).image_height(0)
                            .image_depth(0)
                            .image_row_pitch(size * channelType.sizeof(channelOrder))
                            .image_slice_pitch(0)
                            .num_mip_levels(mipmaps)
                            .num_samples(0);
                    if (hostMemory != null && hostMemory.remaining() < descriptor.image_row_pitch())
                        throw new ImageError(String.format("Image size %d too large for host memory %d.",
                                descriptor.image_row_pitch(), hostMemory.remaining()));
                    return new Workaround(stack, memoryFlags, format, descriptor, hostMemory);
                }
            }
        }
    }
}

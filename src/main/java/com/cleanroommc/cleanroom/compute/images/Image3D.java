package com.cleanroommc.cleanroom.compute.images;

import com.cleanroommc.cleanroom.compute.buffers.BufferFlags;
import com.cleanroommc.cleanroom.compute.errors.ImageError;
import com.cleanroommc.cleanroom.compute.utils.ErrorUtils;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.google.common.base.Preconditions;
import org.joml.Vector3L;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLImageDesc;
import org.lwjgl.opencl.CLImageFormat;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleEnqueueCopyImageError;
import static com.cleanroommc.cleanroom.compute.utils.ErrorUtils.handleEnqueueReadWriteImageError;

public final class Image3D extends Image<Vector3L> {
    public Image3D(@NonNull MemoryStack stack,
                   @NonNull BufferFlags memoryFlags,
                   final long width,
                   final long height,
                   final long depth,
                   final int mipmaps,
                   final @NonNull ChannelType channelType,
                   final @NonNull ChannelOrder channelOrder,
                   @Nullable ByteBuffer hostMemory) {
        super(workaround(stack, memoryFlags, width, height, depth, mipmaps, channelType, channelOrder, hostMemory));
    }

    public Image3D(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags, int mipLevel) {
        Preconditions.checkArgument(texture.type == TextureType.TEX_3D);
        super(texture, memoryFlags, mipLevel);
    }

    public Image3D(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags) {
        this(texture, memoryFlags, 0);
    }

    @Override
    public <B extends Buffer> long fill(@NonNull MemoryStack stack, long commandQueue, @NonNull B color, @NonNull Vector3L from, @NonNull Vector3L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(color instanceof ByteBuffer
                || color instanceof IntBuffer
                || color instanceof FloatBuffer);
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 8;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap , dependencies);
            ErrorUtils.handleEnqueueFillImageError(switch (color) {
                case ByteBuffer bb -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        bb,
                        coordinates.slice(0, 4),
                        coordinates.slice(4, 3),
                        bufLen - 8 > 0 ? coordinates.slice(7, bufLen - 8) : null,
                        coordinates.slice(bufLen - 1, 1)
                );
                case IntBuffer ib -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        ib,
                        coordinates.slice(0, 4),
                        coordinates.slice(4, 3),
                        bufLen - 8 > 0 ? coordinates.slice(7, bufLen - 8) : null,
                        coordinates.slice(bufLen - 1, 1)
                );
                case FloatBuffer fb -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        fb,
                        coordinates.slice(0, 4),
                        coordinates.slice(4, 3),
                        bufLen - 8 > 0 ? coordinates.slice(7, bufLen - 8) : null,
                        coordinates.slice(bufLen - 1, 1)
                );
                default -> throw new ImageError("How?");
            });
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return coordinates.get(bufLen - 1);
        }
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, int @NonNull [] color, @NonNull Vector3L from, @NonNull Vector3L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue,
                    this.handle,
                    color,
                    coordinates.slice(0,4),
                    coordinates.slice(4,3),
                    bufLen - 8 > 0 ? coordinates.slice(6, bufLen - 8) : null,
                    coordinates.slice(bufLen - 1, 1)
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return coordinates.get(bufLen - 1);
        }
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, float @NonNull [] color, @NonNull Vector3L from, @NonNull Vector3L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 8;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue,
                    this.handle,
                    color,
                    coordinates.slice(0,4),
                    coordinates.slice(4,3),
                    bufLen - 8 > 0 ? coordinates.slice(6, bufLen - 8) : null,
                    coordinates.slice(bufLen - 1, 1)
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return coordinates.get(bufLen - 1);
        }
    }

    @Override
    public <CT2> long copy(@NonNull MemoryStack stack, long commandQueue, @NonNull Image<CT2> destination, @NonNull Vector3L from, int fromMipmap, @NonNull CT2 to, int toMipmap, @NonNull CT2 size, long... dependencies) {
        try (MemoryStack substack = stack.push()) {
            PointerBuffer fromBuf = substack.mallocPointer(4);
            fromBuf.put(from.x);
            fromBuf.put(from.y);
            fromBuf.put(from.z);
            fromBuf.put(fromMipmap);
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
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public <B extends Buffer> long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

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
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public <B extends Buffer> long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

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
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector3L from, int mipmap, @NonNull Vector3L size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(from.z + size.z < this.size.z);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, waitList, ev
            ));
            if (dependencies != null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            return ev.get(0);
        }
    }

    private static PointerBuffer makeParameterBuffer(@NonNull MemoryStack stack, Vector3L from, Vector3L region, int mipmap, long... dependencies) {
        int bufLen = 8;
        if (dependencies != null && dependencies.length > 0)
            bufLen += dependencies.length;
        PointerBuffer coordinates = stack.mallocPointer(bufLen);
        coordinates.put(from.x);
        coordinates.put(from.y);
        coordinates.put(from.z);
        coordinates.put(mipmap);
        coordinates.put(region.x);
        coordinates.put(region.y);
        coordinates.put(region.z);
        if (dependencies != null && dependencies.length > 0)
            coordinates.put(dependencies);
        return coordinates.rewind();
    }

    private static Workaround workaround(@NonNull MemoryStack stack,
                                         @NonNull BufferFlags memoryFlags,
                                         final long width,
                                         final long height,
                                         final long depth,
                                         final int mipmaps,
                                         final @NonNull ChannelType channelType,
                                         final @NonNull ChannelOrder channelOrder,
                                         @Nullable ByteBuffer hostMemory) {
        try (MemoryStack substack = stack.push()) {
            ByteBuffer container = substack.calloc(CLImageFormat.SIZEOF + CLImageDesc.SIZEOF);
            try (CLImageFormat format = new CLImageFormat(container.slice(0, CLImageFormat.SIZEOF))) {
                try (CLImageDesc descriptor = new CLImageDesc(container.slice(CLImageFormat.SIZEOF, CLImageDesc.SIZEOF))) {
                    format.image_channel_data_type(channelType.type).image_channel_order(channelOrder.order);
                    descriptor.image_type(CL12.CL_MEM_OBJECT_IMAGE3D).image_width(width).image_height(height)
                            .image_depth(depth)
                            .image_row_pitch(hostMemory != null ? width * channelType.sizeof(channelOrder) : 0)
                            .image_slice_pitch(hostMemory != null ? width * height * channelType.sizeof(channelOrder) : 0)
                            .num_mip_levels(mipmaps);
                    if (hostMemory != null && hostMemory.remaining() < descriptor.image_row_pitch() * descriptor.image_slice_pitch())
                        throw new ImageError(String.format("Image size %d too large for host memory %d.",
                                descriptor.image_row_pitch() * descriptor.image_slice_pitch(), hostMemory.remaining()));
                    return new Workaround(stack, memoryFlags, format, descriptor, hostMemory);
                }
            }
        }
    }

}

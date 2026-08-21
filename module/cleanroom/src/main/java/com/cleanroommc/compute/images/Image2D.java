package com.cleanroommc.compute.images;

import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.errors.ImageError;
import com.cleanroommc.compute.utils.ErrorUtils;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.kirino.gl.texture.TextureType;
import com.google.common.base.Preconditions;
import org.joml.Vector2L;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static com.cleanroommc.compute.utils.ErrorUtils.handleEnqueueCopyImageError;
import static com.cleanroommc.compute.utils.ErrorUtils.handleEnqueueReadWriteImageError;

public final class Image2D extends Image<Vector2L> {

    public Image2D(@NonNull MemoryStack stack,
                   @NonNull BufferFlags memoryFlags,
                   final long width,
                   final long height,
                   final int mipmaps,
                   final @NonNull ChannelType channelType,
                   final @NonNull ChannelOrder channelOrder,
                   @Nullable ByteBuffer hostMemory) {
        super(workaround(stack, memoryFlags, width, height, mipmaps, channelType, channelOrder, hostMemory));
    }

    public Image2D(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags, int mipLevel) {
        Preconditions.checkArgument(texture.type == TextureType.TEX_2D);
        super(texture, memoryFlags, mipLevel);
    }

    public Image2D(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags) {
        this(texture, memoryFlags, 0);
    }


    @Override
    public <B extends Buffer> long fill(@NonNull MemoryStack stack, long commandQueue, @NonNull B color, @NonNull Vector2L from, @NonNull Vector2L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(color instanceof ByteBuffer
                || color instanceof IntBuffer
                || color instanceof FloatBuffer);
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap , dependencies);
            PointerBuffer event = coordinates.slice(bufLen - 1, 1);
            PointerBuffer waitList = bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null;
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, event);
                if (waitList == null)
                    waitList = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                waitList.put(0, event.get(0)).rewind();
            }
            ErrorUtils.handleEnqueueFillImageError(switch (color) {
                case ByteBuffer bb -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        bb,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        this.isGLTexture() ? waitList.slice(0,1) : waitList,
                        event
                );
                case IntBuffer ib -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        ib,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        this.isGLTexture() ? waitList.slice(0,1) : waitList,
                        event
                );
                case FloatBuffer fb -> CL12.clEnqueueFillImage(
                        commandQueue,
                        this.handle,
                        fb,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        this.isGLTexture() ? waitList.slice(0,1) : waitList,
                        event
                );
                default -> throw new ImageError("How?");
            });
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else if (this.isGLTexture()) {
                CL10.clReleaseEvent(waitList.get(0));
                waitList.put(0, event.get(0)).rewind();
                event.rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, handle, waitList, event);
            }
            return event.get(0);
        }
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, int @NonNull [] color, @NonNull Vector2L from, @NonNull Vector2L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            PointerBuffer event = coordinates.slice(bufLen - 1, 1);
            PointerBuffer waitList = bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null;
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, event);
                if (waitList == null)
                    waitList = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                waitList.put(0, event.get(0)).rewind();
            }
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue,
                    this.handle,
                    color,
                    coordinates.slice(0,3),
                    coordinates.slice(3,3),
                    this.isGLTexture() ? waitList.slice(0,1) : waitList,
                    event
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else if (this.isGLTexture()) {
                CL10.clReleaseEvent(waitList.get(0));
                waitList.put(0, event.get(0)).rewind();
                event.rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, handle, waitList, event);
            }
            return event.get(0);
        }
    }

    @Override
    public long fill(@NonNull MemoryStack stack, long commandQueue, float @NonNull [] color, @NonNull Vector2L from, @NonNull Vector2L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            PointerBuffer event = coordinates.slice(bufLen - 1, 1);
            PointerBuffer waitList = bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null;
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, event);
                if (waitList == null)
                    waitList = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                waitList.put(0, event.get(0)).rewind();
            }
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue,
                    this.handle,
                    color,
                    coordinates.slice(0,3),
                    coordinates.slice(3,3),
                    this.isGLTexture() ? waitList.slice(0,1) : waitList,
                    event
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else if (this.isGLTexture()) {
                CL10.clReleaseEvent(waitList.get(0));
                waitList.put(0, event.get(0)).rewind();
                event.rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, handle, waitList, event);
            }
            return event.get(0);
        }
    }

    @Override
    public <CT2> long copy(@NonNull MemoryStack stack, long commandQueue, @NonNull Image<CT2> destination, @NonNull Vector2L from, int fromMipmap, @NonNull CT2 to, int toMipmap, @NonNull CT2 size, long... dependencies) {
        try (MemoryStack substack = stack.push()) {
            PointerBuffer handles = null;
            PointerBuffer fromBuf = substack.mallocPointer(3);
            fromBuf.put(from.x);
            fromBuf.put(from.y);
            fromBuf.put(fromMipmap);
            fromBuf.rewind();
            PointerBuffer deps = null;
            if (dependencies != null && dependencies.length > 0) {
                deps = substack.mallocPointer(dependencies.length);
                deps.put(dependencies);
                deps.rewind();
            }
            PointerBuffer ev = stack.mallocPointer(1);
            if (this.isGLTexture()) {
                if (destination.isGLTexture()) {
                    handles = substack.mallocPointer(2);
                    handles.put(destination.handle);
                } else {
                    handles = substack.mallocPointer(1);
                }
                handles.put(this.handle);
                handles.rewind();
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, handles, deps, ev);
                if (deps == null)
                    deps = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                deps.put(0, ev.get(0));
                deps.rewind();
            } else if (destination.isGLTexture()) {
                handles = substack.mallocPointer(1);
                handles.put(destination.handle);
                handles.rewind();
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, handles, deps, ev);
                if (deps == null)
                    deps = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                deps.put(0, ev.get(0));
                deps.rewind();
            }
            handleEnqueueCopyImageError(CL12.clEnqueueCopyImage(commandQueue,
                    this.handle, destination.handle,
                    fromBuf, getCoordinates(substack, to, toMipmap),
                    getRegion(substack, size),
                    handles == null ? deps : deps.slice(0,1), ev
            ));
            if (dependencies != null && handles == null)
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else if (handles != null) {
                deps.put(0, ev.get(0));
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, handles, deps.slice(0,1), ev);
                CL10.clReleaseEvent(deps.get(0));
            }
            return ev.get(0);
        }
    }

    @Override
    public <B extends Buffer> long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(switch (buffer) {
                case ByteBuffer bb -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, bb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case ShortBuffer sb -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, sb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case IntBuffer ib -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, ib, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case FloatBuffer fb -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, fb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case DoubleBuffer db -> CL12.clEnqueueReadImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, db, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long read(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public <B extends Buffer> long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(switch (buffer) {
                case ByteBuffer bb -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, bb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case ShortBuffer sb -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, sb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case IntBuffer ib -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, ib, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case FloatBuffer fb -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, fb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case DoubleBuffer db -> CL12.clEnqueueWriteImage(commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, db, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    @Override
    public long write(@NonNull MemoryStack stack, long commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.size.y);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            return ev.get(0);
        }
    }

    private static PointerBuffer makeParameterBuffer(@NonNull MemoryStack stack, Vector2L from, Vector2L region, int mipmap, long... dependencies) {
        int bufLen = 7;
        if (dependencies != null && dependencies.length > 0)
            bufLen += dependencies.length;
        PointerBuffer coordinates = stack.mallocPointer(bufLen);
        coordinates.put(from.x);
        coordinates.put(from.y);
        coordinates.put(mipmap);
        coordinates.put(region.x);
        coordinates.put(region.y);
        coordinates.put(1L);
        if (dependencies != null && dependencies.length > 0)
            coordinates.put(dependencies);
        return coordinates.rewind();
    }

    private static Workaround workaround(@NonNull MemoryStack stack,
                                         @NonNull BufferFlags memoryFlags,
                                         final long width,
                                         final long height,
                                         final int mipmaps,
                                         final @NonNull ChannelType channelType,
                                         final @NonNull ChannelOrder channelOrder,
                                         @Nullable ByteBuffer hostMemory) {
        try (MemoryStack substack = stack.push()) {
            ByteBuffer container = substack.calloc(CLImageFormat.SIZEOF + CLImageDesc.SIZEOF);
            try (CLImageFormat format = new CLImageFormat(container.slice(0, CLImageFormat.SIZEOF))) {
                try (CLImageDesc descriptor = new CLImageDesc(container.slice(CLImageFormat.SIZEOF, CLImageDesc.SIZEOF))) {
                    format.image_channel_data_type(channelType.type).image_channel_order(channelOrder.order);
                    descriptor.image_type(CL12.CL_MEM_OBJECT_IMAGE2D).image_width(width).image_height(height)
                            .image_row_pitch(hostMemory != null ? width * channelType.sizeof(channelOrder) : 0)
                            .num_mip_levels(mipmaps);
                    if (hostMemory != null && hostMemory.remaining() < descriptor.image_row_pitch())
                        throw new ImageError(String.format("Image size %d too large for host memory %d.",
                                descriptor.image_row_pitch(), hostMemory.remaining()));
                    return new Workaround(stack, memoryFlags, format, descriptor, hostMemory);
                }
            }
        }
    }
}

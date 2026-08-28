package com.cleanroommc.compute.images;

import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
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

/**
 * One-dimensional OpenCL image array.
 * <p>Coordinates are represented by {@link Vector2L}.</p>
 * @see Image
 */
public final class Image1DArray extends Image<Vector2L> {
    /**
     * <p>Creates a one-dimensional OpenCL image array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param memoryFlags Memory access flags.
     * @param size Width of the image.
     * @param mipMaps Number of mipmap levels.
     * @param arraySize Number of images in the array.
     * @param channelType Image channel data type.
     * @param channelOrder Image channel order.
     * @param hostMemory Initial host memory, or null if no host data is supplied.
     * @see Image
     * @author EΣrie
     */
    public Image1DArray(@NonNull MemoryStack stack,
                        @NonNull BufferFlags memoryFlags,
                        final long size,
                        final int mipMaps,
                        final long arraySize,
                        final @NonNull ChannelType channelType,
                        final @NonNull ChannelOrder channelOrder,
                        @Nullable ByteBuffer hostMemory) {
        super(workaround(stack, memoryFlags, size, mipMaps, arraySize, channelType, channelOrder, hostMemory));
    }

    /**
     * <p>Creates an OpenCL image from an OpenGL texture.</p>
     * @param texture OpenGL texture to share with OpenCL.
     * @param memoryFlags Memory access flags.
     * @param mipLevel Mipmap level of the OpenGL texture.
     * @see Image#Image(GLTexture, BufferFlags, int)
     * @author EΣrie
     */
    public Image1DArray(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags, int mipLevel) {
        Preconditions.checkArgument(texture.type == TextureType.TEX_1D_ARRAY);
        super(texture, memoryFlags, mipLevel);
    }

    /**
     * <p>Creates an OpenCL image from an OpenGL texture.</p>
     * @param texture OpenGL texture to share with OpenCL.
     * @param memoryFlags Memory access flags.
     * @see Image#Image(GLTexture, BufferFlags, int)
     * @apiNote Uses mipmap level 0.
     * @author EΣrie
     */
    public Image1DArray(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags) {
        this(texture, memoryFlags, 0);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The fill operation will be enqueued on this {@link CommandQueue}.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, java.nio.Buffer, Object, Object, int, long...)
     * @author EΣrie
     */
    @Override
    public <B extends Buffer> long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull B color, @NonNull Vector2L from, @NonNull Vector2L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(color instanceof ByteBuffer
                || color instanceof IntBuffer
                || color instanceof FloatBuffer);
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap , dependencies);
            PointerBuffer event = coordinates.slice(bufLen - 1, 1);
            PointerBuffer waitList = bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null;
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, event);
                if (waitList == null)
                    waitList = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                waitList.put(0, event.get(0)).rewind();
            }
            ErrorUtils.handleEnqueueFillImageError(switch (color) {
                case ByteBuffer bb -> CL12.clEnqueueFillImage(
                        commandQueue.commandQueue,
                        this.handle,
                        bb,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        this.isGLTexture() ? waitList.slice(0,1) : waitList,
                        event
                );
                case IntBuffer ib -> CL12.clEnqueueFillImage(
                        commandQueue.commandQueue,
                        this.handle,
                        ib,
                        coordinates.slice(0, 3),
                        coordinates.slice(3, 3),
                        this.isGLTexture() ? waitList.slice(0,1) : waitList,
                        event
                );
                case FloatBuffer fb -> CL12.clEnqueueFillImage(
                        commandQueue.commandQueue,
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
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, handle, waitList, event);
            }
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The fill operation will be enqueued on this {@link CommandQueue}.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, int[], Object, Object, int, long...)
     * @author EΣrie
     */
    @Override
    public long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] color, @NonNull Vector2L from, @NonNull Vector2L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            PointerBuffer event = coordinates.slice(bufLen - 1, 1);
            PointerBuffer waitList = bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null;
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, event);
                if (waitList == null)
                    waitList = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                waitList.put(0, event.get(0)).rewind();
            }
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue.commandQueue,
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
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, handle, waitList, event);
            }
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The fill operation will be enqueued on this {@link CommandQueue}.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, float[], Object, Object, int, long...)
     * @author EΣrie
     */
    @Override
    public long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] color, @NonNull Vector2L from, @NonNull Vector2L size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);
        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            int bufLen = 7;
            if (dependencies != null && dependencies.length > 0)
                bufLen += dependencies.length;
            PointerBuffer coordinates = makeParameterBuffer(substack, from, size, mipmap, dependencies);
            PointerBuffer event = coordinates.slice(bufLen - 1, 1);
            PointerBuffer waitList = bufLen - 7 > 0 ? coordinates.slice(6, bufLen - 7) : null;
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, event);
                if (waitList == null)
                    waitList = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                waitList.put(0, event.get(0)).rewind();
            }
            ErrorUtils.handleEnqueueFillImageError(CL12.clEnqueueFillImage(
                    commandQueue.commandQueue,
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
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, handle, waitList, event);
            }
            this.reference(commandQueue);
            return event.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The copy operation will be enqueued on this {@link CommandQueue}.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param fromMipmap Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param toMipmap Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see Image#copy(MemoryStack, CommandQueue, Image, Object, int, Object, int, Object, long...)
     * @author EΣrie
     */
    @Override
    public <CT2> long copy(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Image<CT2> destination, @NonNull Vector2L from, int fromMipmap, @NonNull CT2 to, int toMipmap, @NonNull CT2 size, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
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
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, handles, deps, ev);
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
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, handles, deps, ev);
                if (deps == null)
                    deps = substack.mallocPointer(1);
                else
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                deps.put(0, ev.get(0));
                deps.rewind();
            }
            handleEnqueueCopyImageError(CL12.clEnqueueCopyImage(commandQueue.commandQueue,
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
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, handles, deps.slice(0,1), ev);
                CL10.clReleaseEvent(deps.get(0));
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @author EΣrie
     */
    @Override
    public <B extends Buffer> long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(switch (buffer) {
                case ByteBuffer bb -> CL12.clEnqueueReadImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, bb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case ShortBuffer sb -> CL12.clEnqueueReadImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, sb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case IntBuffer ib -> CL12.clEnqueueReadImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, ib, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case FloatBuffer fb -> CL12.clEnqueueReadImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, fb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case DoubleBuffer db -> CL12.clEnqueueReadImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, db, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, short[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, int[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, float[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, double[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueReadImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @author EΣrie
     */
    @Override
    public <B extends Buffer> long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(switch (buffer) {
                case ByteBuffer bb -> CL12.clEnqueueWriteImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, bb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case ShortBuffer sb -> CL12.clEnqueueWriteImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, sb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case IntBuffer ib -> CL12.clEnqueueWriteImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, ib, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case FloatBuffer fb -> CL12.clEnqueueWriteImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, fb, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                case DoubleBuffer db -> CL12.clEnqueueWriteImage(commandQueue.commandQueue, this.handle, blocking,
                        getCoordinates(substack, from, mipmap), getRegion(substack, size),
                        rowPitch, slicePitch, db, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev);
                default -> throw new IllegalArgumentException("Wrong buffer type.");
            });
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, short[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, int[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, float[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, double[], boolean, long...)
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Vector2L from, int mipmap, @NonNull Vector2L size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(!commandQueue.isClosed());
        Preconditions.checkArgument(from.x + size.x < this.size.x);
        Preconditions.checkArgument(from.y + size.y < this.length);
        Preconditions.checkArgument(mipmap <= this.mipmaps);

        try (MemoryStack substack = stack.push()) {
            writeLock.lock();
            PointerBuffer waitList = null;
            if (dependencies != null && dependencies.length > 0)
                waitList = substack.mallocPointer(dependencies.length).put(dependencies).rewind();
            PointerBuffer ev = substack.mallocPointer(1);
            if (this.isGLTexture()) {
                CL12GL.clEnqueueAcquireGLObjects(commandQueue.commandQueue, this.handle, waitList, ev);
                if (dependencies != null)
                    for (long dependency : dependencies)
                        CL10.clReleaseEvent(dependency);
                else
                    waitList = substack.mallocPointer(1);
                waitList.put(0, ev.get(0)).rewind();
            }
            handleEnqueueReadWriteImageError(CL12.clEnqueueWriteImage(
                    commandQueue.commandQueue, this.handle, blocking,
                    getCoordinates(substack, from, mipmap), getRegion(substack, size),
                    rowPitch, slicePitch, array, this.isGLTexture() ? waitList.slice(0, 1) : waitList, ev
            ));
            if (dependencies != null && !this.isGLTexture())
                for (long dependency : dependencies)
                    CL10.clReleaseEvent(dependency);
            else {
                waitList.put(0, ev.get(0)).rewind();
                CL12GL.clEnqueueReleaseGLObjects(commandQueue.commandQueue, this.handle, waitList.slice(0, 1), ev);
            }
            this.reference(commandQueue);
            return ev.get(0);
        } finally {
            writeLock.unlock();
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
                                         final long size,
                                         final int mipmaps,
                                         final long arraySize,
                                         final @NonNull ChannelType channelType,
                                         final @NonNull ChannelOrder channelOrder,
                                         @Nullable ByteBuffer hostMemory) {
        try (MemoryStack substack = stack.push()) {
            ByteBuffer container = substack.calloc(CLImageFormat.SIZEOF + CLImageDesc.SIZEOF);
            try (CLImageFormat format = new CLImageFormat(container.slice(0, CLImageFormat.SIZEOF))) {
                try (CLImageDesc descriptor = new CLImageDesc(container.slice(CLImageFormat.SIZEOF, CLImageDesc.SIZEOF))) {
                    format.image_channel_data_type(channelType.type).image_channel_order(channelOrder.order);
                    descriptor.image_type(CL12.CL_MEM_OBJECT_IMAGE1D_ARRAY).image_width(size)
                            .image_row_pitch(hostMemory != null ? size * channelType.sizeof(channelOrder) : 0)
                            .num_mip_levels(mipmaps).image_array_size(arraySize);
                    if (hostMemory != null && hostMemory.remaining() < descriptor.image_row_pitch())
                        throw new ImageError(String.format("Image size %d too large for host memory %d.",
                                descriptor.image_row_pitch(), hostMemory.remaining()));
                    return new Workaround(stack, memoryFlags, format, descriptor, hostMemory);
                }
            }
        }
    }
}

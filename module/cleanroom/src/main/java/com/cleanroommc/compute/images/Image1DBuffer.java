package com.cleanroommc.compute.images;

import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.errors.ImageError;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CLImageDesc;
import org.lwjgl.opencl.CLImageFormat;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

/**
 * One-dimensional OpenCL image which may be backed by an OpenCL buffer.
 * <p>Coordinates are represented by {@link Long}.</p>
 * @see Image
 */
public final class Image1DBuffer extends Image1D {

    /**
     * Buffer backing this image, or null when the image is shared from OpenGL.
     */
    public final Buffer parent;

    /**
     * <p>Creates a one-dimensional OpenCL image backed by an OpenCL buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param memoryFlags Memory access flags.
     * @param size Width of the image.
     * @param buffer OpenCL buffer backing the image.
     * @param channelType Image channel data type.
     * @param channelOrder Image channel order.
     * @param hostMemory Initial host memory, or null if no host data is supplied.
     * @see Image
     * @author EΣrie
     */
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

    /**
     * <p>Creates an OpenCL image from an OpenGL texture.</p>
     * @param texture OpenGL texture to share with OpenCL.
     * @param memoryFlags Memory access flags.
     * @param mipLevel Mipmap level of the OpenGL texture.
     * @see Image#Image(GLTexture, BufferFlags, int)
     * @author EΣrie
     */
    public Image1DBuffer(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags, int mipLevel) {
        super(texture, memoryFlags, mipLevel);
        this.parent = null;
    }

    /**
     * <p>Creates an OpenCL image from an OpenGL texture.</p>
     * @param texture OpenGL texture to share with OpenCL.
     * @param memoryFlags Memory access flags.
     * @see Image#Image(GLTexture, BufferFlags, int)
     * @apiNote Uses mipmap level 0.
     * @author EΣrie
     */
    public Image1DBuffer(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags) {
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull B color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.fill(stack, commandQueue, color, from, size, mipmap, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.fill(stack, commandQueue, color, from, size, mipmap, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] color, @NonNull Long from, @NonNull Long size, int mipmap, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.fill(stack, commandQueue, color, from, size, mipmap, dependencies);
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
     * @apiNote This image does not support mipmaps; the source mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public <CT2> long copy(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Image<CT2> destination, @NonNull Long from, int fromMipmap, @NonNull CT2 to, int toMipmap, @NonNull CT2 size, long... dependencies) {
        return super.copy(stack, commandQueue, destination, from, fromMipmap, to, toMipmap, size, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public <B extends java.nio.Buffer> long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long read(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.read(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public <B extends java.nio.Buffer> long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, @NonNull B buffer, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, short @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, int @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, float @NonNull [] array, boolean blocking, long... dependencies) {
        Preconditions.checkArgument(mipmap == 0);
        return super.write(stack, commandQueue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @apiNote This image does not support mipmaps; the mipmap level must be 0.
     * @author EΣrie
     */
    @Override
    public long write(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Long from, int mipmap, @NonNull Long size, long rowPitch, long slicePitch, double @NonNull [] array, boolean blocking, long... dependencies) {
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

package com.cleanroommc.compute.images;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.buffers.BufferFlags;
import com.cleanroommc.compute.cmd.CommandQueue;
import com.cleanroommc.compute.errors.ImageError;
import com.cleanroommc.kirino.gl.texture.GLTexture;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.google.common.base.Preconditions;
import org.joml.Vector2L;
import org.joml.Vector3L;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Superclass for all Images.
 * @param <CT> Coordinate type. Defines how many dimensions an image has.
 */
public sealed abstract class Image<CT> extends SmartPointer permits Image1D, Image1DArray, Image2D, Image2DArray, Image3D {

    /**
     * OpenCL Pointer
     */
    public final long handle;
    /**
     * Image size, filled with ones to match.
     */
    protected final Vector3L size;
    /**
     * How many images in an array. 0 if not an array.
     */
    public final long length;
    public final int mipmaps;
    /**
     * GL Texture this is shared with if any. Otherwise, null.
     */
    public final @Nullable GLTexture texture;

    /**
     * <p><i>Internal constructor</i></p>
     * <p>Creates a new OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param memoryFlags memory access flags
     * @param format image format
     * @param descriptor image descriptor
     * @param hostMemory host memory buffer (nullable)
     * @author EΣrie
     */
    private Image(@NonNull MemoryStack stack,
                    @NonNull BufferFlags memoryFlags,
                    @NonNull CLImageFormat format,
                    @NonNull CLImageDesc descriptor,
                    @Nullable ByteBuffer hostMemory) {
        super();
        Preconditions.checkState(Compute.instance().supportsImages, "Images are not supported.");
        Preconditions.checkState(!(descriptor.num_mip_levels() > 0) || Compute.instance().supportsMipmaps, "Mipmaps are not supported.");
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(memoryFlags);
        Preconditions.checkNotNull(format);
        Preconditions.checkNotNull(descriptor);

        this.size = new Vector3L((int) descriptor.image_width(),
                (int) descriptor.image_height(),
                (int) descriptor.image_depth()
        );
        this.mipmaps = descriptor.num_mip_levels();
        this.length = descriptor.image_array_size();

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
        this.texture = null;
    }

    /**
     * <p><i>Internal constructor</i></p>
     * @param workaround all arguments to {@link Image#Image(MemoryStack, BufferFlags, CLImageFormat, CLImageDesc, ByteBuffer)}.
     * @apiNote workaround for a problem with Java
     */
    protected Image(@NonNull Workaround workaround) {
        this(workaround.stack(), workaround.memoryFlags(), workaround.format(), workaround.descriptor(), workaround.hostMemory());
    }

    /**
     * Create an image from an OpenGL Image
     * @param texture the OpenGL image.
     * @param memoryFlags memory access flags.
     * @param mipLevel number of mipmaps if any, otherwise 0.
     */
    public Image(@NonNull GLTexture texture, @NonNull BufferFlags memoryFlags, int mipLevel) {
        super();
        Preconditions.checkState(Compute.instance().supportsImages, "Images are not supported.");
        Preconditions.checkState(!(mipLevel > 0) || Compute.instance().supportsMipmaps, "Mipmaps are not supported.");
        Preconditions.checkNotNull(texture);
        Preconditions.checkNotNull(memoryFlags);
        Preconditions.checkArgument(memoryFlags.ordinal() < 3, "Flag %s is not allowed for GL buffers.", memoryFlags);

        this.texture = texture;
        this.size = new Vector3L(texture.extentX(), texture.extentY(), texture.extentZ());
        this.length = texture.layers();
        this.mipmaps = mipLevel;

        int[] err = new int[1];

        this.handle = CL12GL.clCreateFromGLTexture(Compute.instance().context, memoryFlags.flags, texture.type.glValue, mipLevel, texture.textureID, err);

        switch (err[0]) {
            case CL12GL.CL_INVALID_GL_OBJECT -> throw new IllegalArgumentException("Provided OpenGL texture is invalid.");
            case CL10.CL_INVALID_VALUE -> throw new ImageError("Host memory pointer specified for image created from memory object.");
            case CL12.CL_INVALID_IMAGE_SIZE -> throw new ImageError("Image dimensions exceed maximum permitted dimensions of the device.");
            case CL12.CL_IMAGE_FORMAT_NOT_SUPPORTED -> throw new ImageError("Unsupported image format.");
            case CL10.CL_MEM_OBJECT_ALLOCATION_FAILURE -> throw new ImageError("Failed to allocate memory for image.");
            case CL10.CL_INVALID_OPERATION -> throw new ImageError("None of the devices support images.");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL image.");
        }
    }

    /**
     * @return width
     * @author EΣrie
     */
    public final long width() {
        return this.size.x;
    }

    /**
     * @return height
     * @apiNote only in {@link Image1DArray}, {@link Image2D}, {@link Image2DArray} and {@link Image3D}.
     * @author EΣrie
     */
    public final long height() {
        return this.size.y;
    }

    /**
     * @return depth
     * @apiNote only in {@link Image2DArray} and {@link Image3D}.
     * @author EΣrie
     */
    public final long depth() {
        return this.size.z;
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
     * @see CommandQueue#imageFill(MemoryStack, Image, Buffer, Object, Object, int, long...)
     * @author EΣrie
     */
    public abstract <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull B color, @NonNull CT from, @NonNull CT size, int mipmap, long... dependencies);
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
     * @see CommandQueue#imageFill(MemoryStack, Image, int[], Object, Object, int, long...)
     * @author EΣrie
     */
    public abstract long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] color, @NonNull CT from, @NonNull CT size, int mipmap, long... dependencies);
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
     * @see CommandQueue#imageFill(MemoryStack, Image, float[], Object, Object, int, long...)
     * @author EΣrie
     */
    public abstract long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] color, @NonNull CT from, @NonNull CT size, int mipmap, long... dependencies);
    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The fill operation will be enqueued on this {@link CommandQueue}.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see CommandQueue#imageFill(MemoryStack, Image, Buffer, Object, Object, long...)
     * @apiNote Mipmap is always zero as this is a function for non-mipmapped images.
     * @author EΣrie
     */
    public final <B extends java.nio.Buffer> long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull B color, @NonNull CT from, @NonNull CT size, long... dependencies) {
        return this.fill(stack, commandQueue, color, from, size, 0, dependencies);
    }
    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The fill operation will be enqueued on this {@link CommandQueue}.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageFill(MemoryStack, Image, int[], Object, Object, long...)
     * @apiNote Mipmap is always zero as this is a function for non-mipmapped images.
     * @author EΣrie
     */
    public final long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, int @NonNull [] color, @NonNull CT from, @NonNull CT size, long... dependencies) {
        return this.fill(stack, commandQueue, color, from, size, 0, dependencies);
    }
    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The fill operation will be enqueued on this {@link CommandQueue}.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageFill(MemoryStack, Image, float[], Object, Object, long...)
     * @apiNote Mipmap is always zero as this is a function for non-mipmapped images.
     * @author EΣrie
     */
    public final long fill(@NonNull MemoryStack stack, CommandQueue commandQueue, float @NonNull [] color, @NonNull CT from, @NonNull CT size, long... dependencies) {
        return this.fill(stack, commandQueue, color, from, size, 0, dependencies);
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
     * @see CommandQueue#imageCopy(MemoryStack, Image, Image, Object, int, Object, int, Object, long...)
     * @see CommandQueue.Event#copy(Image, Image, Object, int, Object, int, Object, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract <CT2> long copy(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Image<CT2> destination,
                                    @NonNull CT from, int fromMipmap, @NonNull CT2 to, int toMipmap,
                                    @NonNull CT2 size, long... dependencies);
    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The copy operation will be enqueued on this {@link CommandQueue}.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param toMipmap Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see CommandQueue#imageCopy(MemoryStack, Image, Image, Object, int, Object, int, Object, long...)
     * @see CommandQueue.Event#copy(Image, Image, Object, int, Object, int, Object, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as copying from mipmap 0.
     */
    public final <CT2> long copy(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Image<CT2> destination,
                                 @NonNull CT from, @NonNull CT2 to, int toMipmap,
                                 @NonNull CT2 size, long... dependencies) {
        return this.copy(stack, commandQueue, destination, from, 0, to, toMipmap, size, dependencies);
    }
    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The copy operation will be enqueued on this {@link CommandQueue}.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param fromMipmap Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see CommandQueue#imageCopy(MemoryStack, Image, Image, Object, int, Object, int, Object, long...)
     * @see CommandQueue.Event#copy(Image, Image, Object, int, Object, int, Object, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as copying to mipmap 0.
     */
    public final <CT2> long copy(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Image<CT2> destination,
                                 @NonNull CT from, int fromMipmap, @NonNull CT2 to,
                                 @NonNull CT2 size, long... dependencies) {
        return this.copy(stack, commandQueue, destination, from, fromMipmap, to, 0, size, dependencies);
    }
    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The copy operation will be enqueued on this {@link CommandQueue}.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see CommandQueue#imageCopy(MemoryStack, Image, Image, Object, int, Object, int, Object, long...)
     * @see CommandQueue.Event#copy(Image, Image, Object, int, Object, int, Object, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as copying from mipmap 0 to mipmap 0.
     */
    public final <CT2> long copy(@NonNull MemoryStack stack, CommandQueue commandQueue, @NonNull Image<CT2> destination,
                                 @NonNull CT from, @NonNull CT2 to,
                                 @NonNull CT2 size, long... dependencies) {
        return this.copy(stack, commandQueue, destination, from, 0, to, 0, size, dependencies);
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
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, int, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract <B extends java.nio.Buffer> long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                                                          @NonNull CT from, int mipmap, @NonNull CT size,
                                                          long rowPitch, long slicePitch, @NonNull B buffer,
                                                          boolean blocking, long... dependencies);
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
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, int, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, short @NonNull [] array,
                              boolean blocking, long... dependencies);
    /**
     * <p>Read a region of an OpenCL image into an int array.</p>
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
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, int, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, int @NonNull [] array,
                              boolean blocking, long... dependencies);
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
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, int, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, float @NonNull [] array,
                              boolean blocking, long... dependencies);
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
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, int, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                              @NonNull CT from, int mipmap, @NonNull CT size,
                              long rowPitch, long slicePitch, double @NonNull [] array,
                              boolean blocking, long... dependencies);
    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as reading from mipmap 0.
     */
    public final <B extends java.nio.Buffer> long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, B buffer,
                                                       boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, buffer, blocking, dependencies);
    }
    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as reading from mipmap 0.
     */
    public final long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, short @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    /**
     * <p>Read a region of an OpenCL image into an int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as reading from mipmap 0.
     */
    public final long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, int @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as reading from mipmap 0.
     */
    public final long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, float @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The read operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageRead(MemoryStack, Image, Object, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#read(Image, Object, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as reading from mipmap 0.
     */
    public final long read(@NonNull MemoryStack stack, CommandQueue commandQueue,
                           CT from, CT size, long rowPitch, long slicePitch, double @NonNull [] array,
                           boolean blocking, long... dependencies) {
        return this.read(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
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
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract <B extends java.nio.Buffer> long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                                                           @NonNull CT from, int mipmap, @NonNull CT size,
                                                           long rowPitch, long slicePitch, @NonNull B buffer,
                                                           boolean blocking, long... dependencies);
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
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, short[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, short[], boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, short @NonNull [] array,
                               boolean blocking, long... dependencies);
    /**
     * <p>Write data from an int array to a region of an OpenCL image.</p>
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
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, int[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, int[], boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, int @NonNull [] array,
                               boolean blocking, long... dependencies);
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
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, float[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, float[], boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, float @NonNull [] array,
                               boolean blocking, long... dependencies);
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
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, double[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, double[], boolean, CommandQueue.Event...)
     * @author EΣrie
     */
    public abstract long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                               @NonNull CT from, int mipmap, @NonNull CT size,
                               long rowPitch, long slicePitch, double @NonNull [] array,
                               boolean blocking, long... dependencies);
    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, Object, long, long, java.nio.Buffer, boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, Object, long, long, java.nio.Buffer, boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as writing to mipmap 0.
     */
    public final <B extends java.nio.Buffer> long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                                                        @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, B buffer,
                                                       boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, buffer, blocking, dependencies);
    }
    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, short[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, short[], boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as writing to mipmap 0.
     */
    public final long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, short @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    /**
     * <p>Write data from an int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, int[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, int[], boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as writing to mipmap 0.
     */
    public final long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, int @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, float[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, float[], boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as writing to mipmap 0.
     */
    public final long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, float @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }
    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param commandQueue The write operation will be enqueued on this {@link CommandQueue}.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @return Event of the operation.
     * @see CommandQueue#imageWrite(MemoryStack, Image, Object, int, Object, long, long, double[], boolean, long...)
     * @see CommandQueue.Event#write(Image, Object, int, Object, long, long, double[], boolean, CommandQueue.Event...)
     * @author EΣrie
     * @apiNote Treated as writing to mipmap 0.
     */
    public final long write(@NonNull MemoryStack stack, CommandQueue commandQueue,
                            @NonNull CT from, @NonNull CT size, long rowPitch, long slicePitch, double @NonNull [] array,
                            boolean blocking, long... dependencies) {
        return this.write(stack, commandQueue, from, 0, size, rowPitch, slicePitch, array, blocking, dependencies);
    }

    /**
     * Releases the image and frees the memory.
     * @author EΣrie
     */
    @Override
    public final void close() {
        super.close();
        CL12.clReleaseMemObject(this.handle);
    }

    /**
     * @return Is this GL shared?
     */
    public final boolean isGLTexture() {
        return this.texture != null;
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

    /**
     * Arguments for the constructor
     * @param stack MemoryStack used for temporary native allocations.
     * @param memoryFlags memory access flags
     * @param format image format
     * @param descriptor image descriptor
     * @param hostMemory host memory buffer (nullable)
     * @apiNote workaround for a problem with Java
     * @author EΣrie
     */
    protected record Workaround(@NonNull MemoryStack stack,
                              @NonNull BufferFlags memoryFlags,
                              @NonNull CLImageFormat format,
                              @NonNull CLImageDesc descriptor,
                              @Nullable ByteBuffer hostMemory) {}
}

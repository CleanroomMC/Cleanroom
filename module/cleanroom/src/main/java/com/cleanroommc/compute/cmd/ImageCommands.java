package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.images.Image;
import com.google.common.base.Preconditions;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import com.cleanroommc.compute.cmd.CommandQueue.Event;

/** ImageCommands: operations bound to one command queue. */
public final class ImageCommands {
    private final CommandQueue queue;

    ImageCommands(CommandQueue queue) {
        this.queue = queue;
    }
    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Color used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, java.nio.Buffer, Object, Object, int, Event...)
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event fill(MemoryStack stack, Image<CT> image,
                                                           B color, CT from, CT size, int mipmap,
                                                           Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.fill(stack, queue, color, from, size, mipmap, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, java.nio.Buffer, Object, Object, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event fill(MemoryStack stack, Image<CT> image,
                                                           B color, CT from, CT size,
                                                           Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.fill(stack, queue, color, from, size, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, int[], Object, Object, int, Event...)
     * @author EΣrie
     */
    public <CT> Event fill(MemoryStack stack, Image<CT> image,
                                int [] color, CT from, CT size, int mipmap,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.fill(stack, queue, color, from, size, mipmap, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, int[], Object, Object, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event fill(MemoryStack stack, Image<CT> image,
                                int [] color, CT from, CT size,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.fill(stack, queue, color, from, size, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Color used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, float[], Object, Object, int, Event...)
     * @author EΣrie
     */
    public <CT> Event fill(MemoryStack stack, Image<CT> image,
                                float [] color, CT from, CT size, int mipmap,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.fill(stack, queue, color, from, size, mipmap, dependencies), stack);
    }

    /**
     * <p>Fill a region of an OpenCL image with a colour.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#fill(MemoryStack, CommandQueue, float[], Object, Object, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event fill(MemoryStack stack, Image<CT> image,
                                float [] color, CT from, CT size,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(color);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.fill(stack, queue, color, from, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param mipmapFrom Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param mipmapTo Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see Image#copy(MemoryStack, CommandQueue, Image, Object, int, Object, int, Object, Event...)
     * @author EΣrie
     */
    public <CT1, CT2> Event copy(MemoryStack stack,
                                      Image<CT1> start, Image<CT2> destination,
                                      CT1 from, int mipmapFrom,
                                      CT2 to, int mipmapTo, CT2 size,
                                      Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return queue.new Event(start.copy(stack, queue, destination, from, mipmapFrom, to, mipmapTo, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param mipmapTo Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see Image#copy(MemoryStack, CommandQueue, Image, Object, Object, int, Object, Event...)
     * @author EΣrie
     * @apiNote The source uses mipmap level 0.
     */
    public <CT1, CT2> Event copy(MemoryStack stack,
                                      Image<CT1> start, Image<CT2> destination,
                                      CT1 from,
                                      CT2 to, int mipmapTo, CT2 size,
                                      Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return queue.new Event(start.copy(stack, queue, destination, from, to, mipmapTo, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param mipmapFrom Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see Image#copy(MemoryStack, CommandQueue, Image, Object, int, Object, Object, Event...)
     * @author EΣrie
     * @apiNote The destination uses mipmap level 0.
     */
    public <CT1, CT2> Event copy(MemoryStack stack,
                                      Image<CT1> start, Image<CT2> destination,
                                      CT1 from, int mipmapFrom,
                                      CT2 to, CT2 size,
                                      Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return queue.new Event(start.copy(stack, queue, destination, from, mipmapFrom, to, size, dependencies), stack);
    }

    /**
     * <p>Copy a region from one OpenCL image to another.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return Event of the operation.
     * @see Image#copy(MemoryStack, CommandQueue, Image, Object, Object, Object, Event...)
     * @author EΣrie
     * @apiNote Both source and destination use mipmap level 0.
     */
    public <CT1, CT2> Event copy(MemoryStack stack,
                                      Image<CT1> start, Image<CT2> destination,
                                      CT1 from,
                                      CT2 to, CT2 size,
                                      Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(start);
        Preconditions.checkNotNull(destination);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(to);
        Preconditions.checkNotNull(size);
        return queue.new Event(start.copy(stack, queue, destination, from, to, size, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event read(MemoryStack stack, Image<CT> image,
                                                           CT from, int mipmap, CT size,
                                                           long rowPitch, long slicePitch, B buffer,
                                                           boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT, B extends java.nio.Buffer> Event read(MemoryStack stack, Image<CT> image,
                                                           CT from, int mipmap, CT size,
                                                           long rowPitch, long slicePitch, B buffer,
                                                           Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event read(MemoryStack stack, Image<CT> image,
                                                           CT from, CT size,
                                                           long rowPitch, long slicePitch, B buffer,
                                                           boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a NIO buffer.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event read(MemoryStack stack, Image<CT> image,
                                                           CT from, CT size,
                                                           long rowPitch, long slicePitch, B buffer,
                                                           Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, buffer, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, short [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, short [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, short [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a short array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, short [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, int [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, int [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, int [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a int array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, int [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, float [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, float [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, float [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a float array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, float [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, int, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, double [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, int mipmap, CT size,
                                long rowPitch, long slicePitch, double [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, double [] array,
                                boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack);
    }

    /**
     * <p>Read a region of an OpenCL image into a double array.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#read(MemoryStack, CommandQueue, Object, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(MemoryStack stack, Image<CT> image,
                                CT from, CT size,
                                long rowPitch, long slicePitch, double [] array,
                                Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.new Event(image.read(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event write(MemoryStack stack, Image<CT> image,
                                                            CT from, int mipmap, CT size,
                                                            long rowPitch, long slicePitch, B buffer,
                                                            boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT, B extends java.nio.Buffer> Event write(MemoryStack stack, Image<CT> image,
                                                            CT from, int mipmap, CT size,
                                                            long rowPitch, long slicePitch, B buffer, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event write(MemoryStack stack, Image<CT> image,
                                                            CT from, CT size,
                                                            long rowPitch, long slicePitch, B buffer,
                                                            boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, buffer, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a NIO buffer to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, java.nio.Buffer, boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event write(MemoryStack stack, Image<CT> image,
                                                            CT from, CT size,
                                                            long rowPitch, long slicePitch, B buffer, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(buffer);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, buffer, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, short [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, short [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, short [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a short array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, short[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, short [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, int [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, int [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, int [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a int array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, int[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, int [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, float [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, float [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, float [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a float array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, float[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, float [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, int, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, double [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, int mipmap, CT size,
                                 long rowPitch, long slicePitch, double [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, mipmap, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, double [] array,
                                 boolean blocking, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencies), stack, image, blocking, null);
    }

    /**
     * <p>Write data from a double array to a region of an OpenCL image.</p>
     * @param stack MemoryStack used for temporary native allocations.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on.
     * @param <CT> Image coordinate type.
     * @return Event of the operation.
     * @see Image#write(MemoryStack, CommandQueue, Object, Object, long, long, double[], boolean, Event...)
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(MemoryStack stack, Image<CT> image,
                                 CT from, CT size,
                                 long rowPitch, long slicePitch, double [] array, Event... dependencies) {
        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(image);
        Preconditions.checkNotNull(array);
        Preconditions.checkNotNull(from);
        Preconditions.checkNotNull(size);
        return queue.createWriteEvent(image.write(stack, queue, from, size, rowPitch, slicePitch, array, true, dependencies), stack, image, true, null);
    }
}

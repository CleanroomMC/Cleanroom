package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.images.Image;
import com.google.common.base.Preconditions;

import java.nio.*;

import com.cleanroommc.compute.cmd.CommandQueue.Event;

/** ImageEventCommands: operations bound to one event chain. */
public final class ImageEventCommands {
    private final CommandQueue queue;
    private final Event event;

    ImageEventCommands(CommandQueue queue, Event event) {
        this.queue = queue;
        this.event = event;
    }
    /**
     * Fills an image region after this event and returns the next event in the chain.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event fill(Image<CT> image, B color,
                                                               CT from, CT size, int mipmap,
                                                               final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.fill(event.stack, image, color, from, size, mipmap, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Fills an image region after this event and returns the next event in the chain.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event fill(Image<CT> image, B color,
                                                               CT from, CT size,
                                                               final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.fill(event.stack, image, color, from, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Fills an image region after this event and returns the next event in the chain.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event fill(Image<CT> image, int [] color,
                                    CT from, CT size, int mipmap,
                                    final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.fill(event.stack, image, color, from, size, mipmap, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Fills an image region after this event and returns the next event in the chain.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event fill(Image<CT> image, int [] color,
                                    CT from, CT size,
                                    final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.fill(event.stack, image, color, from, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Fills an image region after this event and returns the next event in the chain.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param mipmap Mipmap level of the image.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event fill(Image<CT> image, float [] color,
                                    CT from, CT size, int mipmap,
                                    final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.fill(event.stack, image, color, from, size, mipmap, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Fills an image region after this event and returns the next event in the chain.
     * @param image The image to fill.
     * @param color Colour used to fill the image region.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event fill(Image<CT> image, float [] color,
                                    CT from, CT size,
                                    final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.fill(event.stack, image, color, from, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Copies an image region after this event and returns the next event in the chain.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param mipmapFrom Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param mipmapTo Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT1, CT2> Event copy(Image<CT1> start, Image<CT2> destination,
                                          CT1 from, int mipmapFrom,
                                          CT2 to, int mipmapTo, CT2 size,
                                          final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        event.checkNonBlockingWrite(start);

        next = queue.images.copy(event.stack, start, destination, from, mipmapFrom, to, mipmapTo, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Copies an image region after this event and returns the next event in the chain.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param mipmapTo Mipmap level of the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote The source uses mipmap level 0.
     */
    public <CT1, CT2> Event copy(Image<CT1> start, Image<CT2> destination,
                                          CT1 from,
                                          CT2 to, int mipmapTo, CT2 size,
                                          final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        event.checkNonBlockingWrite(start);

        next = queue.images.copy(event.stack, start, destination, from, to, mipmapTo, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Copies an image region after this event and returns the next event in the chain.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param mipmapFrom Mipmap level of the source image.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote The destination uses mipmap level 0.
     */
    public <CT1, CT2> Event copy(Image<CT1> start, Image<CT2> destination,
                                          CT1 from, int mipmapFrom,
                                          CT2 to, CT2 size,
                                          final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        event.checkNonBlockingWrite(start);

        next = queue.images.copy(event.stack, start, destination, from, mipmapFrom, to, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Copies an image region after this event and returns the next event in the chain.
     * @param start Source image.
     * @param destination Destination image.
     * @param from Origin of the image region.
     * @param to Origin in the destination image.
     * @param size Size of the image region.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT1> Source image coordinate type.
     * @param <CT2> Destination image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote Both source and destination use mipmap level 0.
     */
    public <CT1, CT2> Event copy(Image<CT1> start, Image<CT2> destination,
                                          CT1 from,
                                          CT2 to, CT2 size,
                                          final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        event.checkNonBlockingWrite(start);

        next = queue.images.copy(event.stack, start, destination, from, to, size, dependencyEvents);


        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a NIO buffer after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                      CT from, int mipmap, CT size,
                                                      long rowPitch, long slicePitch, B buffer,
                                                      boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a NIO buffer after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                      CT from, int mipmap, CT size,
                                                      long rowPitch, long slicePitch, B buffer,
                                                      final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a NIO buffer after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                      CT from, CT size,
                                                      long rowPitch, long slicePitch, B buffer,
                                                      boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, buffer, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a NIO buffer after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Destination NIO buffer for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event read(Image<CT> image,
                                                      CT from, CT size,
                                                      long rowPitch, long slicePitch, B buffer,
                                                      final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, buffer, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a short array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, short [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a short array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, short [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a short array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, short [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a short array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, short [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into an int array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, int [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into an int array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, int [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into an int array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, int [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into an int array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, int [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a float array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, float [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a float array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, float [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a float array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, float [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a float array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, float [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a double array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, double [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a double array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, int mipmap, CT size,
                           long rowPitch, long slicePitch, double [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a double array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, double [] array,
                           boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        if (!blocking) event.checkNonBlockingWrite(image);

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents);


        if (blocking) event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Reads an image region into a double array after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Destination array for the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event read(Image<CT> image,
                           CT from, CT size,
                           long rowPitch, long slicePitch, double [] array,
                           final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.read(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a NIO buffer to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                       CT from, int mipmap, CT size,
                                                       long rowPitch, long slicePitch, B buffer,
                                                       boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, mipmap, size, rowPitch, slicePitch, buffer, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a NIO buffer to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                       CT from, int mipmap, CT size,
                                                       long rowPitch, long slicePitch, B buffer,
                                                       final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, mipmap, size, rowPitch, slicePitch, buffer, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a NIO buffer to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                       CT from, CT size,
                                                       long rowPitch, long slicePitch, B buffer,
                                                       boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, size, rowPitch, slicePitch, buffer, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a NIO buffer to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param buffer Source NIO buffer containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @param <B> Type of NIO buffer.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT, B extends java.nio.Buffer> Event write(Image<CT> image,
                                                       CT from, CT size,
                                                       long rowPitch, long slicePitch, B buffer,
                                                       final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, size, rowPitch, slicePitch, buffer, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, short [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, short [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, short [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a short array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, short [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, int [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, int [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, int [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from an int array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, int [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, float [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, float [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, float [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a float array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, float [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, double [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, mipmap, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param mipmap Mipmap level of the image.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, int mipmap, CT size,
                            long rowPitch, long slicePitch, double [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, mipmap, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param blocking Whether the operation blocks until the transfer is complete.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, double [] array,
                            boolean blocking, final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.createWriteEvent(image.write(event.stack, queue, from, size, rowPitch, slicePitch, array, blocking, dependencyEvents), event.stack, image, blocking, event.nonBlockingWrites);


        return event.transferOwnership(next);

    }

    /**
     * Writes data from a double array to an image region after this event and returns the next event in the chain.
     * @param image The image involved in the operation.
     * @param from Origin of the image region.
     * @param size Size of the image region.
     * @param rowPitch Row pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param slicePitch Slice pitch in bytes. A value of 0 lets OpenCL derive it.
     * @param array Source array containing the image data.
     * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
     * @param <CT> Image coordinate type.
     * @return The next event in the chain.
     * @throws IllegalStateException If this event can no longer be chained.
     * @author EΣrie
     * @apiNote This is always a blocking operation.
     * @apiNote This operates on mipmap level 0.
     */
    public <CT> Event write(Image<CT> image,
                            CT from, CT size,
                            long rowPitch, long slicePitch, double [] array,
                            final Event... dependencies) throws IllegalStateException {
        Preconditions.checkState(
            event.chainable,
            "This Event has already transferred or released its MemoryStack."
        );
        Event[] dependencyEvents = event.dependencyEvents(dependencies);
        Event next;

        next = queue.images.write(event.stack, image, from, size, rowPitch, slicePitch, array, true, dependencyEvents);


        event.clearNonBlockingWrites();
        return event.transferOwnership(next);

    }
}

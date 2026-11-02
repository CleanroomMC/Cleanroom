package com.cleanroommc.compute.cmd;

import com.cleanroommc.compute.Compute;
import com.cleanroommc.compute.buffers.Buffer;
import com.cleanroommc.compute.errors.UnavaliableDeviceError;
import com.cleanroommc.compute.smrtptr.GarbageCollector;
import com.cleanroommc.compute.smrtptr.SmartPointer;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;

import java.lang.ref.Cleaner;
import java.nio.*;
import java.util.Set;

/**
 * An OpenCL CommandQueue
 * @apiNote This is how you should use almost everything in OpenCL.
 * @author EΣrie
 */
public class CommandQueue extends SmartPointer {

    private final Cleaner.Cleanable cleanable;
    public final long commandQueue;
    private final long device;
    public final BufferCommands buffers = new BufferCommands(this);
    public final ImageCommands images = new ImageCommands(this);
    public final KernelCommands kernels = new KernelCommands(this);

    public long device() { return device; }

    /**
     * Creates a new CommandQueue for a device.
     * @param device Commands passed via this queue will be executed on this device.
     * @author EΣrie
     * @throws RuntimeException If the OpenCL context is invalid or,
     * the properties of the command queue are invalid or unsupported by the device.
     * @throws UnavaliableDeviceError If the device is not available for the context.
     * @throws OutOfMemoryError If there are not enough resources available to create the command queue.
     * @see CommandQueueDispatch#dispatch(String)
     * @see CommandQueueDispatch#dispatch(String, boolean, boolean, boolean)
     */
    CommandQueue(long device) throws RuntimeException, UnavaliableDeviceError, OutOfMemoryError {
        super();
        int[] err = new int[1];
        commandQueue = CL10.clCreateCommandQueue(
                Compute.instance().context,
                device,
                0,
                err
        );
        this.device = device;
        switch (err[0]) {
            case CL10.CL_INVALID_CONTEXT -> throw new RuntimeException("Invalid OpenCL context.");
            case CL10.CL_INVALID_DEVICE -> throw new UnavaliableDeviceError("Out of context device provided to command queue.");
            case CL10.CL_INVALID_VALUE -> throw new RuntimeException("Invalid value provided as command queue properties.");
            case CL10.CL_INVALID_QUEUE_PROPERTIES -> throw new RuntimeException("Queue properties unsupported by device");
            case CL10.CL_OUT_OF_RESOURCES, CL10.CL_OUT_OF_HOST_MEMORY -> throw new OutOfMemoryError("Not enough resources available to create OpenCL command queue.");
        }

        final long tmp = commandQueue;

        this.cleanable = GarbageCollector.INSTANCE.cleaner.register(this,
                GarbageCollector.INSTANCE.deletionTask(() -> CL10.clReleaseCommandQueue(tmp))
        );
    }

    /**
     * Releases this OpenCL command queue.
     * @author EΣrie
     */
    @Override
    public void close() {
        super.close();
        this.cleanable.clean();
    }

    Event createWriteEvent(long eventID,
                           @NonNull MemoryStack stack,
                           @NonNull SmartPointer object,
                           boolean blocking,
                           @Nullable Set<SmartPointer> nonBlockingWrites) {
        Event event = new Event(eventID, stack, nonBlockingWrites);
        event.updateNonBlockingWrites(object, blocking);
        return event;
    }

    /**
     * An event returned by a queued OpenCL command.
     * <p>Event instances form a chain that reuses the same MemoryStack until the chain is executed.</p>
     * @author EΣrie
     */
    public final class Event extends SmartPointer {
        public final long eventID;
        public final BufferEventCommands buffers = new BufferEventCommands(CommandQueue.this, this);
        public final ImageEventCommands images = new ImageEventCommands(CommandQueue.this, this);
        public final KernelEventCommands kernels = new KernelEventCommands(CommandQueue.this, this);
        final MemoryStack stack;
        private final Cleaner.Cleanable cleanable;

        /** True only when this chain created the stack itself. */
        boolean ownsStack;

        /**
         * A chained Event transfers its stack ownership to the next Event.
         * The previous Event remains usable as a dependency and must be closed by its owner.
         */
        boolean chainable = true;

        @Nullable Set<SmartPointer> nonBlockingWrites;

        Event(long eventID, @NonNull MemoryStack stack) {
            this(eventID, stack, null);
        }

        Event(long eventID, @NonNull MemoryStack stack, @Nullable Set<SmartPointer> nonBlockingWrites) {
            Preconditions.checkNotNull(stack);

            this.eventID = eventID;
            this.stack = stack;
            this.nonBlockingWrites = nonBlockingWrites;
            this.cleanable = GarbageCollector.INSTANCE.cleaner.register(this,
                    GarbageCollector.INSTANCE.deletionTask(() -> CL10.clReleaseEvent(eventID))
            );
        }

        /**
         * Queues a barrier after this event and returns the next event in the chain.
         * @param dependencies Additional events this operation depends on. They are borrowed; the caller remains responsible for closing them.
         * @return The next event in the chain.
         * @throws IllegalStateException If this event can no longer be chained.
         * @author EΣrie
         */
        public Event barrier(final Event... dependencies) throws IllegalStateException {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );
            Event[] dependencyEvents = dependencyEvents(dependencies);
            long[] ids = CommandQueue.eventIDs(dependencyEvents);
            PointerBuffer dependenciesBuffer = stack.mallocPointer(ids.length + 1);
            dependenciesBuffer.put(0, 0);
            for (int i = 0; i < ids.length; i++)
                dependenciesBuffer.put(i + 1, ids[i]);
            dependenciesBuffer.rewind();

            CL12.clEnqueueBarrierWithWaitList(commandQueue,
                    dependenciesBuffer.slice(1, ids.length),
                    dependenciesBuffer.slice(0, 1)
            );

            for (Event dependency : dependencyEvents)
                java.lang.ref.Reference.reachabilityFence(dependency);

            clearNonBlockingWrites();
            return transferOwnership(new Event(dependenciesBuffer.get(0), this.stack));

        }

        /**
         * Flushes the queue and ends this Event chain. If the stack was created
         * internally by CommandQueue, its active frame is closed here.
         * A caller-supplied MemoryStack is never closed by Event.
         */
        public void execute() {
            Preconditions.checkState(
                    chainable,
                    "This Event has already transferred or released its MemoryStack."
            );

            try {
                CL10.clFinish(commandQueue);
                close();
            } finally {
                chainable = false;
                releaseOwnedStack();
            }
        }

        /**
         * Releases Event and the MemoryStack
         */
        @Override
        public synchronized void close() {
            if (isClosed())
                return;
            super.close();
            cleanable.clean();
            chainable = false;
            releaseOwnedStack();
        }

        /**
         * Transfers ownership of the current chain stack to the next Event.
         */
        Event transferOwnership(Event next) {
            Preconditions.checkNotNull(next);

            if (next.nonBlockingWrites == null) {
                next.nonBlockingWrites = this.nonBlockingWrites;
            } else if (this.nonBlockingWrites != null && next.nonBlockingWrites != this.nonBlockingWrites) {
                this.nonBlockingWrites.addAll(next.nonBlockingWrites);
                next.nonBlockingWrites = this.nonBlockingWrites;
            }

            next.ownsStack = this.ownsStack;
            this.ownsStack = false;
            this.chainable = false;
            return next;
        }

        /**
         * The current event is always the first OpenCL dependency.
         */
        Event[] dependencyEvents(Event... dependencies) {
            Preconditions.checkNotNull(dependencies);
            Event[] result = new CommandQueue.Event[dependencies.length + 1];
            result[0] = this;
            for (int i = 0; i < dependencies.length; i++) {
                Event dependency = Preconditions.checkNotNull(dependencies[i]);
                Preconditions.checkArgument(dependency != this,
                        "The current Event is already an implicit dependency.");
                result[i + 1] = dependency;
            }
            return result;
        }

        /**
         * Releases only stacks created by CommandQueue itself. An externally
         * supplied MemoryStack remains the caller's responsibility.
         */
        private void releaseOwnedStack() {
            if (ownsStack) {
                stack.close();
                ownsStack = false;
                chainable = false;
            }
        }

        /**
         * Tracks objects used by non-blocking writes and clears the tracking set on blocking operations.
         * @param object Object written by the operation.
         * @param blocking Whether the operation is blocking.
         */
        private void updateNonBlockingWrites(@NonNull SmartPointer object, boolean blocking) {
            Preconditions.checkNotNull(object);

            if (blocking) {
                clearNonBlockingWrites();
                return;
            }

            if (nonBlockingWrites == null) {
                nonBlockingWrites = new ReferenceArraySet<>();
            }
            nonBlockingWrites.add(object);
        }

        /**
         * Clears the set of objects with outstanding non-blocking writes.
         */
        void clearNonBlockingWrites() {
            if (nonBlockingWrites != null)
                nonBlockingWrites.clear();
        }

        /**
         * Warns when an operation uses an object that still has an outstanding non-blocking write.
         * @param object Object about to be used by another operation.
         */
        void checkNonBlockingWrite(SmartPointer object) {
            if (nonBlockingWrites != null && nonBlockingWrites.contains(object)) {
                Compute.instance().LOGGER.warn("Potential data race caused by operation involving {} after non-blocking write.", object instanceof Buffer ? "a buffer" : "an image");
            }
        }
    }

    /** Converts dependency events to the native event wait list. */
    public static long[] eventIDs(Event... dependencies) {
        Preconditions.checkNotNull(dependencies);
        long[] ids = new long[dependencies.length];
        for (int i = 0; i < dependencies.length; i++) {
            Event dependency = Preconditions.checkNotNull(dependencies[i]);
            Preconditions.checkState(!dependency.isClosed(), "Dependency event has already been closed.");
            ids[i] = dependency.eventID;
        }
        return ids;
    }
}


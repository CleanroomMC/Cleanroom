package com.cleanroommc.compute.buffers;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CL20;

/**
 * Buffer Memory Flags
 * @author EΣrie
 */
public enum BufferFlags {
    /**
     * This flag specifies that the memory object will be read and written by a kernel. This is the default.
     */
    READ_WRITE(CL10.CL_MEM_READ_WRITE, true, true),
    /**
     * <p>This flag specifies that the memory object will be written but not read by a kernel.</p>
     * <p>Reading from a buffer or image object created with WRITE_ONLY inside a kernel is undefined.</p>
     * <p>{@link BufferFlags#READ_WRITE} and WRITE_ONLY are mutually exclusive.</p>
     */
    WRITE_ONLY(CL10.CL_MEM_WRITE_ONLY, false, true),
    /**
     * <p>This flag specifies that the memory object is a readonly memory object when used inside a kernel.</p>
     * <p>Writing to a buffer or image object created with READ_ONLY inside a kernel is undefined.</p>
     * <p>{@link BufferFlags#READ_WRITE} or {@link BufferFlags#WRITE_ONLY} and READ_ONLY are mutually exclusive.</p>
     */
    READ_ONLY(CL10.CL_MEM_READ_ONLY, true, false),
    /**
     * This flag specifies that the host will only write to the memory object
     * (using OpenCL APIs that enqueue a write or a map for write). This can be used to optimise
     * write access from the host (e.g. enable write-combined allocations for memory objects for
     * devices that communicate with the host over a system bus such as PCIe).
     */
    HOST_WRITE_ONLY(CL12.CL_MEM_HOST_WRITE_ONLY, false, true),
    /**
     * <p>This flag specifies that the host will only read the memory object (using OpenCL APIs that enqueue a read or a map for read).</p>
     * <p>{@link BufferFlags#HOST_WRITE_ONLY} and HOST_READ_ONLY are mutually exclusive.</p>
     */
    HOST_READ_ONLY(CL12.CL_MEM_HOST_READ_ONLY, true, false),
    /**
     * <p>This flag specifies that the host will not read or write the memory object.</p>
     * <p>{@link BufferFlags#HOST_WRITE_ONLY} or {@link BufferFlags#HOST_READ_ONLY} and HOST_NO_ACCESS are mutually exclusive.</p>
     */
    NO_ACCESS(CL12.CL_MEM_HOST_NO_ACCESS, false, false);

    public final long flags;
    public final boolean canRead;
    public final boolean canWrite;

    BufferFlags(long flags, boolean canRead, boolean canWrite) {
        this.flags = flags;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    /**
     * Can the two flags coexist on a memory object?
     * @param flags The flag to test for compatibility with.
     * @return Is this flag compatible with flags?
     */
    public boolean isConflicting(@NonNull BufferFlags flags) {
        Preconditions.checkNotNull(flags);
        return ((SUBBUFFER_CONFLICTS >>> ((6 - this.ordinal()) * 6 - flags.ordinal() - 1)) & 1) == 1; // Because I trust in my evil
    }

    private final static long SUBBUFFER_CONFLICTS = 0b000000101000110000000010000100000110; // Evil graph trick
}

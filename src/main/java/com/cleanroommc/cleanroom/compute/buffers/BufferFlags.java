package com.cleanroommc.cleanroom.compute.buffers;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.opencl.CL20;

public enum BufferFlags {
    READ_WRITE(CL10.CL_MEM_READ_WRITE, true, true),
    WRITE_ONLY(CL10.CL_MEM_WRITE_ONLY, false, true),
    READ_ONLY(CL10.CL_MEM_READ_ONLY, true, false),
    HOST_WRITE_ONLY(CL12.CL_MEM_HOST_WRITE_ONLY, false, true),
    HOST_READ_ONLY(CL12.CL_MEM_HOST_READ_ONLY, true, false),
    NO_ACCESS(CL12.CL_MEM_HOST_NO_ACCESS, false, false),
    KERNEL_READ_WRITE(CL20.CL_MEM_KERNEL_READ_AND_WRITE, true, true);

    public final long flags;
    public final boolean canRead;
    public final boolean canWrite;

    BufferFlags(long flags, boolean canRead, boolean canWrite) {
        this.flags = flags;
        this.canRead = canRead;
        this.canWrite = canWrite;
    }

    public boolean isConflicting(@NonNull BufferFlags flags) {
        Preconditions.checkNotNull(flags);
        return ((SUBBUFFER_CONFLICTS >>> ((6 - this.ordinal()) * 6 - flags.ordinal() - 1)) & 1) == 1; // Because I trust in my evil
    }

    private final static long SUBBUFFER_CONFLICTS = 0b000000101000110000000010000100000110; // Evil graph trick
}

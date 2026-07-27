package com.cleanroommc.cleanroom.compute;

import com.cleanroommc.cleanroom.compute.buffers.BufferFlags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BufferFlagsTest {
    @Test
    public void subbufferConflictsTest() {
        assertTrue(BufferFlags.READ_ONLY.isConflicting(BufferFlags.WRITE_ONLY));
        assertTrue(BufferFlags.READ_ONLY.isConflicting(BufferFlags.READ_WRITE));
        assertTrue(BufferFlags.WRITE_ONLY.isConflicting(BufferFlags.READ_ONLY));
        assertTrue(BufferFlags.WRITE_ONLY.isConflicting(BufferFlags.READ_WRITE));
        assertFalse(BufferFlags.READ_WRITE.isConflicting(BufferFlags.WRITE_ONLY));
        assertFalse(BufferFlags.READ_WRITE.isConflicting(BufferFlags.READ_ONLY));
        assertTrue(BufferFlags.HOST_WRITE_ONLY.isConflicting(BufferFlags.HOST_READ_ONLY));
        assertFalse(BufferFlags.HOST_WRITE_ONLY.isConflicting(BufferFlags.NO_ACCESS));
        assertTrue(BufferFlags.HOST_READ_ONLY.isConflicting(BufferFlags.HOST_WRITE_ONLY));
        assertFalse(BufferFlags.HOST_READ_ONLY.isConflicting(BufferFlags.NO_ACCESS));
        assertTrue(BufferFlags.NO_ACCESS.isConflicting(BufferFlags.HOST_WRITE_ONLY));
        assertTrue(BufferFlags.NO_ACCESS.isConflicting(BufferFlags.HOST_READ_ONLY));
    }
}

package com.cleanroommc.compute;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DeviceOrderingTest {
    private Device device(long handle) {
        return new Device(handle, null, new long[0], false, false, false);
    }

    @Test
    void distantHandlesRemainDistinct() {
        Device first = device(1);
        Device second = device(0x1_0000_0001L);
        assertTrue(first.compareTo(second) < 0);
        assertTrue(second.compareTo(first) > 0);
        assertEquals(0, first.compareTo(device(1)));
        assertTrue(device(Long.MIN_VALUE).compareTo(device(Long.MAX_VALUE)) < 0);
    }
}

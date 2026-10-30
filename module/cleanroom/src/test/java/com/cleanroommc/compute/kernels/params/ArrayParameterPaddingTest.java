package com.cleanroommc.compute.kernels.params;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayParameterPaddingTest {
    @Test
    void threeComponentsHaveFourComponentStorage() {
        assertArrayEquals(new int[]{1, 2, 3, 0}, new ArrayIntParameter(1, 2, 3).values());
        assertArrayEquals(new short[]{1, 2, 3, 0}, new ArrayShortParameter((short) 1, (short) 2, (short) 3).values());
        assertArrayEquals(new long[]{1, 2, 3, 0}, new ArrayLongParameter(1, 2, 3).values());
        assertArrayEquals(new float[]{1, 2, 3, 0}, new ArrayFloatParameter(1, 2, 3).values());
        assertArrayEquals(new double[]{1, 2, 3, 0}, new ArrayDoubleParameter(1, 2, 3).values());
    }

    @Test
    void paddingDoesNotModifyTheInput() {
        int[] original = {1, 2, 3};
        new ArrayIntParameter(original);
        assertArrayEquals(new int[]{1, 2, 3}, original);
        assertEquals(2, new ArrayIntParameter(1, 2).values().length);
        assertEquals(4, new ArrayIntParameter(1, 2, 3, 4).values().length);
        assertThrows(IllegalArgumentException.class, () -> new ArrayIntParameter(new int[5]));
    }
}

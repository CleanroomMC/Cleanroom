package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.utils.QuantileUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class QuantileUtilsTest {
    @Test
    public void testMedianIntegers() {
        Integer[] arr = {6, 3, 5, 2, 4, 1, 7, 9, 8};
        assertEquals(5, QuantileUtils.median(arr));
    }

    @Test
    public void testMedianStrings() {
        String[] arr = {"6", "3", "5", "2", "4", "1", "7", "9", "8"};
        assertEquals("5", QuantileUtils.median(arr));
    }
}

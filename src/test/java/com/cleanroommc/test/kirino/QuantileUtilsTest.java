package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.utils.QuantileUtils;
import net.minecraft.util.EnumFacing;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class QuantileUtilsTest {

    @Test
    public void testIntSizeOdd() {
        Random rng = new Random();
        rng.setSeed(114514);
        List<Integer> list = new java.util.ArrayList<>(IntStream.range(1, 4096).boxed().toList());
        float median = (float) list.get((list.size() / 2));
        Collections.shuffle(list, rng);
        assertEquals(median, QuantileUtils.median(list.toArray(new Integer[0])), 0.f);
    }

    @Test
    public void testIntSizeEven() {
        Random rng = new Random();
        rng.setSeed(114514);
        List<Integer> list = new java.util.ArrayList<>(IntStream.range(1, 4097).boxed().toList());
        float median = (float) (list.get((list.size() / 2) - 1) + list.get((list.size() / 2))) / 2;
        Collections.shuffle(list, rng);
        assertEquals(median, QuantileUtils.median(list.toArray(new Integer[0])), 0.f);
    }

    @Test
    public void testMedianStrings() {
        String[] arr = {"6", "3", "5", "2", "4", "1", "7", "9", "8"};
        assertEquals("5", QuantileUtils.median(arr));
    }

//    @Test
//    public void testMedianObjects() {
//        Random rng = new Random();
//        rng.setSeed(114514);
//
//        List<Meshlet> list = new ArrayList<>();
//        for (int x = 0; x < 16; x++) {
//            for (int y = 0; y < 16; y++) {
//                for (int z = 0; z < 16; z++) {
//                    if (rng.nextBoolean()) {
//                        list.add(new Meshlet(EnumFacing.DOWN, x, y, z, 0b111111,false));
//                    }
//                }
//            }
//        }
//
//        QuantileUtils.median(list.toArray(new Meshlet[0]));
//
//        //assertDoesNotThrow(() -> QuantileUtils.median(list.toArray(new Meshlet[0])));
//    }
}

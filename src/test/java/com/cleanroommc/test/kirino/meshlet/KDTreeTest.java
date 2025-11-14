package com.cleanroommc.test.kirino.meshlet;

import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.KDTreeBlock;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KDTreeTest {
    @Test
    public void testKNN() {
        KDTreeBlock near = new KDTreeBlock(0, 0, 0, 0b111111);
        KDTreeBlock far = new KDTreeBlock(15, 15, 15, 0b111111);
        KDTree kdTree = new KDTree(114514);
        kdTree.add(List.of(near, far));
        var neighbours = kdTree.knn(new KDTreeBlock(14,14,14, 0b111111), 16, 1, false);
        assertTrue("No neighbours returned", neighbours.isPresent());
        assertTrue("Did not find searched neighbour", neighbours.get().contains(far));
    }

    @Test
    public void testSimpleDeletion() {
        KDTreeBlock near = new KDTreeBlock(0, 0, 0, 0b111111);
        KDTreeBlock far = new KDTreeBlock(15, 15, 15, 0b111111);

        KDTree kdTree = new KDTree(114514);
        kdTree.add(List.of(near, far));

        kdTree.delete(far);
        var neighbours = kdTree.knn(new KDTreeBlock(14,14,14, 0b111111), 32, 1, false);

        assertTrue(neighbours.isPresent());
        assertTrue(neighbours.get().contains(near));
        assertFalse(neighbours.get().contains(far));
    }

    @Test
    public void testDeletion() {
        KDTreeBlock near = new KDTreeBlock(0, 0, 0, 0b111111);
        KDTreeBlock inner1 = new KDTreeBlock(7, 15, 15, 0b111111);
        KDTreeBlock inner2 = new KDTreeBlock(7, 10, 15, 0b111111);
        KDTreeBlock inner3 = new KDTreeBlock(7, 7, 15, 0b111111);
        KDTreeBlock inner4 = new KDTreeBlock(7, 10, 10, 0b111111);
        KDTreeBlock far = new KDTreeBlock(15, 15, 15, 0b111111);

        KDTree kdTree = new KDTree(114514);
        kdTree.add(List.of(near, far, inner1, inner2, inner3, inner4));

        kdTree.delete(far);
        var neighbours = kdTree.knn(new KDTreeBlock(14,14,14, 0b111111), 32, 1, false);

        assertTrue(neighbours.isPresent());
        assertTrue(neighbours.get().contains(inner1));
        assertFalse(neighbours.get().contains(far));
    }

    @Test
    public void testConstruction() {
        Random rng = new Random();
        rng.setSeed(114514);

        List<KDTreeBlock> list = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (rng.nextBoolean()) {
                        list.add(new KDTreeBlock(x, y, z, 0b111111));
                    }
                }
            }
        }

        KDTree kdTree = new KDTree(114514);

        try {
            kdTree.add(list);
        } catch (Throwable t) {
            fail(t.getMessage());
        }
    }

    @Test
    public void testObjectRemoval() {
        Random rng = new Random();
        rng.setSeed(114514);

        List<KDTreeBlock> list = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (rng.nextBoolean()) {
                        list.add(new KDTreeBlock(x, y, z, 0b111111));
                    }
                }
            }
        }

        KDTree kdTree = new KDTree(114514);

        try {
            kdTree.add(list);
            for (int i = 0; i < Math.min(10, list.size()); i++) {
                kdTree.delete(list.get(rng.nextInt(list.size())));
            }
            var ns = kdTree.knn(new KDTreeBlock(14,14,14, 0b111111), 10.f, 1, false);
            assertTrue(ns.isPresent());
        } catch (Throwable t) {
            fail(t.getMessage());
        }
        assertTrue(kdTree.getLeftExtremity().isPresent());
        assertTrue(kdTree.getRightExtremity().isPresent());
    }
}

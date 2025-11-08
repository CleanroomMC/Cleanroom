package com.cleanroommc.test.kirino.adt;

import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import net.minecraft.util.EnumFacing;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class KDTreeTest {
    @Test
    public void testKNN() {
        final EnumFacing normal = EnumFacing.NORTH;
        Meshlet near = new Meshlet(normal, 0, 0, 0, false);
        Meshlet far = new Meshlet(normal, 16, 16, 16, false);
        KDTree kdTree = new KDTree();
        kdTree.add(List.of(near, far));
        var neighbours = kdTree.knn(new Vector3f(14,14,14), 16, 1);
        assertTrue(neighbours.isPresent());
        assertTrue(neighbours.get().contains(far));
    }

    @Test
    public void testDeletion() {
        final EnumFacing normal = EnumFacing.NORTH;
        Meshlet near = new Meshlet(normal, 0, 0, 0, true);
        Meshlet far = new Meshlet(normal, 16, 16, 16, true);
        KDTree kdTree = new KDTree();
        kdTree.add(List.of(near, far));
        kdTree.delete(far);
        var neighbours = kdTree.knn(new Vector3f(14,14,14), 32, 1);
        assertTrue(neighbours.isPresent());
        assertTrue(neighbours.get().contains(near));
    }

    @Test
    public void testConstruction() {
        Random rng = new Random();
        rng.setSeed(114514);

        List<Meshlet> list = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (rng.nextBoolean()) {
                        list.add(new Meshlet(EnumFacing.DOWN, x, y, z, false));
                    }
                }
            }
        }

        KDTree kdTree = new KDTree();

        assertDoesNotThrow(() -> kdTree.add(list));
    }
}

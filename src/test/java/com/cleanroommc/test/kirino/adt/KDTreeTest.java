package com.cleanroommc.test.kirino.adt;

import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import net.minecraft.util.EnumFacing;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class KDTreeTest {
    @Test
    public void testKNN() {
        final EnumFacing normal = EnumFacing.NORTH;
        Meshlet near = new Meshlet(normal, 0, 0, 0);
        Meshlet far = new Meshlet(normal, 16, 16, 16);
        KDTree kdTree = new KDTree();
        kdTree.add(List.of(near, far));
        var neighbours = kdTree.knn(new Vector3f(14,14,14), 16, 1);
        assertTrue(neighbours.isPresent());
        assertTrue(neighbours.get().contains(far));
    }
}

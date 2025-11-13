package com.cleanroommc.test.kirino.meshlet;

import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import com.cleanroommc.kirino.engine.render.task.adt.Vector3b;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.EnumFacing;
import org.junit.Before;
import org.junit.Test;

import static com.cleanroommc.kirino.engine.render.task.job.ChunkMeshletGenJob.nearestNeighbourChain;
import static org.junit.Assert.*;

import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class MeshletGenerationTest {

    List<Vector3b> meshletsToAdd = new ObjectArrayList<>();
    KDTree kdTree;

    @Before
    public void setup() {
        Random rng = new Random();
        rng.setSeed(114514);

        BitSet blocks = new BitSet(4096);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (rng.nextBoolean()) {
                        blocks.set((z * 256) + (y * 16) + x);
                        if (y == 0 || !blocks.get((z * 256) + ((y-1) * 16) + x)) {
                            meshletsToAdd.add(new Vector3b(x, y, z));
                        }
                    }
                }
            }
        }

        kdTree = new KDTree(2137);
        kdTree.add(meshletsToAdd);
        //System.out.println(kdTree);
    }

    @Test
    public void nearestNeighbourChainTest() {
        List<Meshlet> generatedMeshlets = new ObjectArrayList<>();
        int idx = -1;
        while (kdTree.size() > 0) {
            idx++;
            Optional<Vector3b> start = idx % 2 == 1 ? kdTree.getLeftExtremity() : kdTree.getRightExtremity();

            if (start.isEmpty()) {
                continue;
            }

            try {
                generatedMeshlets.add(nearestNeighbourChain(kdTree, start.get(), 0, 0, 0, EnumFacing.DOWN, false));
            } catch (Throwable t) {
                t.printStackTrace();
                fail(t.getMessage());
            }
        }
        System.out.println(generatedMeshlets);
    }
}

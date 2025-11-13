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
    KDTree kdTreeRandom;
    KDTree kdTreeFlat;

    @Before
    public void setupRandom() {
        List<KDTreeBlock> meshletsToAdd = new ObjectArrayList<>();
        Random rng = new Random();
        rng.setSeed(114514);

        BitSet blocks = new BitSet(4096);

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    if (rng.nextBoolean()) {
                        blocks.set((z * 256) + (y * 16) + x);
                        if (y == 0 || !blocks.get((z * 256) + ((y-1) * 16) + x)) {
                            meshletsToAdd.add(new KDTreeBlock(x, y, z, 0b111111));
                        }
                    }
                }
            }
        }

        kdTreeRandom = new KDTree(2137);
        kdTreeRandom.add(meshletsToAdd);
        //System.out.println(kdTree);
    }

    @Test
    public void nearestNeighbourChainTest() {
        List<Meshlet> generatedMeshlets = new ObjectArrayList<>();
        int idx = -1;
        while (kdTreeRandom.size() > 0) {
            idx++;
            Optional<KDTreeBlock> start = idx % 2 == 1 ? kdTreeRandom.getLeftExtremity() : kdTreeRandom.getRightExtremity();

            if (start.isEmpty()) {
                continue;
            }

            try {
                generatedMeshlets.add(nearestNeighbourChain(kdTreeRandom, start.get(), 0, 0, 0, EnumFacing.DOWN, false));
            } catch (Throwable t) {
                t.printStackTrace();
                fail(t.getMessage());
            }
        }
        System.out.println(generatedMeshlets);
    }
}

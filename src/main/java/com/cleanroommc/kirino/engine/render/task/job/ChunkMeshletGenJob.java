package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.geometry.component.MeshletComponent;
import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class ChunkMeshletGenJob implements IParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobExternalDataQuery
    public int startY;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"isDirty"})
    public boolean isChunkDirty;

    @Override
    public void execute(EntityManager entityManager, int index) {
        if (!isChunkDirty)
            return;
        int x = chunkPosXArray.getInt(index);
        int z = chunkPosZArray.getInt(index);
        Chunk chunk = chunkProvider.provideChunk(x, z);
        KirinoCore.LOGGER.info("debug chunk xz: {}, {}", x, z);
        List<Meshlet> meshlets = new ObjectArrayList<>();
        MeshletComponent meshletComponent = new MeshletComponent();
        if (!chunk.isEmptyBetween(startY, startY+16)) {
            for (EnumFacing side : EnumFacing.values()) {
                meshlets.addAll(generateMeshlets(chunk, startY, side));
            }
        }
        for (Meshlet meshlet : meshlets) {
            setMeshletComponent(meshlet, meshletComponent);
            entityManager.createEntity(meshletComponent);
        }
    }

    private static int index(int x, int y, int z) {
        return (z*256)+(y*16)+x;
    }

    private @NonNull List<Meshlet> generateMeshlets(@NonNull Chunk chunk, int chunkY, EnumFacing side) {
        int chunkX = chunk.x << 4;
        int chunkZ = chunk.z << 4;

        KDTree tree = buildKDTree(chunk, chunkX, chunkY, chunkZ, side);
        List<Meshlet> meshlets = new ObjectArrayList<>();

        while(tree.size() > 0) {
            Optional<Meshlet> start = tree.size() % 2 == 1 ? tree.getLeftExtremity() : tree.getRightExtremity();
            start.ifPresent(meshlet -> meshlets.add(nearestNeighbourChain(tree, meshlet)));
        }

        return meshlets;
    }

    public @NonNull KDTree buildKDTree(@NonNull Chunk chunk, int chunkX, int chunkY, int chunkZ, @NonNull EnumFacing side) {
        Preconditions.checkNotNull(chunk);
        Preconditions.checkNotNull(side);

        List<Meshlet> toAdd = new ObjectArrayList<>();

        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int y = chunkY; y < chunkY + 16; y++) {
                for (int z = chunkZ; z < chunkZ + 16; z++) {
                    if (!isOpaqueBlockPresent(chunk,
                            chunkX, chunkY, chunkZ,
                            x + side.getXOffset(), y + side.getYOffset(), z + side.getZOffset())
                            && chunk.getBlockState(x, y, z).getMaterial() != Material.AIR) {
                        toAdd.add(new Meshlet(side, x, y, z)); // the tree building function KDTree::add uses a list for maximizing the balance
                    }
                }
            }
        }

        KDTree tree = new KDTree();
        tree.add(toAdd);
        return tree;
    }

    private static boolean isOpaqueBlockPresent(@NonNull Chunk chunk, int cpX, int cpY, int cpZ, int x, int y, int z) {
        if (y < cpY || y >= cpY+16) {
            return false;
        }
        if (x < cpX || x >= cpX+16) {
            return false;
        }
        if (z < cpZ || z >= cpZ+16) {
            return false;
        }
        return chunk.getBlockState(x,y,z).isOpaqueCube();
    }

    // Ironically it's closer to BFS, but it has the main trait of NNC which is the clustering.
    private static @NonNull Meshlet nearestNeighbourChain(@NonNull KDTree tree, @NonNull Meshlet meshlet) {
        Preconditions.checkNotNull(meshlet);
        Preconditions.checkNotNull(tree);

        Stack<Vector3f> stack = new ObjectArrayList<>();
        stack.push(meshlet.median());
        tree.delete(meshlet);
        while (!stack.isEmpty() && meshlet.size() < 32) {
            // Get 4 at once instead of just one like in normal NNS because it's faster
            Optional<List<Meshlet>> nearest = tree.knn(stack.pop(), 1.f, 4);
            if (nearest.isPresent()) {
                for(Meshlet m : nearest.get()) {
                    stack.push(m.median());
                    meshlet.merge(m);
                    tree.delete(m);
                }
            }
        }

        return meshlet;
    }

    private static void setMeshletComponent(@NonNull Meshlet meshlet, @NonNull MeshletComponent component) {
        component.aabb = meshlet.aabb();
        component.normal = meshlet.normal();
        List<Block> blocks = meshlet.emptyBlocks();
        for (int i = 0; i < blocks.size(); i++) {
            setComponentBlock(component, i, blocks.get(i));
        }
    }

    private static void setComponentBlock(@NonNull MeshletComponent component, int idx, @NonNull Block block) {
        Preconditions.checkNotNull(component);
        Preconditions.checkNotNull(block);
        Preconditions.checkPositionIndex(idx, 32);

        switch (idx) {
            case 0:
                component.block1 = block;
                break;
            case 1:
                component.block2 = block;
                break;
            case 2:
                component.block3 = block;
                break;
            case 3:
                component.block4 = block;
                break;
            case 4:
                component.block5 = block;
                break;
            case 5:
                component.block6 = block;
                break;
            case 6:
                component.block7 = block;
                break;
            case 7:
                component.block8 = block;
                break;
            case 8:
                component.block9 = block;
                break;
            case 9:
                component.block10 = block;
                break;
            case 10:
                component.block11 = block;
                break;
            case 11:
                component.block12 = block;
                break;
            case 12:
                component.block13 = block;
                break;
            case 13:
                component.block14 = block;
                break;
            case 14:
                component.block15 = block;
                break;
            case 15:
                component.block16 = block;
                break;
            case 16:
                component.block17 = block;
                break;
            case 17:
                component.block18 = block;
                break;
            case 18:
                component.block19 = block;
                break;
            case 19:
                component.block20 = block;
                break;
            case 20:
                component.block21 = block;
                break;
            case 21:
                component.block22 = block;
                break;
            case 22:
                component.block23 = block;
                break;
            case 23:
                component.block24 = block;
                break;
            case 24:
                component.block25 = block;
                break;
            case 25:
                component.block26 = block;
                break;
            case 26:
                component.block27 = block;
                break;
            case 27:
                component.block28 = block;
                break;
            case 28:
                component.block29 = block;
                break;
            case 29:
                component.block30 = block;
                break;
            case 30:
                component.block31 = block;
                break;
            case 31:
                component.block32 = block;
                break;
        }
    }

    @Override
    public void query(EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }
}

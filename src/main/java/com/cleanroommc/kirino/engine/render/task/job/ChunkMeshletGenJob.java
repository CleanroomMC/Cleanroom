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
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.cleanroommc.kirino.engine.render.task.adt.KDTree;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import com.cleanroommc.kirino.engine.render.task.adt.Vector3b;
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
import java.util.Objects;
import java.util.Optional;

public class ChunkMeshletGenJob implements IParallelJob {
    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobExternalDataQuery
    public GizmosManager gizmosManager;

    @JobExternalDataQuery
    public int startY;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"isDirty"})
    public IPrimitiveArray isChunkDirty;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index) {
        if (!isChunkDirty.getBool(index)) {
            return;
        }

        int x = chunkPosXArray.getInt(index);
        int z = chunkPosZArray.getInt(index);
        Chunk chunk = chunkProvider.provideChunk(x, z);
        if (chunk.isEmptyBetween(startY, startY+16)) {
            return;
        }
        KirinoCore.LOGGER.info("debug chunk xz: {}, {}", x, z);
        List<Meshlet> meshlets = new ObjectArrayList<>();
        MeshletComponent meshletComponent = new MeshletComponent();
        for (EnumFacing side : EnumFacing.values()) {
            meshlets.addAll(generateMeshlets(chunk, startY, side));
        }
        for (Meshlet meshlet : meshlets) {
            setMeshletComponent(meshlet, meshletComponent);
            entityManager.createEntity(meshletComponent);
            // todo: gizmosManager
        }
    }

    private @NonNull List<Meshlet> generateMeshlets(@NonNull Chunk chunk, int chunkY, EnumFacing side) {
        int chunkX = chunk.x << 4;
        int chunkZ = chunk.z << 4;

        final long seed = Objects.hash(chunkX, chunkZ);

        KDTree tree = new KDTree(seed);
        KDTree transparentTree = new KDTree(seed);

        //KirinoCore.LOGGER.info("building kD-Trees: {} {}", chunk.x, chunk.z);
        buildKDTree(chunk, chunkX, chunkY, chunkZ, side, tree, transparentTree);
        List<Meshlet> meshlets = new ObjectArrayList<>();

        //KirinoCore.LOGGER.info("opaque meshlets: {} {}", chunk.x, chunk.z);
        for (int idx = 0; tree.size() > 0; idx++) {
            Optional<Vector3b> startPoint = idx % 2 == 1 ? tree.getLeftExtremity() : tree.getRightExtremity();
            startPoint.ifPresent(vector3b -> meshlets.add(nearestNeighbourChain(tree, vector3b, chunkX, chunkY, chunkZ, side, false)));
        }

        //KirinoCore.LOGGER.info("transparent meshlets: {} {}", chunkX, chunkZ);
        for (int idx = 0; transparentTree.size() > 0; idx++) {
            Optional<Vector3b> startPoint = idx % 2 == 1 ? transparentTree.getLeftExtremity() : transparentTree.getRightExtremity();
            startPoint.ifPresent(vector3b -> meshlets.add(nearestNeighbourChain(transparentTree, vector3b, chunkX, chunkY, chunkZ, side, true)));
        }

        return meshlets;
    }

    public void buildKDTree(@NonNull Chunk chunk, int chunkX, int chunkY, int chunkZ, @NonNull EnumFacing side, KDTree tree, KDTree transparentTree) {
        Preconditions.checkNotNull(chunk);
        Preconditions.checkNotNull(side);

        List<Vector3b> toAdd = new ObjectArrayList<>();
        List<Vector3b> transparents = new ObjectArrayList<>();

        for (int x = chunkX; x < chunkX + 16; x++) {
            for (int y = chunkY; y < chunkY + 16; y++) {
                for (int z = chunkZ; z < chunkZ + 16; z++) {
                    if (chunk.getBlockState(x,y,z).isFullCube() && chunk.getBlockState(x,y,z).getMaterial() != Material.AIR) {
                        if (!isOpaqueBlockPresent(chunk,
                                chunkX, chunkY, chunkZ,
                                x + side.getXOffset(), y + side.getYOffset(), z + side.getZOffset())
                                && chunk.getBlockState(x, y, z).getMaterial() != Material.AIR) {
                            // The tree building function KDTree::add uses a list for maximizing the balance.
                            if (chunk.getBlockState(x,y,z).isOpaqueCube()) {
                                toAdd.add(new Vector3b(x, y, z));
                            } else {
                                transparents.add(new Vector3b(x, y, z));
                            }
                        }
                    }
                }
            }
        }

        tree.add(toAdd);

        transparentTree.add(transparents);
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
    public static @NonNull Meshlet nearestNeighbourChain(@NonNull KDTree tree,
                                                          @NonNull Vector3b meshletStart,
                                                          int chunkX,
                                                          int chunkY,
                                                          int chunkZ,
                                                          EnumFacing side,
                                                          boolean transparent) {
        Preconditions.checkNotNull(meshletStart);
        Preconditions.checkNotNull(tree);
        Preconditions.checkNotNull(side);

        Stack<Vector3b> stack = new ObjectArrayList<>();
        stack.push(meshletStart);
        tree.delete(meshletStart);
        Meshlet meshlet = new Meshlet(side,
                chunkX + meshletStart.x,
                chunkY + meshletStart.y,
                chunkZ + meshletStart.z,
                transparent);
        while (!stack.isEmpty() && meshlet.size() < 32) {
            // Get 4 at once instead of just one like in normal NNS because it's faster
            Optional<List<Vector3b>> nearest = tree.knn(stack.pop(), 1.f, 4, true);
            if (nearest.isPresent()) {
                for(Vector3b m : nearest.get()) {
                    if (meshlet.size() >= 32) {
                        break;
                    }
                    stack.push(m);
                    meshlet.addBlock(new Vector3f(
                            chunkX + m.x,
                            chunkY + m.y,
                            chunkZ + m.z));
                    tree.delete(m);
                }
            }
        }

        return meshlet;
    }

    private static void setMeshletComponent(@NonNull Meshlet meshlet, @NonNull MeshletComponent component) {
        component.aabb = meshlet.aabb();
        component.normal = meshlet.normal();
        component.transparent = meshlet.transparent();
        List<Block> blocks = meshlet.blockList();
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

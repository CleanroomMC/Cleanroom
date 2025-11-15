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
import com.cleanroommc.kirino.engine.render.task.adt.KDTreeBlock;
import com.cleanroommc.kirino.engine.render.task.adt.Meshlet;
import com.cleanroommc.kirino.engine.render.task.meshing.BoundedChunk;
import com.cleanroommc.kirino.engine.render.task.meshing.Visibility;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.cleanroommc.kirino.engine.render.task.meshing.Visibility.*;

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

        BoundedChunk chunk = new BoundedChunk(chunkProvider, x, z);
        if (chunk.getCenter().isEmptyBetween(startY, startY + 16)) {
            return;
        }
        KirinoCore.LOGGER.info("debug chunk xz: {}, {}", x, z);
        List<Meshlet> meshlets = new ObjectArrayList<>();
        MeshletComponent meshletComponent = new MeshletComponent();

        int chunkX = chunk.centerX << 4;
        int chunkZ = chunk.centerZ << 4;
        QuadGroups quadGroups = buildQuadGroups(chunk, chunkX, startY, chunkZ);

        KDTree[] forest = new KDTree[6];
        KDTree[] transparentForest = new KDTree[6];
        buildKDTree(quadGroups, chunkX, startY, chunkZ, forest, transparentForest);
        for (EnumFacing side : EnumFacing.values()) {
            meshlets.addAll(generateMeshlets(chunk.getCenter(), startY, side, forest[side.getIndex()], transparentForest[side.getIndex()]));
        }
        for (Meshlet meshlet : meshlets) {
            setMeshletComponent(meshlet, meshletComponent);
            entityManager.createEntity(meshletComponent);
            gizmosManager.addMeshlet(meshlet);
        }
    }

    @SuppressWarnings("unchecked")
    private QuadGroups buildQuadGroups(@NonNull BoundedChunk chunk, int chunkX, int chunkY, int chunkZ) {
        Preconditions.checkNotNull(chunk);

        EnumFacing[] sides = EnumFacing.values();
        List<KDTreeBlock>[] opaqueGroups = new ObjectArrayList[sides.length];
        List<KDTreeBlock>[] transparentGroups = new ObjectArrayList[sides.length];

        for (int i = 0; i < sides.length; i++) {
            opaqueGroups[i] = new ObjectArrayList<>();
            transparentGroups[i] = new ObjectArrayList<>();
        }

        // TODO: Cache calls to getBlockState
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    switch (chunk.getVisibility(x, y, z, null)) {
                        case EMPTY -> {
                        }
                        case Visibility vis -> {
                            int faces = 0;
                            // Gather the faces to render
                            for (EnumFacing side : sides) {
                                Visibility neighborVis = chunk.getVisibility(x, y, z, side);
                                boolean generate;
                                if ((vis == OPAQUE && (neighborVis == EMPTY || neighborVis == TRANSPARENT))
                                        || (vis == TRANSPARENT && neighborVis == EMPTY)) {
                                    generate = true;
                                } else if (vis == TRANSPARENT && neighborVis == TRANSPARENT) {
                                    IBlockState state = chunk.getBlockState(x, y, z, null);
                                    IBlockState neighborState = chunk.getBlockState(x, y, z, side);
                                    generate = state != neighborState;
                                } else {
                                    generate = false;
                                }

                                if (generate) {
                                    faces |= PERMUTATION[side.getIndex()];
                                }
                            }
                            // Add the faces to their groups
                            for (int i = 0; i < sides.length; i++) {
                                if ((faces & PERMUTATION[i]) != PERMUTATION[i]) {
                                    continue;
                                }
                                if (chunk.getVisibility(x, y, z, null) == OPAQUE) {
                                    opaqueGroups[i].add(new KDTreeBlock(x + chunkX, y + chunkY, z + chunkZ, faces));
                                } else {
                                    transparentGroups[i].add(new KDTreeBlock(x + chunkX, y + chunkY, z + chunkZ, faces));
                                }
                            }
                        }
                    }
                }
            }
        }
        return new QuadGroups(opaqueGroups, transparentGroups);
    }

    private void buildKDTree(@NonNull QuadGroups quadGroups, int chunkX, int chunkY, int chunkZ, KDTree[] forest, KDTree[] transparentForest) {
        final long seed = Objects.hash(chunkX, chunkY, chunkZ);

        EnumFacing[] sides = EnumFacing.values();

        for (int i = 0; i < sides.length; i++) {
            forest[i] = new KDTree(seed);
            forest[i].add(quadGroups.opaque()[i]);
            transparentForest[i] = new KDTree(seed);
            transparentForest[i].add(quadGroups.transparent()[i]);
        }
    }

    private @NonNull List<Meshlet> generateMeshlets(@NonNull Chunk chunk, int chunkY, EnumFacing side, KDTree tree, KDTree transparentTree) {
        int chunkX = chunk.x << 4;
        int chunkZ = chunk.z << 4;

        List<Meshlet> meshlets = new ObjectArrayList<>();

        //KirinoCore.LOGGER.info("opaque meshlets: {} {}", chunk.x, chunk.z);
        for (int idx = 0; tree.size() > 0; idx++) {
            Optional<KDTreeBlock> startPoint = idx % 2 == 1 ? tree.getLeftExtremity() : tree.getRightExtremity();
            startPoint.ifPresent(vector3b -> meshlets.add(nearestNeighbourChain(tree, vector3b, chunkX, chunkY, chunkZ, side, false)));
        }

        //KirinoCore.LOGGER.info("transparent meshlets: {} {}", chunkX, chunkZ);
        for (int idx = 0; transparentTree.size() > 0; idx++) {
            Optional<KDTreeBlock> startPoint = idx % 2 == 1 ? transparentTree.getLeftExtremity() : transparentTree.getRightExtremity();
            startPoint.ifPresent(vector3b -> meshlets.add(nearestNeighbourChain(transparentTree, vector3b, chunkX, chunkY, chunkZ, side, true)));
        }

        return meshlets;
    }

    // Ironically it's closer to BFS, but it has the main trait of NNC which is the clustering.
    public static @NonNull Meshlet nearestNeighbourChain(@NonNull KDTree tree,
                                                          @NonNull KDTreeBlock meshletStart,
                                                          int chunkX,
                                                          int chunkY,
                                                          int chunkZ,
                                                          EnumFacing side,
                                                          boolean transparent) {
        Preconditions.checkNotNull(meshletStart);
        Preconditions.checkNotNull(tree);
        Preconditions.checkNotNull(side);

        Stack<KDTreeBlock> stack = new ObjectArrayList<>();
        stack.push(meshletStart);
        tree.delete(meshletStart);
        Meshlet meshlet = new Meshlet(side,
                chunkX + meshletStart.x,
                chunkY + meshletStart.y,
                chunkZ + meshletStart.z,
                meshletStart.faces,
                transparent);
        while (!stack.isEmpty() && meshlet.size() < 32) {
            // Get 4 at once instead of just one like in normal NNS because it's faster
            Optional<List<KDTreeBlock>> nearest = tree.knn(stack.pop(), 1.f, 1, true);
            if (nearest.isPresent() && !nearest.get().isEmpty()) {
                KDTreeBlock m = nearest.get().getFirst();
                if (meshlet.size() >= 32) {
                    break;
                }
                stack.push(m);
                meshlet.addBlock(new Block(
                            chunkX + m.x,
                            chunkY + m.y,
                            chunkZ + m.z,
                            m.faces));
                tree.delete(m);
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

    private static final int[] PERMUTATION = {
        0b000100, 0b001000, 0b000001, 0b000010, 0b010000, 0b100000
    };

    private record QuadGroups(List<KDTreeBlock>[] opaque, List<KDTreeBlock>[] transparent) {}
}

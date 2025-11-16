package com.cleanroommc.kirino.engine.render.task.job;

import com.cleanroommc.kirino.ecs.entity.EntityManager;
import com.cleanroommc.kirino.ecs.entity.EntityQuery;
import com.cleanroommc.kirino.ecs.job.IParallelJob;
import com.cleanroommc.kirino.ecs.job.JobDataQuery;
import com.cleanroommc.kirino.ecs.job.JobExternalDataQuery;
import com.cleanroommc.kirino.ecs.storage.IPrimitiveArray;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.engine.render.geometry.Meshlet;
import com.cleanroommc.kirino.engine.render.geometry.component.ChunkComponent;
import com.cleanroommc.kirino.engine.render.gizmos.GizmosManager;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.ChunkProviderClient;
import net.minecraft.init.Blocks;
import net.minecraft.world.chunk.Chunk;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ChunkMeshletGenJob implements IParallelJob {
    // todo: pass opaque or transparent
    public int pass = 0; // 0: opaque, 1: transparent

    @JobExternalDataQuery
    public ChunkProviderClient chunkProvider;

    @JobExternalDataQuery
    public GizmosManager gizmosManager;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosX"})
    public IPrimitiveArray chunkPosXArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosY"})
    public IPrimitiveArray chunkPosYArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"chunkPosZ"})
    public IPrimitiveArray chunkPosZArray;

    @JobDataQuery(componentClass = ChunkComponent.class, fieldAccessChain = {"isDirty"})
    public IPrimitiveArray isChunkDirty;

    @Override
    public void query(@NonNull EntityQuery entityQuery) {
        entityQuery.with(ChunkComponent.class);
    }

    record ChunkCluster(
            int chunkY,
            Chunk center,
            Chunk xPlus,
            Chunk xMinus,
            Chunk zPlus,
            Chunk zMinus) {
    }

    final static ImmutableList<Vector3i> FACE_DIRS = ImmutableList.of(
            new Vector3i(1, 0, 0),
            new Vector3i(-1, 0, 0),
            new Vector3i(0, 1, 0),
            new Vector3i(0, -1, 0),
            new Vector3i(0, 0, 1),
            new Vector3i(0, 0, -1));

    final static int FACE_X_POS = 0b100000;
    final static int FACE_X_NEG = 0b010000;
    final static int FACE_Y_POS = 0b001000;
    final static int FACE_Y_NEG = 0b000100;
    final static int FACE_Z_POS = 0b000010;
    final static int FACE_Z_NEG = 0b000001;

    final static double MESHLET_MAX_ANGLE = 1.1f * Math.PI / 2f;
    final static int MESHLET_MAX_SIZE = 32;

    @Override
    public void execute(@NonNull EntityManager entityManager, int index) {
        Preconditions.checkState(pass == 0 || pass == 1,
                "Invalid pass number %d. Must be either 0 or 1.", pass);

        if (!isChunkDirty.getBool(index)) {
            return;
        }

        int chunkX = chunkPosXArray.getInt(index);
        int chunkY = chunkPosYArray.getInt(index);
        int chunkZ = chunkPosZArray.getInt(index);

        ChunkCluster chunkCluster = new ChunkCluster(
                chunkY,
                chunkProvider.provideChunk(chunkX, chunkZ),
                chunkProvider.provideChunk(chunkX + 1, chunkZ),
                chunkProvider.provideChunk(chunkX - 1, chunkZ),
                chunkProvider.provideChunk(chunkX, chunkZ + 1),
                chunkProvider.provideChunk(chunkX, chunkZ - 1));

        int[][][] faceMask = new int[16][16][16];
        boolean[][][] visited = new boolean[16][16][16];

        buildFaceMask(faceMask, chunkCluster);
        regionGrowing(faceMask, visited);
    }

    /**
     * <p>Notice: a chunk is a 16x16x16 cube here, and xyz is a local coordinate inside this cube.</p>
     *
     * Returns whether the target position has a block or not depending on the current pass, opaque or transparent.
     * <br>
     * <br>
     * If at least two arguments equal to -1 or 16 (i.e. the coordinate lies exactly on the edge of the 18x18x18 cube),
     * then this method straight up returns <code>false</code>.
     * <br>
     * <br>
     * If world y-coordinate is less than 0 or greater than 255,
     * then this method straight up returns <code>false</code>.
     *
     * @param x Local x-coordinate. Domain: [-1, 16]. -1 and 16 corresponds to two neighbor chunks.
     * @param y Local y-coordinate. Domain: [-1, 16]. -1 and 16 corresponds to two neighbor chunks.
     * @param z Local z-coordinate. Domain: [-1, 16]. -1 and 16 corresponds to two neighbor chunks.
     */
    boolean blockExists(ChunkCluster chunkCluster, int x, int y, int z) {
        Preconditions.checkArgument(x >= -1 && x <= 16,
                "Argument x=%d must be between [-1, 16].", x);
        Preconditions.checkArgument(y >= -1 && y <= 16,
                "Argument y=%d must be between [-1, 16].", y);
        Preconditions.checkArgument(z >= -1 && z <= 16,
                "Argument z=%d must be between [-1, 16].", z);

        if ((x == -1 || x == 16) && (y == -1 || y == 16) && (z == -1 || z == 16)) {
            return false;
        }
        if ((x == -1 || x == 16) && (y == -1 || y == 16)) {
            return false;
        }
        if ((x == -1 || x == 16) && (z == -1 || z == 16)) {
            return false;
        }
        if ((y == -1 || y == 16) && (z == -1 || z == 16)) {
            return false;
        }
        int yWorldCoord = chunkCluster.chunkY * 16;
        if (yWorldCoord + y < 0 || yWorldCoord + y > 255) {
            return false;
        }

        IBlockState result = null;
        if (x == -1) {
            result = chunkCluster.xMinus.getBlockState(15, yWorldCoord + y, z);
        } else if (x == 16) {
            result = chunkCluster.xPlus.getBlockState(0, yWorldCoord + y, z);
        } else if (z == -1) {
            result = chunkCluster.zMinus.getBlockState(x, yWorldCoord + y, 15);
        } else if (z == 16) {
            result = chunkCluster.zPlus.getBlockState(x, yWorldCoord + y, 0);
        } else if (y == -1 || y == 16) {
            result = chunkCluster.center.getBlockState(x, yWorldCoord + y, z);
        } else {
            // exactly inside the 16x16x16 cube
            result = chunkCluster.center.getBlockState(x, yWorldCoord + y, z);
        }

        Preconditions.checkNotNull(result); // impossible to be null
        Preconditions.checkNotNull(Blocks.AIR);

        if (result == Blocks.AIR.getDefaultState()) {
            return false;
        }

        if (pass == 0) {
            return result.isOpaqueCube();
        } else if (pass == 1) {
            return result.isTranslucent();
        }

        return false; // impossible
    }

    void buildFaceMask(int[][][] faceMask, ChunkCluster chunkCluster) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    faceMask[x][y][z] = 0;
                    if (blockExists(chunkCluster, x, y, z)) {
                        for (int i = 0; i < FACE_DIRS.size(); i++) {
                            Vector3i dir = FACE_DIRS.get(i);
                            int mask = 1 << (5 - i);
                            // neighbor doesn't exist -> face exists
                            if (!blockExists(chunkCluster, x + dir.x, y + dir.y, z + dir.z)) {
                                faceMask[x][y][z] |= mask;
                            }
                        }
                    }
                }
            }
        }
    }

    Vector3f dominantNormal(int faceMask) {
        float x = 0f, y = 0f, z = 0f;
        if ((faceMask & FACE_X_POS) != 0 && (faceMask & FACE_X_NEG) != 0) {
            x += 2f;
        } else {
            if ((faceMask & FACE_X_POS) != 0) {
                x += 1f;
            }
            if ((faceMask & FACE_X_NEG) != 0) {
                x -= 1f;
            }
        }
        if ((faceMask & FACE_Y_POS) != 0 && (faceMask & FACE_Y_NEG) != 0) {
            y += 2f;
        } else {
            if ((faceMask & FACE_Y_POS) != 0) {
                y += 1f;
            }
            if ((faceMask & FACE_Y_NEG) != 0) {
                y -= 1f;
            }
        }
        if ((faceMask & FACE_Z_POS) != 0 && (faceMask & FACE_Z_NEG) != 0) {
            z += 2f;
        } else {
            if ((faceMask & FACE_Z_POS) != 0) {
                z += 1f;
            }
            if ((faceMask & FACE_Z_NEG) != 0) {
                z -= 1f;
            }
        }
        return (new Vector3f(x, y, z)).normalize();
    }

    void regionGrowing(int[][][] faceMask, boolean[][][] visited) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {

                    if (faceMask[x][y][z] == 0) {
                        continue;
                    }
                    if (visited[x][y][z]) {
                        continue;
                    }

                    Block voxel = new Block(x, y, z, faceMask[x][y][z]);

                    List<Block> cluster = new ArrayList<>();
                    Deque<Block> queue = new ArrayDeque<>();

                    cluster.add(voxel);
                    queue.offerLast(voxel);
                    visited[x][y][z] = true;

                    Vector3f meshletNormal = dominantNormal(faceMask[x][y][z]);

                    Block v;
                    while ((v = queue.pollFirst()) != null) {
                        for (Vector3i dir : FACE_DIRS) {
                            int nx = v.position.x + dir.x;
                            int ny = v.position.y + dir.y;
                            int nz = v.position.z + dir.z;
                            // ignore if it's out of the bounds
                            if (nx == -1 || nx == 16 || ny == -1 || ny == 16 || nz == -1 || nz == 16) {
                                continue;
                            }
                            // ignore if it's not the surface
                            if (faceMask[nx][ny][nz] == 0) {
                                continue;
                            }
                            if (visited[nx][ny][nz]) {
                                continue;
                            }

                            Vector3f voxelNormal = dominantNormal(faceMask[nx][ny][nz]);

                            if (meshletNormal.dot(voxelNormal) < Math.cos(MESHLET_MAX_ANGLE)) {
                                continue;
                            }
                            if (cluster.size() >= MESHLET_MAX_SIZE) {
                                continue;
                            }

                            Block newVoxel = new Block(nx, ny, nz, faceMask[nx][ny][nz]);
                            cluster.add(newVoxel);
                            queue.offerLast(newVoxel);
                            visited[nx][ny][nz] = true;

                            float n = cluster.size();
                            meshletNormal = meshletNormal
                                    .mul((n - 1f) / n)
                                    .add(voxelNormal.mul(1f / n))
                                    .normalize();
                        }
                    }

                    gizmosManager.addMeshlet(new Meshlet(cluster));
                }
            }
        }
    }
}

package com.cleanroommc.kirino.engine.render.gizmos;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.geometry.Block;
import com.cleanroommc.kirino.engine.render.geometry.Meshlet;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.resource.UploadStrategy;
import com.cleanroommc.kirino.engine.render.resource.builder.MeshTicketBuilder;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;
import com.cleanroommc.kirino.gl.vao.attribute.Slot;
import com.cleanroommc.kirino.gl.vao.attribute.Stride;
import com.cleanroommc.kirino.gl.vao.attribute.Type;
import com.google.common.base.Preconditions;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class GizmosManager {
    private final GraphicResourceManager graphicResourceManager;

    private record BlockSurface(float x, float y, float z, int faceMask, int color) {
    }

    private final ConcurrentLinkedQueue<BlockSurface> blockSurfaces = new ConcurrentLinkedQueue<>();

    // test
    private final AtomicInteger counter = new AtomicInteger(0);
    private record BlockRecord(float x, float y, float z, int faceMask) { }
    private final ConcurrentLinkedQueue<BlockRecord> blocks = new ConcurrentLinkedQueue<>();

    // test
    public void addMeshlet(Meshlet meshlet) {
        KirinoCore.LOGGER.info("Added a meshlet (" + meshlet.blocks.size() + " blocks). current meshlet count: " + counter.addAndGet(1) + ", current block count: " + blockSurfaces.size());

        Random random = new Random();
        Color color = new Color(random.nextFloat(), random.nextFloat(), random.nextFloat(), 0.5f);
        for (Block block : meshlet.blocks) {
            StringBuilder face = new StringBuilder();
            face.append((block.faceMask & FACE_X_POS) != 0 ? "1" : "0")
                    .append((block.faceMask & FACE_X_NEG) != 0 ? "1" : "0")
                    .append((block.faceMask & FACE_Y_POS) != 0 ? "1" : "0")
                    .append((block.faceMask & FACE_Y_NEG) != 0 ? "1" : "0")
                    .append((block.faceMask & FACE_Z_POS) != 0 ? "1" : "0")
                    .append((block.faceMask & FACE_Z_NEG) != 0 ? "1" : "0");

            KirinoCore.LOGGER.info("    block pos: " + block.position.x + ", " + block.position.y + ", " + block.position.z + ", " + face);

            if (blocks.contains(new BlockRecord(block.position.x, block.position.y, block.position.z, block.faceMask))) {
                addBlockSurface(block.position.x, block.position.y, block.position.z, block.faceMask, Color.RED.getRGB());
            } else {
                blocks.add(new BlockRecord(block.position.x, block.position.y, block.position.z, block.faceMask));
                addBlockSurface(block.position.x, block.position.y, block.position.z, block.faceMask, color.getRGB());
            }
        }
    }

    public void addBlockSurface(float x, float y, float z, int faceMask, int color) {
        blockSurfaces.add(new BlockSurface(x, y, z, faceMask, color));
    }

    public void clearBlockSurfaces() {
        blockSurfaces.clear();
    }

    public GizmosManager(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    private final static AttributeLayout ATTRIBUTE_LAYOUT;

    static {
        ATTRIBUTE_LAYOUT = new AttributeLayout();
        ATTRIBUTE_LAYOUT.push(new Stride(16)
                .push(new Slot(Type.FLOAT, 3))
                .push(new Slot(Type.UNSIGNED_BYTE, 4).setNormalize(true)));
    }

    private static final int FACE_X_POS = 0b100000;
    private static final int FACE_X_NEG = 0b010000;
    private static final int FACE_Y_POS = 0b001000;
    private static final int FACE_Y_NEG = 0b000100;
    private static final int FACE_Z_POS = 0b000010;
    private static final int FACE_Z_NEG = 0b000001;

    private void buildBlockFaces(MeshTicketBuilder builder, float x, float y, float z, int faceMask, int color) {
        int faceCount = Integer.bitCount(faceMask);
        Preconditions.checkArgument(faceCount > 0, "Must have at least one face to draw.");

        byte a = (byte) ((color >> 24) & 0xFF);
        byte r;
        byte g;
        byte b;
        Color color1 = new Color(color, true);

        ByteBuffer vboData = BufferUtils.createByteBuffer(faceCount * 4 * ATTRIBUTE_LAYOUT.getFirstStride().getSize());
        ByteBuffer eboData = BufferUtils.createByteBuffer(faceCount * 6);

        int vertexBase = 0;

        if ((faceMask & FACE_X_POS) != 0) {
            float factor = 1.1f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x + 1f).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);

            eboData.put((byte) vertexBase).put((byte) (vertexBase + 1)).put((byte) (vertexBase + 2));
            eboData.put((byte) vertexBase).put((byte) (vertexBase + 2)).put((byte) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_X_NEG) != 0) {
            float factor = 0.8f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);

            eboData.put((byte) vertexBase).put((byte) (vertexBase + 1)).put((byte) (vertexBase + 2));
            eboData.put((byte) vertexBase).put((byte) (vertexBase + 2)).put((byte) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Y_POS) != 0) {
            float factor = 1.2f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);

            eboData.put((byte) (vertexBase)).put((byte) (vertexBase + 1)).put((byte) (vertexBase + 2));
            eboData.put((byte) vertexBase).put((byte) (vertexBase + 2)).put((byte) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Y_NEG) != 0) {
            float factor = 0.6f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);

            eboData.put((byte) vertexBase).put((byte) (vertexBase + 1)).put((byte) (vertexBase + 2));
            eboData.put((byte) vertexBase).put((byte) (vertexBase + 2)).put((byte) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Z_POS) != 0) {
            float factor = 1.05f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y).putFloat(z + 1f);
            vboData.put(r).put(g).put(b).put(a);

            eboData.put((byte) vertexBase).put((byte) (vertexBase + 1)).put((byte) (vertexBase + 2));
            eboData.put((byte) vertexBase).put((byte) (vertexBase + 2)).put((byte) (vertexBase + 3));
            vertexBase += 4;
        }

        if ((faceMask & FACE_Z_NEG) != 0) {
            float factor = 0.85f;
            r = (byte) (int) Math.min(255, Math.max(0, color1.getRed() * factor));
            g = (byte) (int) Math.min(255, Math.max(0, color1.getGreen() * factor));
            b = (byte) (int) Math.min(255, Math.max(0, color1.getBlue() * factor));

            vboData.putFloat(x + 1f).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x + 1f).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y + 1f).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);
            vboData.putFloat(x).putFloat(y).putFloat(z);
            vboData.put(r).put(g).put(b).put(a);

            eboData.put((byte) vertexBase).put((byte) (vertexBase + 1)).put((byte) (vertexBase + 2));
            eboData.put((byte) vertexBase).put((byte) (vertexBase + 2)).put((byte) (vertexBase + 3));
        }

        vboData.flip();
        eboData.flip();

        builder.build(vboData, eboData, ATTRIBUTE_LAYOUT);
    }

    public List<HighLevelDC> getDrawCommands() {
        List<HighLevelDC> list = new ArrayList<>();

        Iterator<BlockSurface> iterator = blockSurfaces.iterator();
        int index = 0;

        while (iterator.hasNext()) {
            BlockSurface blockSurface = iterator.next();
            String id = "block_surface_mesh_" + Objects.hash(index, blockSurface);

            graphicResourceManager.requestMeshTicket(id, UploadStrategy.TEMPORARY).ifPresent(builder -> {
                buildBlockFaces(builder, blockSurface.x, blockSurface.y, blockSurface.z, blockSurface.faceMask, blockSurface.color);
                graphicResourceManager.submitMeshTicket(builder);
            });

            list.add(HighLevelDC.passInternal()
                    .meshTicketID(id)
                    .mode(GL11.GL_TRIANGLES)
                    .elementType(GL11.GL_UNSIGNED_BYTE)
                    .build());

            index++;
        }

        return list;
    }
}

package com.cleanroommc.kirino.engine.render.pipeline.command;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.resource.GResourceTicket;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.gl.buffer.GLBuffer;
import com.cleanroommc.kirino.gl.buffer.IDBView;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;

public class DrawQueue {
    private final GraphicResourceManager graphicResourceManager;
    private final Deque<IDrawCommand> deque = new ArrayDeque<>();

    public DrawQueue(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    public void enqueue(IDrawCommand command) {
        deque.offerLast(command);
    }

    @Nullable
    public IDrawCommand dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }

    /**
     * Compiles everything into {@link LowLevelDC}s.
     * After calling this method, every element in this draw queue is guaranteed to be a {@link LowLevelDC}.
     * High-level commands are converted in-place. Low-level commands remain unchanged.
     *
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue compile() {
        List<IDrawCommand> baked = new ArrayList<>();

        IDrawCommand drawCommand;
        while ((drawCommand = dequeue()) != null) {
            if (drawCommand instanceof LowLevelDC) {
                baked.add(drawCommand);
            } else if (drawCommand instanceof HighLevelDC highLevelDC) {
                Optional<GResourceTicket<MeshPayload, MeshReceipt>> optional = graphicResourceManager.getMeshTicket(highLevelDC.meshTicketID);
                if (optional.isEmpty()) {
                    continue;
                }
                if (!optional.get().isResourceReady() || optional.get().isExpired()) {
                    continue;
                }

                MeshReceipt meshReceipt = optional.get().getReceipt();

                LowLevelDC.MultiElementIndirectUnitBuilder builder = LowLevelDC.multiElementIndirectUnit().vao(meshReceipt.vao);
                builder.mode(highLevelDC.mode).elementType(highLevelDC.elementType);

                int elementSize = 0;
                if (highLevelDC.elementType == GL11.GL_UNSIGNED_BYTE) {
                    elementSize = 1;
                } else if (highLevelDC.elementType == GL11.GL_UNSIGNED_SHORT) {
                    elementSize = 2;
                } else if (highLevelDC.elementType == GL11.GL_UNSIGNED_INT) {
                    elementSize = 4;
                }

                builder.indicesCount(meshReceipt.eboLength / elementSize);
                builder.instanceCount(1);
                builder.eboFirstIndex(meshReceipt.eboOffset / elementSize);
                builder.baseVertex(meshReceipt.baseVertex);
                builder.baseInstance(0);

                baked.add(builder.build());
            }
        }

//        KirinoCore.LOGGER.info("compile count: " + baked.size());

        deque.clear();
        deque.addAll(baked);
        return this;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All elements in this {@link DrawQueue} is a {@link LowLevelDC}</li>
     * </ul>
     *
     * It combines and simplifies {@link LowLevelDC}s, especially combines commands into <code>MULTI_ELEMENTS_INDIRECT</code> command.
     * Usually it's called after {@link #compile()} which compiles everything into {@link LowLevelDC}s.
     *
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue simplify() {
        Map<VAOKey, List<LowLevelDC>> grouped = new HashMap<>();

        IDrawCommand drawCommand;
        while ((drawCommand = dequeue()) != null) {
            LowLevelDC lowLevelDC = (LowLevelDC) drawCommand;
            VAOKey key = new VAOKey(lowLevelDC.vao, lowLevelDC.mode, lowLevelDC.elementType);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(lowLevelDC);
        }

        deque.clear();

        List<IDrawCommand> baked = new ArrayList<>();

        for (Map.Entry<VAOKey, List<LowLevelDC>> entry : grouped.entrySet()) {
            List<LowLevelDC> units = new ArrayList<>();
            for (LowLevelDC lowLevelDC : entry.getValue()) {
                if (lowLevelDC.type == LowLevelDC.DrawType.MULTI_ELEMENTS_INDIRECT_UNIT) {
                    units.add(lowLevelDC);
                } else {
                    baked.add(lowLevelDC);
                }
            }

            // combine units and upload idb
            if (units.isEmpty()) {
                continue;
            }

            int idbBufferSize = units.size() * 5 * Integer.BYTES; // 5 ints per command

            try (MemoryStack stack = MemoryStack.stackPush()) {
                ByteBuffer byteBuffer = stack.malloc(4, idbBufferSize);

                IntBuffer intView = byteBuffer.asIntBuffer();
                for (LowLevelDC lowLevelDC : units) {
                    intView.put(new int[] {
                            lowLevelDC.idIndicesCount,
                            lowLevelDC.idInstanceCount,
                            lowLevelDC.idEboFirstIndex,
                            lowLevelDC.idBaseVertex,
                            lowLevelDC.idBaseInstance
                    });
                }

                byteBuffer.limit(idbBufferSize);
                byteBuffer.position(0);

                idbView.bind();
                idbView.uploadDirectly(byteBuffer);
                idbView.bind(0);
            }

            LowLevelDC.MultiElementIndirectBuilder builder = LowLevelDC.multiElementIndirect().vao(entry.getKey().vao).idb(idbView.bufferID);
            builder.mode(entry.getKey().mode).elementType(entry.getKey().elementType);
            builder.idbOffset(0);
            builder.idbStride(5 * Integer.BYTES);
            builder.instanceCount(units.size());

            baked.add(builder.build());
        }

//        KirinoCore.LOGGER.info("simplify count: " + baked.size());

        deque.addAll(baked);
        return this;
    }

    // temp
    private IDBView idbView = new IDBView(new GLBuffer());

    private record VAOKey(int vao, int mode, int elementType) {
    }
}

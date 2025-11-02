package com.cleanroommc.kirino.engine.render.pipeline.draw;

import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.HighLevelDC;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.IDrawCommand;
import com.cleanroommc.kirino.engine.render.pipeline.draw.cmd.LowLevelDC;
import com.cleanroommc.kirino.engine.render.resource.GResourceTicket;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class DrawQueue {
    private final Deque<IDrawCommand> deque = new ArrayDeque<>();

    public DrawQueue() {
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
     * @param graphicResourceManager The graphic resource manager
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue compile(GraphicResourceManager graphicResourceManager) {
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

        deque.clear();
        deque.addAll(baked);
        return this;
    }

    /**
     * Only used by {@link #simplify(IndirectDrawBufferGenerator)}.
     */
    private record VAOKey(int vao, int mode, int elementType) {
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>Every element in this {@link DrawQueue} is a {@link LowLevelDC}</li>
     * </ul>
     *
     * It combines and simplifies {@link LowLevelDC}s, especially combines commands into <code>MULTI_ELEMENTS_INDIRECT</code> command.
     * Usually it's called after {@link #compile(GraphicResourceManager)} which compiles everything into {@link LowLevelDC}s.
     *
     * @param idbGenerator The indirect draw buffer manager
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue simplify(IndirectDrawBufferGenerator idbGenerator) {
        Map<VAOKey, List<LowLevelDC>> grouped = new HashMap<>();

        IDrawCommand drawCommand;
        while ((drawCommand = dequeue()) != null) {
            LowLevelDC lowLevelDC = (LowLevelDC) drawCommand;
            VAOKey key = new VAOKey(lowLevelDC.vao, lowLevelDC.mode, lowLevelDC.elementType);
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(lowLevelDC);
        }

        deque.clear();
        idbGenerator.reset();

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

            // combine units and upload to idb
            if (units.isEmpty()) {
                continue;
            }

            baked.add(idbGenerator.generate(units, entry.getKey().vao, entry.getKey().mode, entry.getKey().elementType));
        }

        deque.addAll(baked);
        return this;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>Every element in this {@link DrawQueue} is a {@link LowLevelDC}</li>
     * </ul>
     *
     * It sorts all {@link LowLevelDC}s, which is the final stage of command processing.
     *
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue sort() {

        return this;
    }
}

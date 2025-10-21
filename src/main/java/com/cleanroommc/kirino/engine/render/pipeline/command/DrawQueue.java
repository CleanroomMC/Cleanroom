package com.cleanroommc.kirino.engine.render.pipeline.command;

import com.cleanroommc.kirino.KirinoCore;
import com.cleanroommc.kirino.engine.render.resource.GResourceTicket;
import com.cleanroommc.kirino.engine.render.resource.GraphicResourceManager;
import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;

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
                // todo: use multi draw indirect unit command if more than one commands have the same vao

                Optional<GResourceTicket<MeshPayload, MeshReceipt>> optional = graphicResourceManager.getMeshTicket(highLevelDC.meshTicketID);
                if (optional.isEmpty()) {
                    continue;
                }
                if (!optional.get().isResourceReady() || optional.get().isExpired()) {
                    continue;
                }

                MeshReceipt meshReceipt = optional.get().getReceipt();

                LowLevelDC.ElementBuilder element = LowLevelDC.element().vao(meshReceipt.vao);
                element.mode(highLevelDC.mode).elementType(highLevelDC.elementType);

                int elementSize = 0;
                if (highLevelDC.elementType == GL11.GL_UNSIGNED_BYTE) {
                    elementSize = 1;
                } else if (highLevelDC.elementType == GL11.GL_UNSIGNED_SHORT) {
                    elementSize = 2;
                } else if (highLevelDC.elementType == GL11.GL_UNSIGNED_INT) {
                    elementSize = 4;
                }

                element.indicesCount(meshReceipt.eboLength / elementSize);
                element.eboOffset(meshReceipt.eboOffset);

                baked.add(element.build());
            }
        }

        KirinoCore.LOGGER.info("baked draw queue size: " + baked.size());

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

        return this;
    }
}

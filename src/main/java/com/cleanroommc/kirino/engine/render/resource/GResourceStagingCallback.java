package com.cleanroommc.kirino.engine.render.resource;

import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.engine.render.staging.IStagingCallback;
import com.cleanroommc.kirino.engine.render.staging.StagingContext;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVBOHandle;

import java.util.HashMap;
import java.util.Map;

public final class GResourceStagingCallback implements IStagingCallback {
    private final GraphicResourceManager graphicResourceManager;

    public GResourceStagingCallback(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(StagingContext context) {
        Map<String, GResourceTicket<?, ?>> map = graphicResourceManager.resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());
        for (GResourceTicket<?, ?> ticket : map.values()) {
            GResourceTicket<MeshPayload, MeshReceipt> meshTicket = (GResourceTicket<MeshPayload, MeshReceipt>) ticket;

            // must upload every time cuz i use temporary handles to test
            //if (meshTicket.status == GResourceStatus.CPU_ONLY) {
                // todo: combine vbo ebo if possible + make use of RenderObj2BufMorphism to compute indices automatically

                // todo: persistent upload + double buffering results in a deferred ticket status change (use UPLOADING instead)

                // todo: hint to control temp/persistent upload ?

                MeshPayload meshPayload = meshTicket.payload.getPayload();
                MeshReceipt meshReceipt = meshTicket.receipt.getReceipt();

                TemporaryVBOHandle vboHandle = context.getTemporaryVBO(meshPayload.vboByteBuffer.remaining()).write(0, meshPayload.vboByteBuffer);
                TemporaryEBOHandle eboHandle = context.getTemporaryEBO(meshPayload.eboByteBuffer.remaining()).write(0, meshPayload.eboByteBuffer);
                meshReceipt.vao = context.getTemporaryVAO(meshPayload.attributeLayout, eboHandle, vboHandle).getVaoID();
                meshReceipt.eboOffset = 0;
                meshReceipt.eboLength = meshPayload.eboByteBuffer.remaining();

                // todo: release payload

                meshTicket.status = GResourceStatus.GPU_READY;
            //}

            meshTicket.tickFrame();
        }

        // todo: texture tickets
    }
}

package com.cleanroommc.kirino.engine.render.resource;

import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.engine.render.staging.IStagingCallback;
import com.cleanroommc.kirino.engine.render.staging.StagingContext;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryEBOHandle;
import com.cleanroommc.kirino.engine.render.staging.handle.TemporaryVBOHandle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GResourceStagingCallback implements IStagingCallback {
    private final GraphicResourceManager graphicResourceManager;

    public GResourceStagingCallback(GraphicResourceManager graphicResourceManager) {
        this.graphicResourceManager = graphicResourceManager;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(StagingContext context) {
        // handle mesh tickets
        Map<String, GResourceTicket<?, ?>> map = graphicResourceManager.resourceTickets.computeIfAbsent(GResourceType.MESH, k -> new HashMap<>());

        List<String> toBeRemoved = new ArrayList<>();

        for (Map.Entry<String, GResourceTicket<?, ?>> entry : map.entrySet()) {
            GResourceTicket<MeshPayload, MeshReceipt> meshTicket = (GResourceTicket<MeshPayload, MeshReceipt>) entry.getValue();

            if (meshTicket.isExpired()) {
                toBeRemoved.add(entry.getKey());
                meshTicket.payload.release();
                continue;
            }

            // all temporary handles will be disposed every frame
            if (meshTicket.uploadStrategy == UploadStrategy.TEMPORARY) {
                // we don't release payloads for temporary tickets
                // so their payload is now cpu only
                meshTicket.status = GResourceStatus.CPU_ONLY;
            }

            if (meshTicket.status == GResourceStatus.CPU_ONLY) {
                if (meshTicket.uploadStrategy == UploadStrategy.PERSISTENT) {
                    // todo: combine vbo ebo if possible + make use of RenderObj2BufMorphism to compute indices automatically

                    // todo: persistent upload + double buffering results in a deferred ticket status change (use UPLOADING instead)

                } else if (meshTicket.uploadStrategy == UploadStrategy.TEMPORARY) {
                    MeshPayload meshPayload = meshTicket.payload.getPayload();
                    MeshReceipt meshReceipt = meshTicket.receipt.getReceipt();

                    TemporaryVBOHandle vboHandle = context.getTemporaryVBO(meshPayload.vboByteBuffer.remaining()).write(0, meshPayload.vboByteBuffer);
                    TemporaryEBOHandle eboHandle = context.getTemporaryEBO(meshPayload.eboByteBuffer.remaining()).write(0, meshPayload.eboByteBuffer);
                    meshReceipt.vao = context.getTemporaryVAO(meshPayload.attributeLayout, eboHandle, vboHandle).getVaoID();
                    meshReceipt.eboOffset = 0;
                    meshReceipt.eboLength = meshPayload.eboByteBuffer.remaining();

                    // dont release payloads here

                    meshTicket.status = GResourceStatus.GPU_READY;
                }
            }

            meshTicket.tickFrame();
        }

        toBeRemoved.forEach(map::remove);

        // todo: handle texture tickets
    }
}

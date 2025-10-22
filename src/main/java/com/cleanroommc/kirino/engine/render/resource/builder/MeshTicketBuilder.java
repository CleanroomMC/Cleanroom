package com.cleanroommc.kirino.engine.render.resource.builder;

import com.cleanroommc.kirino.engine.render.resource.GResourceTicket;
import com.cleanroommc.kirino.engine.render.resource.UploadStrategy;
import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.gl.vao.attribute.AttributeLayout;

import java.nio.ByteBuffer;

public final class MeshTicketBuilder {
    private final String meshID;
    private final UploadStrategy uploadStrategy;
    private GResourceTicket<MeshPayload, MeshReceipt> ticket;
    private boolean built = false;

    public MeshTicketBuilder(String meshID, UploadStrategy uploadStrategy) {
        this.meshID = meshID;
        this.uploadStrategy = uploadStrategy;
    }

    public String getMeshID() {
        return meshID;
    }

    public UploadStrategy getUploadStrategy() {
        return uploadStrategy;
    }

    public GResourceTicket<MeshPayload, MeshReceipt> getTicket() {
        return ticket;
    }

    public boolean isBuilt() {
        return built;
    }

    public void build(ByteBuffer vboByteBuffer, ByteBuffer eboByteBuffer, AttributeLayout attributeLayout) {
        if (!built) {
            MeshPayload payload = new MeshPayload();
            MeshReceipt receipt = new MeshReceipt();

            payload.vboByteBuffer = vboByteBuffer;
            payload.eboByteBuffer = eboByteBuffer;
            payload.attributeLayout = attributeLayout;

            ticket = new GResourceTicket<>(uploadStrategy, MeshPayload.class, MeshReceipt.class, payload, receipt);
            built = true;
        }
    }
}

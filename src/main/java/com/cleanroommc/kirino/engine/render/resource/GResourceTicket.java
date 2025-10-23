package com.cleanroommc.kirino.engine.render.resource;

import com.cleanroommc.kirino.engine.render.resource.payload.IResourcePayload;
import com.cleanroommc.kirino.engine.render.resource.payload.MeshPayload;
import com.cleanroommc.kirino.engine.render.resource.payload.TexturePayload;
import com.cleanroommc.kirino.engine.render.resource.receipt.IResourceReceipt;
import com.cleanroommc.kirino.engine.render.resource.receipt.MeshReceipt;
import com.cleanroommc.kirino.engine.render.resource.receipt.TextureReceipt;
import com.google.common.base.Preconditions;

public class GResourceTicket<TP extends IResourcePayload<TP>, TR extends IResourceReceipt<TR>> {
    public final GResourceType type;
    public final UploadStrategy uploadStrategy;
    protected GResourceStatus status;
    protected final IResourcePayload<TP> payload;
    protected final IResourceReceipt<TR> receipt;

    private static final int DEFAULT_LIFE = 2;
    protected int lifeFrame = DEFAULT_LIFE;

    public final TR getReceipt() {
        return receipt.getReceipt();
    }

    public final boolean isResourceReady() {
        return status == GResourceStatus.GPU_READY;
    }

    public final boolean isExpired() {
        if (status == GResourceStatus.UPLOADING) {
            return false;
        } else {
            return lifeFrame <= 0 || status == GResourceStatus.TO_BE_DESTROYED;
        }
    }

    protected final void tickFrame() {
        if (status == GResourceStatus.GPU_READY) {
            lifeFrame--;
        }
    }

    protected final void keepAlive() {
        lifeFrame = DEFAULT_LIFE;
    }

    public GResourceTicket(UploadStrategy uploadStrategy, Class<TP> payloadClass, Class<TR> receiptClass, TP payload, TR receipt) {
        Preconditions.checkArgument(payloadClass == MeshPayload.class || payloadClass == TexturePayload.class,
                "Argument payloadClass " + payloadClass.getName() + " is invalid. Must use a built-in payload class.");
        Preconditions.checkArgument(receiptClass == MeshReceipt.class || receiptClass == TextureReceipt.class,
                "Argument receiptClass " + receiptClass.getName() + " is invalid. Must use a built-in receipt class.");

        if (payloadClass == MeshPayload.class) {
            type = GResourceType.MESH;
        } else if (payloadClass == TexturePayload.class) {
            type = GResourceType.TEXTURE;
        } else {
            type = null; // impossible
        }

        GResourceType tempType;
        if (receiptClass == MeshReceipt.class) {
            tempType = GResourceType.MESH;
        } else if (receiptClass == TextureReceipt.class) {
            tempType = GResourceType.TEXTURE;
        } else {
            tempType = null; // impossible
        }

        Preconditions.checkArgument(type == tempType,
                "Arguments payloadClass and receiptClass refer to a different resource type.");

        status = GResourceStatus.CPU_ONLY;
        this.payload = payload;
        this.receipt = receipt;
        this.uploadStrategy = uploadStrategy;
    }
}

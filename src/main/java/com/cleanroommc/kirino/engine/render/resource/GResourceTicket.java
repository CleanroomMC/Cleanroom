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
    protected GResourceStatus status;
    protected final IResourcePayload<TP> payload;
    protected final IResourceReceipt<TR> receipt;
    protected int lifeFrame;

    protected GResourceTicket(int lifeFrame, Class<TP> payloadClass, Class<TR> receiptClass, TP payload, TR receipt) {
        Preconditions.checkArgument(payloadClass == MeshPayload.class || payloadClass == TexturePayload.class,
                "Argument payloadClass " + payloadClass.getName() + " is invalid. Must use a built-in payload class.");
        Preconditions.checkArgument(receiptClass == MeshReceipt.class || receiptClass == TextureReceipt.class,
                "Argument receiptClass " + receiptClass.getName() + " is invalid. Must use a built-in receipt class.");

        this.lifeFrame = lifeFrame;

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
    }
}

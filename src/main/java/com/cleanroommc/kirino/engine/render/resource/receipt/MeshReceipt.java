package com.cleanroommc.kirino.engine.render.resource.receipt;

import org.jspecify.annotations.NonNull;

public class MeshReceipt implements IResourceReceipt<MeshReceipt> {
    public MeshReceipt() {
    }

    public int vao;
    public int eboOffset;
    public int eboLength;

    @NonNull
    @Override
    public MeshReceipt getReceipt() {
        return this;
    }
}

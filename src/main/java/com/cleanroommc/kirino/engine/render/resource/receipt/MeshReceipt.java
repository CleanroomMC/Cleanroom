package com.cleanroommc.kirino.engine.render.resource.receipt;

import org.jspecify.annotations.NonNull;

public class MeshReceipt implements IResourceReceipt<MeshReceipt> {
    public MeshReceipt() {
    }

    private int vao;

    @NonNull
    @Override
    public MeshReceipt getReceipt() {
        return this;
    }
}

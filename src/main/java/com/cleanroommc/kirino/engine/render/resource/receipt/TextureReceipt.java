package com.cleanroommc.kirino.engine.render.resource.receipt;

import org.jspecify.annotations.NonNull;

public class TextureReceipt implements IResourceReceipt<TextureReceipt> {
    public TextureReceipt() {
    }

    @NonNull
    @Override
    public TextureReceipt getReceipt() {
        return this;
    }
}

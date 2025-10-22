package com.cleanroommc.kirino.engine.render.resource.payload;

import org.jspecify.annotations.NonNull;

public class TexturePayload implements IResourcePayload<TexturePayload> {
    public TexturePayload() {
    }

    @NonNull
    @Override
    public TexturePayload getPayload() {
        return this;
    }

    private boolean released = false;

    @Override
    public void release() {
        if (!released){

            released = true;
        }
    }
}

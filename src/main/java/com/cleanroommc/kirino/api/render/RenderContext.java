package com.cleanroommc.kirino.api.render;

public class RenderContext<T extends IRenderCommand> {
    public final RenderQueueBuilder<T> builder;

    public RenderContext(RenderQueueBuilder<T> builder) {
        this.builder = builder;
    }
}

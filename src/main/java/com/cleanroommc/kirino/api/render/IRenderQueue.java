package com.cleanroommc.kirino.api.render;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public sealed interface IRenderQueue<T extends IRenderCommand> permits RenderQueue, RenderQueueSpecial {
    void enqueue(@NonNull T command);
    @Nullable T dequeue();
    void clear();
}

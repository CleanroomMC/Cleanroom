package com.cleanroommc.kirino.api.render;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;

public class RenderQueueBuilder<T extends IRenderCommand> {
    private final IRenderQueue<T> renderQueue;
    private boolean lock = false;

    @SuppressWarnings("unchecked")
    public RenderQueueBuilder(Class<T> clazz) {
        Preconditions.checkArgument(clazz != IRenderCommand.class,
                "Argument \"clazz\" must not be the interface IRenderCommand itself.");

        if (clazz == RenderCommand.class) {
            renderQueue = (IRenderQueue<T>) new RenderQueue();
        } else if (clazz == RenderCommandSpecial.class) {
            renderQueue = (IRenderQueue<T>) new RenderQueueSpecial();
        } else {
            renderQueue = null;
        }

        Preconditions.checkNotNull(renderQueue);
    }

    @NonNull
    public RenderQueueBuilder<T> enqueue(@NonNull T renderCommand) {
        Preconditions.checkState(!lock, "Only call this method before emit().");

        renderQueue.enqueue(renderCommand);
        return this;
    }

    @NonNull
    public RenderQueueBuilder<T> dequeue() {
        Preconditions.checkState(!lock, "Only call this method before emit().");

        renderQueue.dequeue();
        return this;
    }

    @NonNull
    public IRenderQueue<T> emit() {
        Preconditions.checkState(!lock, "Only call this method before emit().");

        lock = true;
        return renderQueue;
    }
}

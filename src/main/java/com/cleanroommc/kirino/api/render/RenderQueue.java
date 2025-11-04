package com.cleanroommc.kirino.api.render;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

final class RenderQueue implements IRenderQueue<RenderCommand> {
    private final Deque<RenderCommand> deque = new ArrayDeque<>();

    RenderQueue() {
    }

    public void enqueue(@NonNull RenderCommand command) {
        deque.offerLast(command);
    }

    @Nullable
    public RenderCommand dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }
}

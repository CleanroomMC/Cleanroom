package com.cleanroommc.kirino.api.render;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

final class RenderQueueSpecial implements IRenderQueue<RenderCommandSpecial> {
    private final Deque<RenderCommandSpecial> deque = new ArrayDeque<>();

    RenderQueueSpecial() {
    }

    public void enqueue(@NonNull RenderCommandSpecial command) {
        deque.offerLast(command);
    }

    @Nullable
    public RenderCommandSpecial dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }
}

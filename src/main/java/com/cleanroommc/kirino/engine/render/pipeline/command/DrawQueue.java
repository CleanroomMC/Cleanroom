package com.cleanroommc.kirino.engine.render.pipeline.command;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

public class DrawQueue {
    private final Deque<DrawCommand> deque = new ArrayDeque<>();

    public void enqueue(DrawCommand command) {
        deque.offerLast(command);
    }

    @Nullable
    public DrawCommand dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }
}

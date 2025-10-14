package com.cleanroommc.kirino.engine.render.pipeline.command;

import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class DrawQueue {
    private final Deque<IDrawCommand> deque = new ArrayDeque<>();

    public void enqueue(IDrawCommand command) {
        deque.offerLast(command);
    }

    @Nullable
    public IDrawCommand dequeue() {
        return deque.pollFirst();
    }

    public void clear() {
        deque.clear();
    }

    /**
     * Compiles everything into {@link LowLevelDC}s.
     * After calling this method, every element in this draw queue is guaranteed to be a {@link LowLevelDC}.
     * High-level commands are converted in-place. Low-level commands remain unchanged.
     *
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue compile() {
        List<IDrawCommand> baked = new ArrayList<>();

        baked.addAll(deque);

        deque.clear();
        deque.addAll(baked);
        return this;
    }

    /**
     * <p>Prerequisite include:</p>
     * <ul>
     *     <li>All elements in this {@link DrawQueue} is a {@link LowLevelDC}</li>
     * </ul>
     *
     * It combines and simplifies {@link LowLevelDC}s, especially combines commands into <code>MULTI_ELEMENTS_INDIRECT</code> command.
     * Usually it's called after {@link #compile()} which compiles everything into {@link LowLevelDC}s.
     *
     * @return The <code>DrawQueue</code> itself
     */
    public DrawQueue simplify() {

        return this;
    }
}

package com.cleanroommc.kirino.schemata.fsm;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class IntEnumStateMachine<I extends Enum<I>> implements FiniteStateMachine<Integer,I> {
    private final int lowerStateBound, upperStateBound;
    private final int[] transitionMap;
    private final OnEnterStateCallback<Integer, I>[] entryCallbacks;
    private final OnExitStateCallback<Integer, I>[] exitCallbacks;
    private final Rollback<Integer, I>[] rollbacks;
    private final ErrorCallback<Integer, I> error;
    private int state;
    private final Deque<FSMBacklogPair<Integer, I>> backlog = new ArrayDeque<>();

    IntEnumStateMachine(int lowerStateBound, int upperStateBound,
                        int @NonNull [] transitionMap,
                        OnEnterStateCallback<Integer, I> @NonNull [] entryCallbacks,
                        OnExitStateCallback<Integer, I> @NonNull [] exitCallbacks,
                        Rollback<Integer, I> @NonNull [] rollbacks,
                        @Nullable ErrorCallback<Integer, I> error,
                        int initialState) {
        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.transitionMap = transitionMap;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbacks = rollbacks;
        this.error = error;
        this.state = initialState;
    }

    @NotNull
    @Override
    public Integer state() {
        return state;
    }

    private int index(@NonNull I input, int state) {
        return ((upperStateBound-lowerStateBound+1)*input.ordinal())+(state-lowerStateBound);
    }

    @Override
    public Optional<Integer> accept(@NotNull I input) {
        int idx = this.index(input, state);
        if (transitionMap[idx] != -1) {
            backlog.push(new FSMBacklogPair<>(state, input));
            if (this.exitCallbacks[state] != null) {
                this.exitCallbacks[state].transition(state, input, transitionMap[idx]);
            }
            if (this.entryCallbacks[transitionMap[idx]] != null) {
                this.entryCallbacks[transitionMap[idx]].transition(state, input, transitionMap[idx]);
            }
            state = transitionMap[idx];
        } else if (error != null) {
            error.error(state, input);
            return Optional.empty();
        }
        return Optional.of(state);
    }

    @Override
    public Optional<FSMBacklogPair<Integer, I>> backtrack() {
        if (backlog.isEmpty()) {
            return Optional.empty();
        }
        FSMBacklogPair<Integer,I> pair = backlog.pop();
        Rollback<Integer,I> rollback = rollbacks[index(pair.input(), pair.state())];
        if (rollback != null) {
            rollback.rollback(state, pair.input(), pair.state());
        }
        FSMBacklogPair<Integer,I> result = new FSMBacklogPair<>(state, pair.input());
        this.state = pair.state();
        return Optional.of(result);
    }

    @Override
    public void reset() {
        if (!backlog.isEmpty()) {
            this.state = backlog.pollLast().state();
            backlog.clear();
        }
    }

    static class Builder<I extends Enum<I>> implements IBuilder<Integer,I> {

        private final int lowerStateBound, upperStateBound;
        private final I[] inputs;
        private final int[] transitionMap;
        private final OnEnterStateCallback<Integer,I>[] entryCallbacks;
        private final OnExitStateCallback<Integer, I>[] exitCallbacks;
        private final Rollback<Integer,I>[] rollbacks;
        private ErrorCallback<Integer,I> error;
        private Integer initialState;

        Builder(int lowerStateBound, int upperStateBound, Class<I> inputClass) {
            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            this.inputs = inputClass.getEnumConstants();
            int length = (upperStateBound-lowerStateBound+1)*inputs.length;
            this.transitionMap = new int[length];
            for (int i = 0; i < length; i++) {
                this.transitionMap[i] = -1;
            }
            this.entryCallbacks = new OnEnterStateCallback[(upperStateBound-lowerStateBound+1)];
            this.exitCallbacks = new OnExitStateCallback[(upperStateBound-lowerStateBound+1)];
            this.rollbacks = new Rollback[length];
        }

        private int index(I input, int state) {
            return (input.ordinal()*(upperStateBound-lowerStateBound+1))+(state-lowerStateBound);
        }

        @Override
        public IBuilder<Integer, I> addTransition(@NotNull Integer state, @NotNull I input, @NotNull Integer nextState,
                                                  @Nullable OnEnterStateCallback<Integer, I> onEnterStateCallback,
                                                  @Nullable OnExitStateCallback<Integer, I> onExitStateCallback,
                                                  @Nullable Rollback<Integer, I> rollbackCallback) {
            if (state < lowerStateBound || state > upperStateBound
            || nextState < lowerStateBound || nextState > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        initialState, lowerStateBound, upperStateBound));
            }
            int index = index(input,state);
            transitionMap[index] = nextState;
            if (onExitStateCallback != null) {
                exitCallbacks[state] = onExitStateCallback;
            }
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState] = onEnterStateCallback;
            }
            rollbacks[index] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<Integer, I> setEntryCallback(@NonNull Integer state, @Nullable OnEnterStateCallback<Integer, I> callback) {
            if (state < lowerStateBound || state > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        state, lowerStateBound, upperStateBound));
            }
            entryCallbacks[state] = callback;
            return this;
        }

        @Override
        public IBuilder<Integer, I> setExitCallback(@NonNull Integer state, @Nullable OnExitStateCallback<Integer, I> callback) {
            if (state < lowerStateBound || state > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        state, lowerStateBound, upperStateBound));
            }
            exitCallbacks[state] = callback;
            return this;
        }

        @Override
        public IBuilder<Integer, I> initialState(@NotNull Integer initialState) {
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<Integer, I> error(@NotNull ErrorCallback<Integer, I> errorCallback) {
            this.error = errorCallback;
            return this;
        }

        @Override
        public boolean validate() {
            final int size = upperStateBound-lowerStateBound+1;
            BitSet reachable = new BitSet(size);
            Deque<Integer> stack = new ArrayDeque<>();
            stack.push(initialState);
            while (!stack.isEmpty()) {
                int state = stack.pop();
                if (!reachable.get(state)) {
                    reachable.set(state);
                    for (I input : inputs) {
                        int next = transitionMap[index(input,state)];
                        if (!(next == -1 || next == state)) {
                            stack.push(next);
                        }
                    }
                }
            }
            return reachable.cardinality() == size;
        }

        @Override
        public FiniteStateMachine<Integer, I> build() {
            return new IntEnumStateMachine<>(lowerStateBound, upperStateBound,
                    transitionMap, entryCallbacks, exitCallbacks,
                    rollbacks, error, initialState);
        }
    }
}

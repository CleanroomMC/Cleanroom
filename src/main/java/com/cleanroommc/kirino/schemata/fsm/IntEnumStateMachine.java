package com.cleanroommc.kirino.schemata.fsm;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

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

    @Override
    public Integer state() {
        return state;
    }

    private int index(@NonNull I input, int state) {
        return ((upperStateBound-lowerStateBound+1)*input.ordinal())+(state-lowerStateBound);
    }

    @Nullable
    @Override
    public Integer accept(@NotNull I input) {
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
            return null;
        }
        return state;
    }

    @Override
    public FSMBacklogPair<Integer, I> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<Integer,I> pair = backlog.pop();
        Rollback<Integer,I> rollback = rollbacks[index(pair.input(), pair.state())];
        if (rollback != null) {
            rollback.rollback(state, pair.input(), pair.state());
        }
        FSMBacklogPair<Integer,I> result = new FSMBacklogPair<>(state, pair.input());
        this.state = pair.state();
        return result;
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
        private final int[] transitionMap;
        private final OnEnterStateCallback<Integer,I>[] entryCallbacks;
        private final OnExitStateCallback<Integer, I>[] exitCallbacks;
        private final Rollback<Integer,I>[] rollbacks;
        private ErrorCallback<Integer,I> error;
        private Integer initialState;

        Builder(int lowerStateBound, int upperStateBound, Class<I> inputClass) {
            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            int length = (upperStateBound-lowerStateBound+1)*inputClass.getEnumConstants().length;
            this.transitionMap = new int[length];
            for (int i = 0; i < length; i++) {
                this.transitionMap[i] = -1;
            }
            this.entryCallbacks = new OnEnterStateCallback[(upperStateBound-lowerStateBound+1)];
            this.exitCallbacks = new OnExitStateCallback[(upperStateBound-lowerStateBound+1)];
            this.rollbacks = new Rollback[length];
        }

        @Override
        public IBuilder<Integer, I> addTransition(Integer state, I input, Integer nextState,
                                                  @Nullable OnEnterStateCallback<Integer, I> onEnterStateCallback,
                                                  @Nullable OnExitStateCallback<Integer, I> onExitStateCallback,
                                                  @Nullable Rollback<Integer, I> rollbackCallback) {
            if (state < lowerStateBound || state > upperStateBound
            || nextState < lowerStateBound || nextState > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        initialState, lowerStateBound, upperStateBound));
            }
            int index = (input.ordinal()*(upperStateBound-lowerStateBound+1))+(state-lowerStateBound);
            transitionMap[index] = nextState;
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                exitCallbacks[state] = onExitStateCallback;
            }
            rollbacks[index] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<Integer, I> initialState(Integer initialState) {
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<Integer, I> error(ErrorCallback<Integer, I> errorCallback) {
            this.error = errorCallback;
            return this;
        }

        @Override
        public FiniteStateMachine<Integer, I> build() {
            return new IntEnumStateMachine<>(lowerStateBound, upperStateBound,
                    transitionMap, entryCallbacks, exitCallbacks,
                    rollbacks, error, initialState);
        }
    }
}

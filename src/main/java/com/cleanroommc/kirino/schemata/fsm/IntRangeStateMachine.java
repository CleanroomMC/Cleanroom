package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class IntRangeStateMachine implements FiniteStateMachine<Integer, Integer> {

    private final int lowerStateBound, upperStateBound;
    private final int lowerInputBound, upperInputBound;

    private final int[] stateTable;
    private final OnEnterStateCallback<Integer,Integer>[] entryCallbacks;
    private final OnExitStateCallback<Integer,Integer>[] exitCallbacks;
    private final Rollback<Integer,Integer>[] rollbackCallbacks;
    private final ErrorCallback<Integer,Integer> errorCallback;
    private int state;
    private final Deque<FSMBacklogPair<Integer,Integer>> backlog = new ArrayDeque<>();

    IntRangeStateMachine(int lowerStateBound, int upperStateBound,
                         int lowerInputBound, int upperInputBound,
                         int @NonNull  [] stateTable,
                         OnEnterStateCallback<Integer, Integer> @NonNull [] entryCallbacks,
                         OnExitStateCallback<Integer,Integer> @NonNull [] exitCallbacks,
                         Rollback<Integer, Integer> @NonNull [] rollbackCallbacks,
                         @Nullable ErrorCallback<Integer, Integer> errorCallback,
                         int initialState) {
        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.stateTable = stateTable;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbackCallbacks = rollbackCallbacks;
        this.errorCallback = errorCallback;
        this.state = initialState;
    }

    @NotNull
    @Override
    public Integer state() {
        return this.state;
    }

    private int index(int input, int state) {
        return ((input - lowerInputBound)*(upperStateBound - lowerStateBound + 1))+(state-lowerStateBound);
    }

    @NotNull
    @Override
    public Optional<Integer> accept(@NotNull Integer input) {
        if (input < lowerInputBound || input > upperInputBound){
            throw new IllegalStateException(String.format(
                    "Input %d is out of range [%d,%d]",
                    input, lowerInputBound, upperInputBound));
        }
        int idx = index(input, state);
        if (stateTable[idx] != -1) {
            backlog.push(new FSMBacklogPair<>(state, input));
            if (this.exitCallbacks[state] != null) {
                this.exitCallbacks[state].transition(state,input,stateTable[idx]);
            }
            if (this.entryCallbacks[stateTable[idx]] != null) {
                this.entryCallbacks[stateTable[idx]].transition(state,input,stateTable[idx]);
            }
            this.state = stateTable[idx];
        } else if (errorCallback != null){
            errorCallback.error(state, input);
        }
        return Optional.of(state);
    }

    @NotNull
    @Override
    public Optional<FSMBacklogPair<Integer, Integer>> backtrack() {
        if (backlog.isEmpty()) {
            return Optional.empty();
        }
        FSMBacklogPair<Integer,Integer> pair = backlog.pop();
        Rollback<Integer,Integer> rollback = rollbackCallbacks[index(pair.input(),pair.state())];
        if (rollback != null) {
            rollback.rollback(state, pair.input(), pair.state());
        }
        FSMBacklogPair<Integer,Integer> result = new FSMBacklogPair<>(state, pair.input());
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

    static class Builder implements IBuilder<Integer,Integer> {
        private final int lowerStateBound, upperStateBound;
        private final int lowerInputBound, upperInputBound;
        private final int[] transitionMap;
        private final OnEnterStateCallback<Integer,Integer>[] entryCallbacks;
        private final OnExitStateCallback<Integer,Integer>[] exitCallbacks;
        private final Rollback<Integer,Integer>[] rollbacks;
        private ErrorCallback<Integer,Integer> error;
        private Integer initialState;

        Builder(int lowerStateBound, int upperStateBound, int lowerInputBound, int upperInputBound) {
            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            int stateCount = (upperStateBound-lowerStateBound+1);
            int size = stateCount*(upperInputBound-lowerInputBound+1);
            this.transitionMap = new int[size];
            for (int i = 0; i < size; i++) {
                this.transitionMap[i] = -1;
            }
            this.entryCallbacks = new OnEnterStateCallback[stateCount];
            this.exitCallbacks = new OnExitStateCallback[stateCount];
            this.rollbacks = new Rollback[size];
        }

        private int index(int input, int state) {
            return ((input-lowerInputBound)*(upperStateBound-lowerStateBound+1))+(state-lowerStateBound);
        }

        @Override
        public IBuilder<Integer, Integer> addTransition(@NotNull Integer state, @NotNull Integer input, @NotNull Integer nextState,
                                                        @Nullable OnEnterStateCallback<Integer, Integer> onEnterStateCallback,
                                                        @Nullable OnExitStateCallback<Integer, Integer> onExitStateCallback,
                                                        @Nullable Rollback<Integer, Integer> rollbackCallback) {
            if (state < lowerStateBound || state > upperStateBound
                    || nextState < lowerStateBound || nextState > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        state, lowerStateBound, upperStateBound));
            }
            if (input < lowerInputBound || input > upperInputBound){
                throw new IllegalStateException(String.format(
                        "Input %d is out of range [%d,%d]",
                        input, lowerInputBound, upperInputBound));
            }
            int idx = this.index(input, state);
            transitionMap[idx] = nextState;
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                exitCallbacks[state] = onExitStateCallback;
            }
            rollbacks[idx] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> setEntryCallback(@NonNull Integer state, @Nullable OnEnterStateCallback<Integer, Integer> callback) {
            if (state < lowerStateBound || state > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        state, lowerStateBound, upperStateBound));
            }
            entryCallbacks[state] = callback;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> setExitCallback(@NonNull Integer state, @Nullable OnExitStateCallback<Integer, Integer> callback) {
            if (state < lowerStateBound || state > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        state, lowerStateBound, upperStateBound));
            }
            exitCallbacks[state] = callback;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> initialState(@NotNull Integer initialState) {
            if (initialState < lowerStateBound || initialState > upperStateBound) {
                throw new IllegalStateException(String.format(
                        "State %d out of range [%d,%d]",
                        initialState, lowerStateBound, upperStateBound));
            }
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> error(@NonNull ErrorCallback<Integer, Integer> errorCallback) {
            Preconditions.checkNotNull(errorCallback);
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
                    for (int input = lowerInputBound; input <= upperInputBound; input++) {
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
        public FiniteStateMachine<Integer, Integer> build() {
            return new IntRangeStateMachine(lowerStateBound, upperStateBound,
                    lowerInputBound, upperInputBound,
                    transitionMap,
                    entryCallbacks, exitCallbacks,
                    rollbacks, error, initialState);
        }
    }
}
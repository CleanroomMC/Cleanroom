package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class IntRangeStateMachine implements FiniteStateMachine<Integer, Integer> {

    private final int lowerStateBound, upperStateBound;
    private final int lowerInputBound, upperInputBound;
    private final int[] transitionMap;
    private final OnEnterStateCallback<Integer,Integer>[] entryCallbacks;
    private final OnExitStateCallback<Integer,Integer>[] exitCallbacks;
    private final Rollback<Integer,Integer>[] rollbackCallbacks;
    private final ErrorCallback<Integer,Integer> errorCallback;
    private int state;
    private final Deque<FSMBacklogPair<Integer,Integer>> backlog = new ArrayDeque<>();

    IntRangeStateMachine(int lowerStateBound, int upperStateBound,
                         int lowerInputBound, int upperInputBound,
                         int @NonNull [] transitionMap,
                         OnEnterStateCallback<Integer, Integer> @NonNull [] entryCallbacks,
                         OnExitStateCallback<Integer,Integer> @NonNull [] exitCallbacks,
                         Rollback<Integer, Integer> @NonNull [] rollbackCallbacks,
                         @Nullable ErrorCallback<Integer, Integer> errorCallback,
                         int initialState) {
        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.transitionMap = transitionMap;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbackCallbacks = rollbackCallbacks;
        this.errorCallback = errorCallback;
        state = initialState;
    }

    @NonNull
    @Override
    public Integer state() {
        return state;
    }

    private int index(int input, int state) {
        return ((input - lowerInputBound) * (upperStateBound - lowerStateBound + 1)) + (state - lowerStateBound);
    }

    @NonNull
    @Override
    public Optional<Integer> accept(@NonNull Integer input) {
        Preconditions.checkArgument(!(input < lowerInputBound || input > upperInputBound),
                "Input %d is out of range [%d, %d].",
                input, lowerInputBound, upperInputBound);

        int idx = index(input, state);
        if (transitionMap[idx] != -1) {
            backlog.push(new FSMBacklogPair<>(state, input));
            if (exitCallbacks[state] != null) {
                exitCallbacks[state].transition(state, input, transitionMap[idx]);
            }
            if (entryCallbacks[transitionMap[idx]] != null) {
                entryCallbacks[transitionMap[idx]].transition(state, input, transitionMap[idx]);
            }
            state = transitionMap[idx];
        } else if (errorCallback != null){
            errorCallback.error(state, input);
        }
        return Optional.of(state);
    }

    @NonNull
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
        state = pair.state();
        return Optional.of(result);
    }

    @Override
    public void reset() {
        if (!backlog.isEmpty()) {
            state = backlog.pollLast().state();
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

        @SuppressWarnings("unchecked")
        Builder(int lowerStateBound, int upperStateBound, int lowerInputBound, int upperInputBound) {
            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            int stateCount = (upperStateBound-lowerStateBound+1);
            int size = stateCount*(upperInputBound-lowerInputBound+1);
            transitionMap = new int[size];
            for (int i = 0; i < size; i++) {
                transitionMap[i] = -1;
            }
            entryCallbacks = new OnEnterStateCallback[stateCount];
            exitCallbacks = new OnExitStateCallback[stateCount];
            rollbacks = new Rollback[size];
        }

        private int index(int input, int state) {
            return ((input - lowerInputBound) * (upperStateBound - lowerStateBound + 1)) + (state - lowerStateBound);
        }

        @Override
        public IBuilder<Integer, Integer> addTransition(@NonNull Integer state, @NonNull Integer input, @NonNull Integer nextState,
                                                        @Nullable OnEnterStateCallback<Integer, Integer> onEnterStateCallback,
                                                        @Nullable OnExitStateCallback<Integer, Integer> onExitStateCallback,
                                                        @Nullable Rollback<Integer, Integer> rollbackCallback) {
            Preconditions.checkArgument(!(state < lowerStateBound || state > upperStateBound || nextState < lowerStateBound || nextState > upperStateBound),
                    "State %d out of range [%d, %d].",
                    state, lowerStateBound, upperStateBound);
            Preconditions.checkArgument(!(input < lowerInputBound || input > upperInputBound),
                    "Input %d is out of range [%d, %d].",
                    input, lowerInputBound, upperInputBound);

            int idx = index(input, state);
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
            Preconditions.checkArgument(!(state < lowerStateBound || state > upperStateBound),
                    "State %d out of range [%d, %d].",
                    state, lowerStateBound, upperStateBound);

            entryCallbacks[state] = callback;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> setExitCallback(@NonNull Integer state, @Nullable OnExitStateCallback<Integer, Integer> callback) {
            Preconditions.checkArgument(!(state < lowerStateBound || state > upperStateBound),
                    "State %d out of range [%d, %d].",
                    state, lowerStateBound, upperStateBound);

            exitCallbacks[state] = callback;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> initialState(@NonNull Integer initialState) {
            Preconditions.checkArgument(!(initialState < lowerStateBound || initialState > upperStateBound),
                    "State %d out of range [%d, %d].",
                    initialState, lowerStateBound, upperStateBound);

            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> error(@NonNull ErrorCallback<Integer, Integer> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Provided \"errorCallback\" can't be null, if you don't want to use a failure callback don't call this function.");

            error = errorCallback;
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
            Preconditions.checkNotNull(initialState, "The Initial State must be set before the FSM is built.");

            return new IntRangeStateMachine(lowerStateBound, upperStateBound,
                    lowerInputBound, upperInputBound,
                    transitionMap,
                    entryCallbacks, exitCallbacks,
                    rollbacks, error, initialState);
        }
    }
}
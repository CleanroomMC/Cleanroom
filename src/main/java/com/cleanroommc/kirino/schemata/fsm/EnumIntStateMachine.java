package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

final class EnumIntStateMachine<S extends Enum<S>> implements FiniteStateMachine<S, Integer> {

    private final int lowerInputBound, upperInputBound;
    private final S[] states;
    private final int[] transitionMap;
    private final OnEnterStateCallback<S,Integer>[] stateEnterCallbackMap;
    private final OnExitStateCallback<S,Integer>[] stateExitCallbackMap;
    private final Rollback<S,Integer>[] rollbacks;
    private final ErrorCallback<S,Integer> error;
    private int state;
    private final Deque<FSMBacklogPair<S,Integer>> backlog = new ArrayDeque<>();

    EnumIntStateMachine(int lowerInputBound, int upperInputBound,
                        @NonNull S[] states, int @NonNull [] transitionMap,
                        OnEnterStateCallback<S, Integer> @NonNull [] stateEnterCallbackMap,
                        OnExitStateCallback<S,Integer> @NonNull [] stateExitCallbackMap,
                        Rollback<S, Integer> @NonNull [] rollbacks,
                        @Nullable ErrorCallback<S, Integer> error,
                        @NonNull S initialState) {
        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.states = states;
        this.transitionMap = transitionMap;
        this.stateEnterCallbackMap = stateEnterCallbackMap;
        this.stateExitCallbackMap = stateExitCallbackMap;
        this.rollbacks = rollbacks;
        this.error = error;
        this.state = initialState.ordinal();
    }

    @NotNull
    @Override
    public S state() {
        return states[state];
    }

    private int index(Integer input, Integer state) {
        return (states.length*(input-lowerInputBound))+state;
    }

    @NotNull
    @Override
    public Optional<S> accept(@NotNull Integer input) {
        if (input < lowerInputBound || input > upperInputBound) {
            throw new IllegalStateException(String.format(
                    "Input %d is out of range [%d,%d]",
                    input, lowerInputBound, upperInputBound));
        }
        final int idx = this.index(input, state);
        if (transitionMap[idx] != -1) {
            backlog.push(new FSMBacklogPair<>(states[state], input));
            if (this.stateExitCallbackMap[state] != null) {
                this.stateExitCallbackMap[state].transition(states[state], input, states[transitionMap[idx]]);
            }
            if (this.stateEnterCallbackMap[transitionMap[idx]] != null) {
                this.stateEnterCallbackMap[transitionMap[idx]].transition(states[state], input, states[transitionMap[idx]]);
            }
            state = transitionMap[idx];
        } else if (error != null) {
            error.error(states[state], input);
            return Optional.empty();
        }
        return Optional.of(states[state]);
    }

    @NotNull
    @Override
    public Optional<FSMBacklogPair<S, Integer>> backtrack() {
        if (backlog.isEmpty()) {
            return Optional.empty();
        }
        FSMBacklogPair<S,Integer> pair = backlog.pop();
        Rollback<S,Integer> rollback = rollbacks[index(pair.input(), pair.state().ordinal())];
        if (rollback != null) {
            rollback.rollback(states[state], pair.input(), pair.state());
        }
        FSMBacklogPair<S,Integer> result = new FSMBacklogPair<>(states[state], pair.input());
        this.state = pair.state().ordinal();
        return Optional.of(result);
    }

    @Override
    public void reset() {
        if (!backlog.isEmpty()) {
            this.state = backlog.pollLast().state().ordinal();
            backlog.clear();
        }
    }

    static class Builder<S extends Enum<S>> implements IBuilder<S, Integer> {
        private final int lowerInputBound, upperInputBound;
        private final int[] transitionMap;
        private final S[] states;
        private final OnEnterStateCallback<S, Integer>[] stateEnterCallbackMap;
        private final OnExitStateCallback<S, Integer>[] stateExitCallbackMap;
        private final Rollback<S, Integer>[] rollbacks;
        private ErrorCallback<S, Integer> error;
        private S initialState;

        Builder(Class<S> stateClass, int lowerInputBound, int upperInputBound) {
            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            this.states = stateClass.getEnumConstants();
            final int size = this.states.length*(upperInputBound-lowerInputBound+1);
            this.transitionMap = new int[size];
            for (int i = 0; i < size; i++) {
                this.transitionMap[i] = -1;
            }
            this.stateEnterCallbackMap = new OnEnterStateCallback[states.length];
            this.stateExitCallbackMap = new OnExitStateCallback[states.length];
            this.rollbacks = new Rollback[size];
        }

        private int index(Integer input, S state) {
            return ((input-lowerInputBound)*states.length)+state.ordinal();
        }

        @Override
        public IBuilder<S, Integer> addTransition(@NonNull S state, @NonNull Integer input, @NonNull S nextState,
                                                  @Nullable OnEnterStateCallback<S, Integer> onEnterStateCallback,
                                                  @Nullable OnExitStateCallback<S, Integer> onExitStateCallback,
                                                  @Nullable Rollback<S, Integer> rollbackCallback) {
            Preconditions.checkNotNull(state, "Parameter \"state\" must not be null");
            Preconditions.checkNotNull(nextState, "Parameter \"nextState\" must not be null");
            if (input < lowerInputBound || input > upperInputBound){
                throw new IllegalStateException(String.format(
                        "Input %d is out of range [%d,%d]",
                        input, lowerInputBound, upperInputBound));
            }
            final int idx = this.index(input, state);
            transitionMap[idx] = nextState.ordinal();
            if (onEnterStateCallback != null) {
                stateEnterCallbackMap[nextState.ordinal()] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                stateExitCallbackMap[state.ordinal()] = onExitStateCallback;
            }
            rollbacks[idx] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<S, Integer> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, Integer> callback) {
            Preconditions.checkNotNull(state, "Parameter \"state\" must not be null");
            stateEnterCallbackMap[state.ordinal()] = callback;
            return this;
        }

        @Override
        public IBuilder<S, Integer> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, Integer> callback) {
            Preconditions.checkNotNull(state, "Parameter \"state\" must not be null");
            stateExitCallbackMap[state.ordinal()] = callback;
            return this;
        }

        @Override
        public IBuilder<S, Integer> initialState(@NotNull S initialState) {
            Preconditions.checkNotNull(initialState, "Parameter \"initialState\" must not be null");
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<S, Integer> error(@NotNull ErrorCallback<S, Integer> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Provided \"errorCallback\" can't be null, if you don't want to use a failure callback don't call this function");
            this.error = errorCallback;
            return this;
        }

        @Override
        public boolean validate() {
            BitSet reachable = new BitSet(states.length);
            Deque<S> stack = new ArrayDeque<>();
            stack.push(initialState);
            while (!stack.isEmpty()) {
                S state = stack.pop();
                if (!reachable.get(state.ordinal())) {
                    reachable.set(state.ordinal());
                    for (int input = lowerInputBound; input <= upperInputBound; input++) {
                        int next = transitionMap[index(input,state)];
                        if (!(next == -1 || next == state.ordinal())) {
                            stack.push(states[next]);
                        }
                    }
                }
            }
            return reachable.cardinality() == states.length;
        }

        @Override
        public FiniteStateMachine<S, Integer> build() {
            Preconditions.checkNotNull(initialState, "The Initial State must be set before the FSM is built");
            return new EnumIntStateMachine<S>(lowerInputBound,upperInputBound,
                    states, transitionMap, stateEnterCallbackMap, stateExitCallbackMap, rollbacks, error, initialState);
        }
    }
}

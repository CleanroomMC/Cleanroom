package com.cleanroommc.kirino.schemata.fsm;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

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

    @Override
    public S state() {
        return states[state];
    }

    private int index(Integer input, Integer state) {
        return (states.length*(input-lowerInputBound))+state;
    }

    @Override
    public @Nullable S accept(@NotNull Integer input) {
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
            return null;
        }
        return states[state];
    }

    @Override
    public FSMBacklogPair<S, Integer> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<S,Integer> pair = backlog.pop();
        Rollback<S,Integer> rollback = rollbacks[index(pair.input(), pair.state().ordinal())];
        if (rollback != null) {
            rollback.rollback(states[state], pair.input(), pair.state());
        }
        FSMBacklogPair<S,Integer> result = new FSMBacklogPair<>(states[state], pair.input());
        this.state = pair.state().ordinal();
        return result;
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
        public IBuilder<S, Integer> addTransition(S state, Integer input, S nextState,
                                                  OnEnterStateCallback<S, Integer> onEnterStateCallback,
                                                  OnExitStateCallback<S, Integer> onExitStateCallback,
                                                  Rollback<S, Integer> rollbackCallback) {
            if (input < lowerInputBound || input > upperInputBound){
                throw new IllegalStateException(String.format(
                        "Input %d is out of range [%d,%d]",
                        input, lowerInputBound, upperInputBound));
            }
            final int idx = this.index(input, state);
            transitionMap[idx] = nextState.ordinal();
            stateEnterCallbackMap[nextState.ordinal()] = onEnterStateCallback;
            stateExitCallbackMap[state.ordinal()] = onExitStateCallback;
            rollbacks[idx] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<S, Integer> initialState(S initialState) {
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<S, Integer> error(ErrorCallback<S, Integer> errorCallback) {
            this.error = errorCallback;
            return this;
        }

        @Override
        public FiniteStateMachine<S, Integer> build() {
            return new EnumIntStateMachine<S>(lowerInputBound,upperInputBound,
                    states, transitionMap, stateEnterCallbackMap, stateExitCallbackMap, rollbacks, error, initialState);
        }
    }
}

package com.cleanroommc.kirino.schemata.fsm;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

final class EnumStateMachine<S extends Enum<S>, I extends Enum<I>> implements FiniteStateMachine<S, I> {

    private final S[] states;
    private final int[] transitionMap;
    private final OnEnterStateCallback<S, I>[] entryCallbacks;
    private final OnExitStateCallback<S, I>[] exitCallbacks;
    private final Rollback<S, I>[] rollbacks;
    private final ErrorCallback<S, I> error;
    private int state;
    private final Deque<FSMBacklogPair<S, I>> backlog = new ArrayDeque<>();

    EnumStateMachine(@NonNull S initialState, @NonNull S[] states,
                     int @NonNull [] transitionMap, OnEnterStateCallback<S, I> @NonNull [] entryCallbacks,
                     OnExitStateCallback<S, I> @NonNull [] exitCallbacks,
                     Rollback<S, I> @NonNull [] rollbacks, @Nullable ErrorCallback<S, I> error) {
        this.state = initialState.ordinal();
        this.transitionMap = transitionMap;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbacks = rollbacks;
        this.error = error;
        this.states = states;
    }

    @Override
    public S state() {
        return states[state];
    }

    private int index(I input, int state) {
        return (states.length*input.ordinal())+state;
    }

    @Nullable
    @Override
    public S accept(@NotNull I input) {
        int idx = this.index(input, state);
        if (transitionMap[idx] != -1) {
            backlog.push(new FSMBacklogPair<>(states[state], input));
            if (this.exitCallbacks[state] != null) {
                this.exitCallbacks[state].transition(states[state], input, states[transitionMap[idx]]);
            }
            if (this.entryCallbacks[transitionMap[idx]] != null) {
                this.entryCallbacks[transitionMap[idx]].transition(states[state], input, states[transitionMap[idx]]);
            }
            state = transitionMap[idx];
        } else if (error != null) {
            error.error(states[state], input);
            return null;
        }
        return states[state];
    }

    @Override
    public FSMBacklogPair<S, I> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<S, I> pair = backlog.pop();
        Rollback<S, I> rollback = rollbacks[index(pair.input(), pair.state().ordinal())];
        if (rollback != null) {
            rollback.rollback(states[state], pair.input(), pair.state());
        }
        FSMBacklogPair<S, I> result = new FSMBacklogPair<>(states[state], pair.input());
        this.state = pair.state().ordinal();
        return result;
    }

    @Override
    public void reset() {
        if (!backlog.isEmpty()) {
            this.state = backlog.pollLast().state().ordinal();
            backlog.clear();
        }
    }

    static class Builder<S extends Enum<S>, I extends Enum<I>> implements IBuilder<S, I> {

        private final S[] states;
        private final int[] transitionMap;
        private final OnEnterStateCallback<S, I>[] entryCallbacks;
        private final OnExitStateCallback<S, I>[] exitCallbacks;
        private final Rollback<S, I>[] rollbacks;
        private ErrorCallback<S, I> error;
        private S initialState;

        Builder(Class<S> stateClass, Class<I> inputClass){
            this.states = stateClass.getEnumConstants();
            int length = states.length*inputClass.getEnumConstants().length;
            this.transitionMap = new int[length];
            for (int i = 0; i < length; i++) {
                this.transitionMap[i] = -1;
            }
            this.entryCallbacks = new OnEnterStateCallback[states.length];
            this.exitCallbacks = new OnExitStateCallback[states.length];
            this.rollbacks = new Rollback[length];
        }

        private int index(I input, S state) {
            return (input.ordinal() * states.length) + state.ordinal();
        }

        @Override
        public IBuilder<S, I> addTransition(@NonNull S state, @NonNull I input, @NonNull S nextState,
                                            @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                                            @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                                            @Nullable Rollback<S, I> rollbackCallback) {
            int idx = this.index(input, state);
            transitionMap[idx] = nextState.ordinal();
            if (onEnterStateCallback != null) {
                entryCallbacks[nextState.ordinal()] = onEnterStateCallback;
            }
            if (onExitStateCallback != null) {
                exitCallbacks[state.ordinal()] = onExitStateCallback;
            }
            rollbacks[idx] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<S, I> initialState(S initialState) {
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<S, I> error(ErrorCallback<S, I> errorCallback) {
            this.error = errorCallback;
            return this;
        }

        @Override
        public FiniteStateMachine<S, I> build() {
            return new EnumStateMachine<>(initialState, states, transitionMap,
                    entryCallbacks, exitCallbacks,
                    rollbacks, error);
        }
    }
}

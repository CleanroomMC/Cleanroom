package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;
import java.util.Optional;

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
        this.transitionMap = transitionMap;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbacks = rollbacks;
        this.error = error;
        this.states = states;
        state = initialState.ordinal();
    }

    @NonNull
    @Override
    public S state() {
        return states[state];
    }

    private int index(I input, int state) {
        return (states.length * input.ordinal()) + state;
    }

    @NonNull
    @Override
    public Optional<S> accept(@NonNull I input) {
        Preconditions.checkNotNull(input, "Parameter \"input\" must not be null.");

        int idx = index(input, state);
        if (transitionMap[idx] != -1) {
            backlog.push(new FSMBacklogPair<>(states[state], input));
            if (exitCallbacks[state] != null) {
                exitCallbacks[state].transition(states[state], input, states[transitionMap[idx]]);
            }
            if (entryCallbacks[transitionMap[idx]] != null) {
                entryCallbacks[transitionMap[idx]].transition(states[state], input, states[transitionMap[idx]]);
            }
            state = transitionMap[idx];
        } else if (error != null) {
            error.error(states[state], input);
            return Optional.empty();
        }
        return Optional.of(states[state]);
    }

    @NonNull
    @Override
    public Optional<FSMBacklogPair<S, I>> backtrack() {
        if (backlog.isEmpty()) {
            return Optional.empty();
        }

        FSMBacklogPair<S, I> pair = backlog.pop();
        Rollback<S, I> rollback = rollbacks[index(pair.input(), pair.state().ordinal())];
        if (rollback != null) {
            rollback.rollback(states[state], pair.input(), pair.state());
        }
        FSMBacklogPair<S, I> result = new FSMBacklogPair<>(states[state], pair.input());
        state = pair.state().ordinal();
        return Optional.of(result);
    }

    @Override
    public void reset() {
        if (!backlog.isEmpty()) {
            state = backlog.pollLast().state().ordinal();
            backlog.clear();
        }
    }

    static class Builder<S extends Enum<S>, I extends Enum<I>> implements IBuilder<S, I> {

        private final S[] states;
        private final I[] inputs; // only for validation
        private final int[] transitionMap;
        private final OnEnterStateCallback<S, I>[] entryCallbacks;
        private final OnExitStateCallback<S, I>[] exitCallbacks;
        private final Rollback<S, I>[] rollbacks;
        private ErrorCallback<S, I> error;
        private S initialState;

        @SuppressWarnings("unchecked")
        Builder(Class<S> stateClass, Class<I> inputClass){
            states = stateClass.getEnumConstants();
            inputs = inputClass.getEnumConstants();
            int length = states.length * inputs.length;
            transitionMap = new int[length];
            for (int i = 0; i < length; i++) {
                transitionMap[i] = -1;
            }
            entryCallbacks = new OnEnterStateCallback[states.length];
            exitCallbacks = new OnExitStateCallback[states.length];
            rollbacks = new Rollback[length];
        }

        private int index(I input, S state) {
            return (states.length * input.ordinal()) + state.ordinal();
        }

        @Override
        public IBuilder<S, I> addTransition(@NonNull S state, @NonNull I input, @NonNull S nextState,
                                            @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                                            @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                                            @Nullable Rollback<S, I> rollbackCallback) {
            Preconditions.checkNotNull(state, "Parameter \"state\" must not be null.");
            Preconditions.checkNotNull(input, "Parameter \"input\" must not be null.");
            Preconditions.checkNotNull(nextState, "Parameter \"nextState\" must not be null.");

            int idx = index(input, state);
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
        public IBuilder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Parameter \"state\" must not be null.");

            entryCallbacks[state.ordinal()] = callback;
            return this;
        }

        @Override
        public IBuilder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Parameter \"state\" must not be null.");

            exitCallbacks[state.ordinal()] = callback;
            return this;
        }

        @Override
        public IBuilder<S, I> initialState(@NonNull S initialState) {
            Preconditions.checkNotNull(initialState, "Parameter \"initialState\" must not be null.");

            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<S, I> error(@NonNull ErrorCallback<S, I> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Provided \"errorCallback\" can't be null, if you don't want to use a failure callback don't call this function.");

            error = errorCallback;
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
                    for (I input : inputs) {
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
        public FiniteStateMachine<S, I> build() {
            Preconditions.checkNotNull(initialState, "The Initial State must be set before the FSM is built.");

            return new EnumStateMachine<>(initialState, states, transitionMap,
                    entryCallbacks, exitCallbacks,
                    rollbacks, error);
        }
    }
}

package com.cleanroommc.kirino.schemata.fsm;

import java.util.Stack;

class EnumStateMachine<S extends Enum<S>,I extends Enum<I>> implements FiniteStateMachine<S,I> {

    protected final S[] states;
    private final int[] stateTable;
    protected final StateTransitionCallback<S,I>[] transitions;
    protected final Rollback<S,I>[] rollbacks;
    protected final ErrorCallback<S,I> error;
    protected int state;
    protected Stack<FSMBacklogPair<S,I>> backlog = new Stack<>();

    EnumStateMachine(S initialState, S[] states,
                     int[] stateTable, StateTransitionCallback<S,I>[] transitions,
                     Rollback<S,I>[] rollbacks, ErrorCallback<S,I> error) {
        this.state = initialState.ordinal();
        this.stateTable = stateTable;
        this.transitions = transitions;
        this.rollbacks = rollbacks;
        this.error = error;
        this.states = states;
    }

    @Override
    public S state() {
        return states[state];
    }

    @Override
    public S accept(I input) {
        int index = (states.length*input.ordinal())+state;
        if (stateTable[index] != -1) {
            backlog.push(new FSMBacklogPair<>(states[state], input));
            if (this.transitions[index] != null) {
                this.transitions[index].transition(states[state], input, states[stateTable[index]]);
            }
            state = stateTable[index];
        } else if (error != null) {
            error.error(states[state], input);
        }
        return states[state];
    }

    @Override
    public FSMBacklogPair<S, I> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<S,I> pair = backlog.pop();
        Rollback<S,I> rollback = rollbacks[(pair.input().ordinal() * states.length) + pair.state().ordinal()];
        if (rollback != null) {
            rollback.rollback(states[state], pair.input(), pair.state());
        }
        FSMBacklogPair<S,I> result = new FSMBacklogPair<>(states[state], pair.input());
        this.state = pair.state().ordinal();
        return result;
    }

    static class Builder<S extends Enum<S>,I extends Enum<I>> implements IBuilder<S,I> {

        private final S[] states;
        private final int[] stateTable;
        private final StateTransitionCallback<S,I>[] transitions;
        private final Rollback<S,I>[] rollbacks;
        private ErrorCallback<S,I> error;
        private S initialState;

        Builder(Class<S> stateClass, Class<I> inputClass){
            this.states = stateClass.getEnumConstants();
            int length = states.length*inputClass.getEnumConstants().length;
            this.stateTable = new int[length];
            for (int i = 0; i < length; i++) {
                this.stateTable[i] = -1;
            }
            this.transitions = new StateTransitionCallback[length];
            this.rollbacks = new Rollback[length];
        }

        @Override
        public IBuilder<S, I> addTransition(S state, I input, S nextState, StateTransitionCallback<S, I> stateTransitionCallback, Rollback<S, I> rollbackCallback) {
            int index = (input.ordinal() * states.length) + state.ordinal();
            stateTable[index] = nextState.ordinal();
            transitions[index] = stateTransitionCallback;
            rollbacks[index] = rollbackCallback;
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
            return new EnumStateMachine<>(initialState, states, stateTable, transitions, rollbacks, error);
        }
    }
}

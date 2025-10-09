package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.kirino.schemata.exception.InputOutOfRangeException;

import java.util.ArrayDeque;
import java.util.Deque;

class EnumIntStateMachine<S extends Enum<S>> implements FiniteStateMachine<S, Integer> {

    protected final int lowerInputBound, upperInputBound;
    protected final S[] states;
    private final int[] stateTable;
    protected final StateTransitionCallback<S,Integer>[] transitions;
    protected final Rollback<S,Integer>[] rollbacks;
    protected final ErrorCallback<S,Integer> error;
    protected int state;
    protected final Deque<FSMBacklogPair<S,Integer>> backlog = new ArrayDeque<>();

    EnumIntStateMachine(int lowerInputBound, int upperInputBound,
                        S[] states, int[] stateTable,
                        StateTransitionCallback<S, Integer>[] transitions,
                        Rollback<S, Integer>[] rollbacks,
                        ErrorCallback<S, Integer> error,
                        S initialState) {
        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.states = states;
        this.stateTable = stateTable;
        this.transitions = transitions;
        this.rollbacks = rollbacks;
        this.error = error;
        this.state = initialState.ordinal();
    }

    @Override
    public S state() {
        return states[state];
    }

    @Override
    public S accept(Integer input) {
        if (input < lowerInputBound || input > upperInputBound)
            throw new InputOutOfRangeException(String.format("Input %d is out of range [%d,%d]", input, lowerInputBound, upperInputBound));
        int index = (states.length*(input-lowerInputBound))+state;
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
    public FSMBacklogPair<S, Integer> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<S,Integer> pair = backlog.pop();
        Rollback<S,Integer> rollback = rollbacks[((pair.input()-lowerInputBound) * states.length) + pair.state().ordinal()];
        if (rollback != null) {
            rollback.rollback(states[state], pair.input(), pair.state());
        }
        FSMBacklogPair<S,Integer> result = new FSMBacklogPair<>(states[state], pair.input());
        this.state = pair.state().ordinal();
        return result;
    }

    static class Builder<S extends Enum<S>> implements IBuilder<S,Integer> {
        private final int lowerInputBound, upperInputBound;
        private final int[] stateTable;
        protected final S[] states;
        private final StateTransitionCallback<S,Integer>[] transitions;
        private final Rollback<S,Integer>[] rollbacks;
        private ErrorCallback<S,Integer> error;
        private S initialState;

        Builder(Class<S> stateClass, int lowerInputBound, int upperInputBound) {
            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            this.states = stateClass.getEnumConstants();
            int size = this.states.length*(upperInputBound-lowerInputBound+1);
            this.stateTable = new int[size];
            for (int i = 0; i < size; i++) {
                this.stateTable[i] = -1;
            }
            this.transitions = new StateTransitionCallback[size];
            this.rollbacks = new Rollback[size];
        }

        @Override
        public IBuilder<S, Integer> addTransition(S state, Integer input, S nextState, StateTransitionCallback<S, Integer> stateTransitionCallback, Rollback<S, Integer> rollbackCallback) {
            if (input < lowerInputBound || input > upperInputBound){
                throw new InputOutOfRangeException(String.format("Input %d is out of range [%d,%d]", input, lowerInputBound, upperInputBound));
            }
            int index = ((input-lowerInputBound)*states.length)+state.ordinal();
            stateTable[index] = nextState.ordinal();
            transitions[index] = stateTransitionCallback;
            rollbacks[index] = rollbackCallback;
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
                    states,stateTable,transitions,rollbacks,error,initialState);
        }
    }
}

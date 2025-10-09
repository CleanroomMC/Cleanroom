package com.cleanroommc.kirino.schemata.fsm;

import java.util.ArrayDeque;
import java.util.Deque;

class IntEnumStateMachine<I extends Enum<I>> implements FiniteStateMachine<Integer,I> {
    protected final int lowerStateBound, upperStateBound;
    private final int[] stateTable;
    protected final StateTransitionCallback<Integer, I>[] transitions;
    protected final Rollback<Integer, I>[] rollbacks;
    protected final ErrorCallback<Integer, I> error;
    protected int state;
    protected final Deque<FSMBacklogPair<Integer, I>> backlog = new ArrayDeque<>();

    IntEnumStateMachine(int lowerStateBound, int upperStateBound,
                        int[] stateTable,
                        StateTransitionCallback<Integer, I>[] transitions,
                        Rollback<Integer, I>[] rollbacks,
                        ErrorCallback<Integer, I> error,
                        int initialState) {
        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.stateTable = stateTable;
        this.transitions = transitions;
        this.rollbacks = rollbacks;
        this.error = error;
        this.state = initialState;
    }

    @Override
    public Integer state() {
        return state;
    }

    @Override
    public Integer accept(I input) {
        int index = ((upperStateBound-lowerStateBound+1)*input.ordinal())+(state-lowerStateBound);
        if (stateTable[index] != -1) {
            backlog.push(new FSMBacklogPair<>(state, input));
            if (this.transitions[index] != null) {
                this.transitions[index].transition(state, input, stateTable[index]);
            }
            state = stateTable[index];
        } else if (error != null) {
            error.error(state, input);
        }
        return state;
    }

    @Override
    public FSMBacklogPair<Integer, I> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<Integer,I> pair = backlog.pop();
        Rollback<Integer,I> rollback = rollbacks[(pair.input().ordinal() * (upperStateBound-lowerStateBound+1)) + (pair.state()-lowerStateBound)];
        if (rollback != null) {
            rollback.rollback(state, pair.input(), pair.state());
        }
        FSMBacklogPair<Integer,I> result = new FSMBacklogPair<>(state, pair.input());
        this.state = pair.state();
        return result;
    }

    static class Builder<I extends Enum<I>> implements IBuilder<Integer,I> {

        private final int lowerStateBound, upperStateBound;
        private final int[] stateTable;
        private final StateTransitionCallback<Integer,I>[] transitions;
        private final Rollback<Integer,I>[] rollbacks;
        private ErrorCallback<Integer,I> error;
        private Integer initialState;

        Builder(int lowerStateBound, int upperStateBound, Class<I> inputClass) {
            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            int length = (upperStateBound-lowerStateBound+1)*inputClass.getEnumConstants().length;
            this.stateTable = new int[length];
            for (int i = 0; i < length; i++) {
                this.stateTable[i] = -1;
            }
            this.transitions = new StateTransitionCallback[length];
            this.rollbacks = new Rollback[length];
        }

        @Override
        public IBuilder<Integer, I> addTransition(Integer state, I input, Integer nextState, StateTransitionCallback<Integer, I> stateTransitionCallback, Rollback<Integer, I> rollbackCallback) {

            int index = (input.ordinal()*(upperStateBound-lowerStateBound+1))+(state-lowerStateBound);
            stateTable[index] = nextState;
            transitions[index] = stateTransitionCallback;
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
                    stateTable, transitions, rollbacks, error, initialState);
        }
    }
}

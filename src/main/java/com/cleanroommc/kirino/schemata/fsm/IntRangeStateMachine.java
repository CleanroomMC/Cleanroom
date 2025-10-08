package com.cleanroommc.kirino.schemata.fsm;

import com.cleanroommc.kirino.errors.InputOutOfRangeException;

import java.util.ArrayDeque;
import java.util.Deque;

class IntRangeStateMachine implements FiniteStateMachine<Integer, Integer> {

    protected final int lowerStateBound, upperStateBound;
    protected final int lowerInputBound, upperInputBound;

    protected final int[] stateTable;
    protected final StateTransitionCallback<Integer,Integer>[] transitionCallbacks;
    protected final Rollback<Integer,Integer>[] rollbackCallbacks;
    protected final ErrorCallback<Integer,Integer> errorCallback;
    protected int state;
    protected final Deque<FSMBacklogPair<Integer,Integer>> backlog = new ArrayDeque<>();

    IntRangeStateMachine(int lowerStateBound, int upperStateBound,
                         int lowerInputBound, int upperInputBound,
                         int[] stateTable,
                         StateTransitionCallback<Integer, Integer>[] transitionCallbacks,
                         Rollback<Integer, Integer>[] rollbackCallbacks,
                         ErrorCallback<Integer, Integer> errorCallback,
                         int initialState) {
        this.lowerStateBound = lowerStateBound;
        this.upperStateBound = upperStateBound;
        this.lowerInputBound = lowerInputBound;
        this.upperInputBound = upperInputBound;
        this.stateTable = stateTable;
        this.transitionCallbacks= transitionCallbacks;
        this.rollbackCallbacks = rollbackCallbacks;
        this.errorCallback = errorCallback;
        this.state = initialState;
    }

    @Override
    public Integer state() {
        return this.state;
    }

    @Override
    public Integer accept(Integer input) {
        if (input < lowerInputBound || input > upperInputBound){
            throw new InputOutOfRangeException(String.format("Input %d is out of range [%d,%d]", input, lowerInputBound, upperInputBound));
        }
        int states = upperStateBound - lowerStateBound + 1;
        int index = ((input - lowerInputBound)*states)+(state-lowerStateBound);
        if (stateTable[index] != -1) {
            backlog.push(new FSMBacklogPair<>(state, input));
            if (this.transitionCallbacks[index] != null) {
                this.transitionCallbacks[index].transition(state,input,stateTable[index]);
            }
            this.state = stateTable[index];
        } else if (errorCallback != null){
            errorCallback.error(state, input);
        }
        return state;
    }

    @Override
    public FSMBacklogPair<Integer, Integer> backtrack() {
        if (backlog.isEmpty()) {
            return null;
        }
        FSMBacklogPair<Integer,Integer> pair = backlog.pop();
        Rollback<Integer,Integer> rollback = rollbackCallbacks
                [((pair.input()-lowerInputBound)*(upperStateBound-lowerStateBound+1))+(pair.state()-lowerStateBound)];
        if (rollback != null) {
            rollback.rollback(state, pair.input(), pair.state());
        }
        FSMBacklogPair<Integer,Integer> result = new FSMBacklogPair<>(state, pair.input());
        this.state = pair.state();
        return result;
    }

    static class Builder implements IBuilder<Integer,Integer> {
        private final int lowerStateBound, upperStateBound;
        private final int lowerInputBound, upperInputBound;
        private final int[] stateTable;
        private final StateTransitionCallback<Integer,Integer>[] transitions;
        private final Rollback<Integer,Integer>[] rollbacks;
        private ErrorCallback<Integer,Integer> error;
        private Integer initialState;

        Builder(int lowerStateBound, int upperStateBound, int lowerInputBound, int upperInputBound) {
            this.lowerStateBound = lowerStateBound;
            this.upperStateBound = upperStateBound;
            this.lowerInputBound = lowerInputBound;
            this.upperInputBound = upperInputBound;
            int size = (upperStateBound-lowerStateBound+1)*(upperInputBound-lowerInputBound+1);
            this.stateTable = new int[size];
            for (int i = 0; i < size; i++) {
                this.stateTable[i] = -1;
            }
            this.transitions = new StateTransitionCallback[size];
            this.rollbacks = new Rollback[size];
        }

        @Override
        public IBuilder<Integer, Integer> addTransition(Integer state, Integer input, Integer nextState, StateTransitionCallback<Integer, Integer> stateTransitionCallback, Rollback<Integer, Integer> rollbackCallback) {
            if (state < lowerStateBound || state > upperStateBound
                    || nextState < lowerInputBound || nextState > upperInputBound) {
                throw new InputOutOfRangeException(String.format("State %d out of range [%d,%d]", state, lowerStateBound, upperStateBound));
            }
            if (input < lowerInputBound || input > upperInputBound){
                throw new InputOutOfRangeException(String.format("Input %d is out of range [%d,%d]", input, lowerInputBound, upperInputBound));
            }
            int index = ((input-lowerInputBound)*(upperStateBound-lowerStateBound+1))+(state-lowerStateBound);
            stateTable[index] = nextState;
            transitions[index] = stateTransitionCallback;
            rollbacks[index] = rollbackCallback;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> initialState(Integer initialState) {
            if (initialState < lowerStateBound || initialState > upperStateBound) {
                throw new InputOutOfRangeException(String.format("State %d out of range [%d,%d]", initialState, lowerStateBound, upperStateBound));
            }
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<Integer, Integer> error(ErrorCallback<Integer, Integer> errorCallback) {
            this.error = errorCallback;
            return this;
        }

        @Override
        public FiniteStateMachine<Integer, Integer> build() {
            return new IntRangeStateMachine(lowerStateBound,upperStateBound,
                    lowerInputBound,upperInputBound,
                    stateTable,transitions,rollbacks,error,initialState);
        }
    }
}
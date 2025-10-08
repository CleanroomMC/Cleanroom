package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

import java.util.ArrayDeque;
import java.util.Deque;

class TableFiniteStateMachine<S, I> implements FiniteStateMachine<S, I> {

    protected final Table<I,S,S> stateTransitionTable;
    protected final Table<I,S, FiniteStateMachine.StateTransitionCallback<S,I>> stateTransitionCallbackTable;
    protected final Table<I,S,Rollback<S,I>> rollbackTable;
    protected final ErrorCallback<S,I> errorCallback;
    private S state;
    protected final Deque<FSMBacklogPair<S,I>> stack = new ArrayDeque<>();

    TableFiniteStateMachine(Table<I,S,S> stateTransitionTable,
                            Table<I,S, FiniteStateMachine.StateTransitionCallback<S,I>> stateTransitionCallbackTable,
                            Table<I,S,Rollback<S,I>> rollbackTable,
                            ErrorCallback<S,I> errorCallback,
                            S initialState) {
        this.stateTransitionTable = stateTransitionTable;
        this.stateTransitionCallbackTable = stateTransitionCallbackTable;
        this.rollbackTable = rollbackTable;
        this.errorCallback = errorCallback;
        this.state = initialState;
    }

    @Override
    public S accept(I input) {
        if (!this.stateTransitionTable.contains(input, state)) {
            if (this.errorCallback != null) {
                this.errorCallback.error(state, input);
            }
            return this.state;
        }
        S newState = stateTransitionTable.get(input, state);
        if (newState != null) {
            stack.push(new FSMBacklogPair<S,I>(this.state, input));
            StateTransitionCallback<S, I> callback = stateTransitionCallbackTable.get(input, state);
            if (callback != null)
                callback.transition(this.state, input, newState);
            this.state = newState;
        } else if (this.errorCallback != null) {
            this.errorCallback.error(state, input);
        }
        return this.state;
    }

    @Override
    public S state() {
        return this.state;
    }

    @Override
    public FSMBacklogPair<S,I> backtrack() {
        if (stack.isEmpty()) {
            return null;
        }
        FSMBacklogPair<S,I> entry = stack.pop();
        Rollback<S,I> rollback = this.rollbackTable.get(entry.input(), entry.state());
        if (rollback != null) {
            rollback.rollback(state, entry.input(), entry.state());
        }
        FSMBacklogPair<S,I> result = new FSMBacklogPair<>(state, entry.input());
        this.state = entry.state();
        return result;
    }

    static class Builder<S, I> implements IBuilder<S, I> {
        private final ImmutableTable.Builder<I,S,S> builder = ImmutableTable.builder();
        private final ImmutableTable.Builder<I,S, FiniteStateMachine.StateTransitionCallback<S,I>> transitionCallbackTable = ImmutableTable.builder();
        private final ImmutableTable.Builder<I,S,Rollback<S,I>> rollbackTable = ImmutableTable.builder();
        private S initialState = null;
        private ErrorCallback<S,I> error = null;

        Builder(){
        }

        @Override
        public Builder<S, I> addTransition(S currentState, I input, S nextState, FiniteStateMachine.StateTransitionCallback<S,I> callback, FiniteStateMachine.Rollback<S,I> rollback) {
            this.builder.put(input,currentState,nextState);
            if (callback != null) {
                this.transitionCallbackTable.put(input, currentState, callback);
            }
            if (rollback != null) {
                this.rollbackTable.put(input,currentState,rollback);
            }
            return this;
        }

        @Override
        public Builder<S, I> initialState(S initialState) {
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
            return new TableFiniteStateMachine<>(builder.buildOrThrow(),
                    transitionCallbackTable.buildOrThrow(),
                    rollbackTable.buildOrThrow(), error, initialState);
        }
    }
}
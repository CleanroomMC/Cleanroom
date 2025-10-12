package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

final class TableFiniteStateMachine<S, I> implements FiniteStateMachine<S, I> {

    private final Table<I, S, S> transitionMap;
    private final Map<S, OnEnterStateCallback<S,I>> entryCallbacks;
    private final Map<S, OnExitStateCallback<S,I>> exitCallbacks;
    private final Table<I,S,Rollback<S, I>> rollbackTable;
    private final ErrorCallback<S, I> errorCallback;
    private S state;
    private final Deque<FSMBacklogPair<S, I>> stack = new ArrayDeque<>();

    TableFiniteStateMachine(@NonNull Table<I, S, S> transitionMap,
                            @NonNull Map<S, OnEnterStateCallback<S,I>> entryCallbacks,
                            @NonNull Map<S, OnExitStateCallback<S,I>> exitCallbacks,
                            @NonNull Table<I, S,Rollback<S, I>> rollbackTable,
                            @Nullable ErrorCallback<S, I> errorCallback,
                            @NonNull S initialState) {
        this.transitionMap = transitionMap;
        this.entryCallbacks = entryCallbacks;
        this.exitCallbacks = exitCallbacks;
        this.rollbackTable = rollbackTable;
        this.errorCallback = errorCallback;
        this.state = initialState;
    }

    @NotNull
    @Override
    public Optional<S> accept(@NonNull I input) {
        Preconditions.checkNotNull(input, "Provided \"input\" can't be null");
        if (!this.transitionMap.containsRow(input)) {
            throw new IllegalArgumentException(String.format("%s is not a valid input", input));
        }
        if (!this.transitionMap.contains(input, state)) {
            if (this.errorCallback != null) {
                this.errorCallback.error(state, input);
            }
            return Optional.empty();
        }
        S newState = transitionMap.get(input, state);
        if (newState != null) {
            stack.push(new FSMBacklogPair<S, I>(this.state, input));
            if (exitCallbacks.containsKey(state)) {
                exitCallbacks.get(state).transition(this.state, input, newState);
            }
            if (entryCallbacks.containsKey(newState)) {
                entryCallbacks.get(newState).transition(this.state, input, newState);
            }
            this.state = newState;
        } else if (this.errorCallback != null) {
            this.errorCallback.error(state, input);
            return Optional.empty();
        }
        return Optional.of(this.state);
    }

    @NotNull
    @Override
    public S state() {
        return this.state;
    }

    @NotNull
    @Override
    public Optional<FSMBacklogPair<S, I>> backtrack() {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        FSMBacklogPair<S, I> entry = stack.pop();
        Rollback<S, I> rollback = this.rollbackTable.get(entry.input(), entry.state());
        if (rollback != null) {
            rollback.rollback(state, entry.input(), entry.state());
        }
        FSMBacklogPair<S, I> result = new FSMBacklogPair<>(state, entry.input());
        this.state = entry.state();
        return Optional.of(result);
    }

    @Override
    public void reset() {
        if (!stack.isEmpty()) {
            this.state = stack.pollLast().state();
            stack.clear();
        }
    }

    static class Builder<S, I> implements IBuilder<S, I> {
        private final ImmutableTable.Builder<I, S, S> builder = ImmutableTable.builder();
        private final ImmutableMap.Builder<S,OnEnterStateCallback<S,I>> entryCallbackMapBuilder = ImmutableMap.builder();
        private final ImmutableMap.Builder<S,OnExitStateCallback<S,I>> exitCallbackMapBuilder = ImmutableMap.builder();
        private final ImmutableTable.Builder<I,S,Rollback<S, I>> rollbackTable = ImmutableTable.builder();
        private S initialState = null;
        private ErrorCallback<S, I> error = null;

        Builder(){
        }

        @Override
        public Builder<S, I> addTransition(@NonNull S currentState, @NonNull I input, @NonNull S nextState,
                                           @Nullable OnEnterStateCallback<S, I> onEnterStateCallback,
                                           @Nullable OnExitStateCallback<S, I> onExitStateCallback,
                                           FiniteStateMachine.@Nullable Rollback<S, I> rollback) {
            Preconditions.checkNotNull(currentState, "Parameter \"currentState\" can't be null");
            Preconditions.checkNotNull(input, "Parameter \"input\" can't be null");
            Preconditions.checkNotNull(nextState,  "Parameter \"nextState\" can't be null");
            this.builder.put(input,currentState,nextState);
            if (onEnterStateCallback != null) {
                entryCallbackMapBuilder.put(nextState,onEnterStateCallback);
            }
            if (onExitStateCallback != null) {
                exitCallbackMapBuilder.put(nextState,onExitStateCallback);
            }
            if (rollback != null) {
                this.rollbackTable.put(input,currentState,rollback);
            }
            return this;
        }

        @Override
        public IBuilder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Provided \"state\" can't be null");
            this.entryCallbackMapBuilder.put(state, callback); // Tell me if I am allowed to suppress this
            return this;
        }

        @Override
        public IBuilder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S, I> callback) {
            Preconditions.checkNotNull(state, "Provided \"state\" can't be null");
            this.exitCallbackMapBuilder.put(state, callback);
            return this;
        }

        @Override
        public Builder<S, I> initialState(@NonNull S initialState) {
            Preconditions.checkNotNull(initialState, "Provided \"initialState\" can't be null");
            this.initialState = initialState;
            return this;
        }

        @Override
        public IBuilder<S, I> error(@NonNull ErrorCallback<S, I> errorCallback) {
            Preconditions.checkNotNull(errorCallback,
                    "Provided \"errorCallback\" can't be null, if you don't want to use a failure callback don't call this function");
            this.error = errorCallback;
            return this;
        }

        @Override
        public boolean validate() {
            Table<I,S,S> table = this.builder.build();
            Set<S> reachable = new HashSet<>();
            Deque<S> stack = new ArrayDeque<>();
            stack.push(initialState);
            while (!stack.isEmpty()) {
                S state = stack.pop();
                if (!reachable.contains(state)) {
                    reachable.add(state);
                    for (I input : table.rowKeySet()) {
                        if (table.contains(input,state)) {
                            stack.push(table.get(input,state));
                        }
                    }
                }
            }
            return reachable.size() == table.columnKeySet().size();
        }

        @Override
        public FiniteStateMachine<S, I> build() {
            Preconditions.checkNotNull(initialState, "The Initial State must be set before the FSM is built");
            return new TableFiniteStateMachine<>(builder.buildOrThrow(),
                    entryCallbackMapBuilder.build(),
                    exitCallbackMapBuilder.build(),
                    rollbackTable.buildOrThrow(), error, initialState);
        }
    }
}
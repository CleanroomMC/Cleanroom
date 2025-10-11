package com.cleanroommc.kirino.schemata.fsm;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Finite State Machine
 * @param <S> State type
 * @param <I> Input type
 * @implSpec Has to store a state transition table, transition callbacks, current state and a backlog.
 * @author Eerie
 */
public interface FiniteStateMachine<S, I> {
    /**
     * State getter
     * @return Current State
     */
    S state();

    /**
     * Accept input
     * @param input Input to the state machine
     * @return next state the FSM transitions to
     * @implSpec if state transition is successful, execute the {@link OnEnterStateCallback transition callback},
     * if it fails call the corresponding {@link ErrorCallback error callback} and return null
     */
    @Nullable
    S accept(@NonNull I input);

    /**
     * Backtrack the state machine to its previous state
     * @return The current state (the one that is backtracked from) and input leading towards it.
     * @implSpec The state machine has to store all it's previous inputs and states for backtracking purpose.
     * Execute rollback callback during backtracking.
     */
    @Nullable
    FSMBacklogPair<S, I> backtrack();

    default void reset() {
        while (backtrack() != null); // Might or might not end up as a temporary solution
    }

    @FunctionalInterface
    interface Rollback<S, I> {
        void rollback(S prevState, I input, S currState);
    }

    @FunctionalInterface
    interface ErrorCallback<S, I> {
        void error(S state, I input);
    }

    @FunctionalInterface
    interface OnEnterStateCallback<S, I> {
        void transition(S currState, I input, S nextState);
    }

    @FunctionalInterface
    interface OnExitStateCallback<S, I> {
        void transition(S currState, I input, S nextState);
    }

    interface IBuilder<S, I> {
        /**
         * Adds a possible transition to the FSM
         * @param state from
         * @param input the input causing the transition
         * @param nextState to
         * @param onEnterStateCallback executed after the transition occurs
         * @param rollbackCallback executed when the transition is undone
         * @return the builder
         */
        IBuilder<S, I> addTransition(S state,I input,S nextState,
                                    @Nullable OnEnterStateCallback<S,I> onEnterStateCallback,
                                    @Nullable OnExitStateCallback<S,I> onExitStateCallback,
                                    @Nullable Rollback<S,I> rollbackCallback);
        default IBuilder<S, I> addTransition(S state,I input,S nextState) {
            return addTransition(state,input,nextState,null,null,null);
        }
        default IBuilder<S, I> addTransition(S state,I input,S nextState,
                                            @NonNull OnEnterStateCallback<S,I> onEnterStateCallback,
                                            @NonNull OnExitStateCallback<S,I> onExitStateCallback) {
            return addTransition(state,input,nextState, onEnterStateCallback, onExitStateCallback, null);
        }
        default IBuilder<S, I> addTransition(S state,I input,S nextState,
                                             @NonNull OnEnterStateCallback<S,I> onEnterStateCallback) {
            return addTransition(state,input,nextState, onEnterStateCallback, null, null);
        }
        default IBuilder<S, I> addTransition(S state,I input,S nextState,
                                             @NonNull OnExitStateCallback<S,I> onExitStateCallback) {
            return addTransition(state,input,nextState, null, onExitStateCallback, null);
        }
        default IBuilder<S, I> addTransition(S state,I input,S nextState,
                                             @NonNull Rollback<S,I> rollbackCallback) {
            return addTransition(state,input,nextState,null,null,rollbackCallback);
        }
        /**
         * Sets the initial state, that the FSM will start in
         * @param initialState the initial state
         * @return the builder
         */
        IBuilder<S, I> initialState(S initialState);
        /**
         * Sets the error callback, that will be executed when a transition fails
         * @param errorCallback the error callback
         * @return the builder
         */
        IBuilder<S, I> error(ErrorCallback<S,I> errorCallback);
        /**
         * Finish instantiating the FSM
         * @return the FSM
         */
        FiniteStateMachine<S, I> build();
    }

    class Builder {
        public static <S extends Enum<S>, I extends Enum<I>> IBuilder<S, I> enumStateMachine(Class<S> stateClass, Class<I> inputClass) {
            return new EnumStateMachine.Builder<>(stateClass, inputClass);
        }

        public static <S, I> IBuilder<S, I> tableStateMachine() {
            return new TableFiniteStateMachine.Builder<>();
        }

        public static IBuilder<Integer, Integer> intRangeStateMachine(int lowerStateBound, int upperStateBound,
                                                                     int lowerInputBound, int upperInputBound) {
            return new IntRangeStateMachine.Builder(lowerStateBound, upperStateBound, lowerInputBound, upperInputBound);
        }

        public static <S extends Enum<S>> IBuilder<S, Integer> enumIntStateMachine(Class<S> stateClass,
                                                                                  int lowerInputBound, int upperInputBound) {
            return new EnumIntStateMachine.Builder<>(stateClass, lowerInputBound, upperInputBound);
        }

        public static <I extends Enum<I>> IBuilder<Integer, I> enumIntStateMachine(int lowerStateBound, int upperStateBound,
                                                                                   Class<I> inputClass) {
            return new IntEnumStateMachine.Builder<>(lowerStateBound, upperStateBound, inputClass);
        }
    }

    record FSMBacklogPair<S, I>(S state, I input) {
    }
}
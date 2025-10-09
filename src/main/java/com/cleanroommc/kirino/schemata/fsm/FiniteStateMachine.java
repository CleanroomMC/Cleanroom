package com.cleanroommc.kirino.schemata.fsm;

/**
 * Finite State Machine
 * @param <S> State type
 * @param <I> Input type
 * @implSpec Has to store a state transition table, transition callbacks, current state and a backlog.
 * @author Eerie
 */
public interface FiniteStateMachine<S,I> {
    /**
     * State getter
     * @return Current State
     */
    S state();

    /**
     * Accept input
     * @param input Input to the state machine
     * @return next state, the FSM transitions to
     * @implSpec if state transition is successful, execute the {@link StateTransitionCallback transition callback},
     * if it fails call the corresponding {@link ErrorCallback error callback}
     */
    S accept(I input);

    /**
     * Backtrack the state machine to its previous state
     * @return The current state (the one that is backtracked from) and input leading towards it.
     * @implSpec The state machine has to store all it's previous inputs and states for backtracking purpose.
     * Execute rollback callback during backtracking.
     */
    FSMBacklogPair<S,I> backtrack();

    @FunctionalInterface
    interface Rollback<S,I> {
        void rollback(S prevState, I input, S currState);
    }

    @FunctionalInterface
    interface ErrorCallback<S,I> {
        void error(S state, I input);
    }

    @FunctionalInterface
    interface StateTransitionCallback<S,I> {
        void transition(S currState, I input, S nextState);
    }

    interface IBuilder<S,I> {
        /**
         * Adds a possible transition to the FSM
         * @param state from
         * @param input the input causing the transition
         * @param nextState to
         * @param stateTransitionCallback executed after the transition occurs
         * @param rollbackCallback executed when the transition is undone
         * @return the builder
         */
        IBuilder<S,I> addTransition(S state,I input,S nextState,
                                    StateTransitionCallback<S,I> stateTransitionCallback,
                                    Rollback<S,I> rollbackCallback);
        default IBuilder<S,I> addTransition(S state,I input,S nextState) {
            return addTransition(state,input,nextState,null,null);
        }
        default IBuilder<S,I> addTransition(S state,I input,S nextState,
                                            StateTransitionCallback<S,I> stateTransitionCallback) {
            return addTransition(state,input,nextState,stateTransitionCallback,null);
        }
        default IBuilder<S,I> addTransition(S state,I input,S nextState,
                                            Rollback<S,I> rollbackCallback) {
            return addTransition(state,input,nextState,null,rollbackCallback);
        }
        /**
         * Sets the initial state, that the FSM will start in
         * @param initialState the initial state
         * @return the builder
         */
        IBuilder<S,I> initialState(S initialState);
        /**
         * Sets the error callback, that will be executed when a transition fails
         * @param errorCallback the error callback
         * @return the builder
         */
        IBuilder<S,I> error(ErrorCallback<S,I> errorCallback);
        /**
         * Finish instantiating the FSM
         * @return the FSM
         */
        FiniteStateMachine<S,I> build();
    }

    class Builder {
        public static <S extends Enum<S>, I extends Enum<I>> IBuilder<S,I> enumStateMachine(Class<S> stateClass, Class<I> inputClass) {
            return new EnumStateMachine.Builder<>(stateClass, inputClass);
        }

        public static <S,I> IBuilder<S,I> tableStateMachine() {
            return new TableFiniteStateMachine.Builder<>();
        }

        public static IBuilder<Integer,Integer> intRangeStateMachine(int lowerStateBound, int upperStateBound,
                                                                     int lowerInputBound, int upperInputBound) {
            return new IntRangeStateMachine.Builder(lowerStateBound, upperStateBound, lowerInputBound, upperInputBound);
        }

        public static <S extends Enum<S>> IBuilder<S,Integer> enumIntStateMachine(Class<S> stateClass,
                                                                                  int lowerInputBound, int upperInputBound) {
            return new EnumIntStateMachine.Builder<>(stateClass, lowerInputBound, upperInputBound);
        }

        public static <I extends Enum<I>> IBuilder<Integer, I> enumIntStateMachine(int lowerStateBound, int upperStateBound,
                                                                                   Class<I> inputClass) {
            return new IntEnumStateMachine.Builder<>(lowerStateBound, upperStateBound, inputClass);
        }
    }

    record FSMBacklogPair<T1,T2>(T1 state, T2 input) {
    }
}
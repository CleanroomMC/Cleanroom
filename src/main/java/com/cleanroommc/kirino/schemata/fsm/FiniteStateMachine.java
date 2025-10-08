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
        IBuilder<S,I> addTransition(S state,I input,S nextState,
                                    StateTransitionCallback<S,I> stateTransitionCallback,
                                    Rollback<S,I> rollbackCallback);
        IBuilder<S,I> initialState(S initialState);
        IBuilder<S,I> error(ErrorCallback<S,I> errorCallback);
        FiniteStateMachine<S,I> build();
    }

    class Builder {
        public static <S extends Enum<S>, I extends Enum<I>> IBuilder<S,I> enumStateMachine(Class<S> stateClass, Class<I> inputClass) {
            return new EnumStateMachine.Builder<>(stateClass, inputClass);
        }

        public static <S,I> IBuilder<S,I> tableStateMachine() {
            return new TableFiniteStateMachine.Builder<>();
        }
    }

    record FSMBacklogPair<T1,T2>(T1 state, T2 input) {
    }
}
package com.cleanroommc.kirino.schemata.fsm;

import com.google.common.base.Preconditions;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

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
    @NonNull
    S state();

    /**
     * Accept input
     * @param input Input to the state machine
     * @return next state the FSM transitions to
     * @implSpec if state transition is successful, execute the {@link OnEnterStateCallback transition callback},
     * if it fails call the corresponding {@link ErrorCallback error callback} and return an empty optional
     */
    @NonNull
    Optional<S> accept(@NonNull I input);

    /**
     * Backtrack the state machine to its previous state
     * @return The current state (the one that is backtracked from) and input leading towards it.
     * @implSpec The state machine has to store all it's previous inputs and states for backtracking purpose.
     * Execute rollback callback during backtracking.
     */
    @NonNull
    Optional<FSMBacklogPair<S, I>> backtrack();

    /**
     * Sets the state to the initial state and clears the backlog
     * @implSpec <code>
     *     if (!stack.isEmpty()) { <br/>
     *         &emsp;    this.state = stack.pollLast().state(); <br/>
     *         &emsp;    stack.clear(); <br/>
     *         }
     * </code>
     */
    void reset();

    /**
     * A quick way to undo all the actions taken by the entire state machine
     */
    default void rewind() {
        while (backtrack().isPresent());
    }

    @FunctionalInterface
    interface Rollback<S, I> {
        /**
         * Undo the changes made by the {@link OnEnterStateCallback} and {@link OnExitStateCallback}
         * @param prevState State from which the function backtracks
         * @param input the input that caused the backtracked transition
         * @param currState the state that the FSM backtracks to
         */
        void rollback(@NonNull S prevState, @NonNull I input, @NonNull S currState);
    }

    @FunctionalInterface
    interface ErrorCallback<S, I> {
        /**
         * Transition failure callback
         * @param state the current state
         * @param input the input that caused the failure
         */
        void error(@NonNull S state, @NonNull I input);
    }

    @FunctionalInterface
    interface OnEnterStateCallback<S, I> {
        /**
         * Called while transitioning to a state
         * @param prevState The state the FSM is transitioning from
         * @param input the input causing the transition
         * @param nextState the state the input is transitioning towards
         * @apiNote executed after {@link OnExitStateCallback}
         */
        void transition(@NonNull S prevState, @NonNull I input, @NonNull S nextState);
    }

    @FunctionalInterface
    interface OnExitStateCallback<S, I> {
        /**
         * Called while exiting a state
         * @param currState The state the FSM is transitioning from
         * @param input the input causing the transition
         * @param nextState the state the input is transitioning towards
         * @apiNote executed before {@link OnEnterStateCallback}
         */
        void transition(@NonNull S currState, @NonNull I input, @NonNull S nextState);
    }

    interface IBuilder<S, I> {
        /**
         * Adds a possible transition to the FSM
         * @param state from
         * @param input the input causing the transition
         * @param nextState to
         * @param onEnterStateCallback executed during transition to nextState.
         *                             If null is passed to this parameter. Nothing will change.
         *                             If a value is passed to this parameter. The {@link OnEnterStateCallback}
         *                             for this state will be changed. <b>This callback is stored per state</b>
         * @param onExitStateCallback executed during transition from state.
         *                            If null is passed to this parameter. Nothing will change.
         *                            If a value is passed to this parameter. The {@link OnExitStateCallback}
         *                            for this state will be changed. <b>This callback is stored per state</b>
         * @param rollbackCallback executed during {@link FiniteStateMachine#backtrack()} to reverse changes caused by the other callbacks.
         *                         <b>This callback is stored per transition,
         *                         it is different for each state and input combination.</b>
         * @throws IllegalStateException if the state or input are non-existent in the set of states/inputs this exception will be thrown
         * @return the builder
         */
        IBuilder<S, I> addTransition(@NonNull S state,@NonNull I input,@NonNull S nextState,
                                    @Nullable OnEnterStateCallback<S,I> onEnterStateCallback,
                                    @Nullable OnExitStateCallback<S,I> onExitStateCallback,
                                    @Nullable Rollback<S,I> rollbackCallback);
        /**
         * Sets the entry callback for a state
         * @param state The state for which the callback will be set.
         * @param callback The callback to be set for the state, unlike
         * {@link IBuilder#addTransition(Object, Object, Object, OnEnterStateCallback, OnExitStateCallback, Rollback)}
         *                 this method <b>does in fact invalidate a callback when this parameter is equal to null.</b>
         * @throws IllegalStateException if inputState is non-existent in the set of states this exception will be thrown
         * @return the builder
         */
        IBuilder<S, I> setEntryCallback(@NonNull S state, @Nullable OnEnterStateCallback<S,I> callback);
        /**
         * Sets the exit callback for a state
         * @param state The state for which the callback will be set.
         * @param callback The callback to be set for the state, unlike
         * {@link IBuilder#addTransition(Object, Object, Object, OnEnterStateCallback, OnExitStateCallback, Rollback)}
         *                 this method <b>does in fact invalidate a callback when this parameter is equal to null.</b>
         * @throws IllegalStateException if inputState is non-existent in the set of states this exception will be thrown
         * @return the builder
         */
        IBuilder<S, I> setExitCallback(@NonNull S state, @Nullable OnExitStateCallback<S,I> callback);
        /**
         * Sets the initial state, that the FSM will start in. <br/>
         * <b>MUST BE CALLED BEFORE {@link IBuilder#build()}!!!</b>
         * @param initialState the initial state
         * @return the builder
         * @throws IllegalStateException if inputState is non-existent in the set of states this exception will be thrown
         */
        IBuilder<S, I> initialState(@NonNull S initialState);
        /**
         * Sets the error callback which will be executed if and only if a transition fails
         * due to an input that leads to a non-existent route. An exception will be thrown if
         * the input itself is invalid.
         * @param errorCallback the error callback
         * @return the builder
         */
        IBuilder<S, I> error(@NonNull ErrorCallback<S,I> errorCallback);
        /**
         * @return If the FSM has states that can't be reached from any other state while not being the initial state,
         * return true, otherwise false
         * @implSpec use DFS or another graph traversal algorithm
         */
        boolean validate();
        /**
         * Finish instantiating the FSM. <br/>
         * <b>{@link IBuilder#initialState(Object)} must be called before this!!!</b>
         * @return the FSM
         */
        FiniteStateMachine<S, I> build();

        // Defaults
        default IBuilder<S, I> addTransition(@NonNull S state, @NonNull I input, @NonNull S nextState) {
            return addTransition(state,input,nextState,null,null,null);
        }
        default IBuilder<S, I> addTransition(@NonNull S state,@NonNull I input,@NonNull S nextState,
                                             @NonNull OnEnterStateCallback<S,I> onEnterStateCallback,
                                             @NonNull OnExitStateCallback<S,I> onExitStateCallback) {
            Preconditions.checkNotNull(onEnterStateCallback);
            Preconditions.checkNotNull(onExitStateCallback);
            return addTransition(state,input,nextState, onEnterStateCallback, onExitStateCallback, null);
        }
        default IBuilder<S, I> addTransition(@NonNull S state,@NonNull I input,@NonNull S nextState,
                                             @NonNull OnEnterStateCallback<S,I> onEnterStateCallback) {
            Preconditions.checkNotNull(onEnterStateCallback);
            return addTransition(state,input,nextState, onEnterStateCallback, null, null);
        }
        default IBuilder<S, I> addTransition(@NonNull S state,@NonNull I input,@NonNull S nextState,
                                             @NonNull OnExitStateCallback<S,I> onExitStateCallback) {
            Preconditions.checkNotNull(onExitStateCallback);
            return addTransition(state,input,nextState, null, onExitStateCallback, null);
        }
        default IBuilder<S, I> addTransition(@NonNull S state,@NonNull I input,@NonNull S nextState,
                                             @NonNull Rollback<S,I> rollbackCallback) {
            Preconditions.checkNotNull(rollbackCallback);
            return addTransition(state,input,nextState,null,null,rollbackCallback);
        }
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
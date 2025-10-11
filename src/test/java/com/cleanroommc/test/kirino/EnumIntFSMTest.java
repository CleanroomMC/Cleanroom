package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EnumIntFSMTest {
    @Test
    void transitionTest() {
        FiniteStateMachine<State, Integer> FSM = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 1, 3)
                .addTransition(State.STATE1, 2, State.STATE2)
                .addTransition(State.STATE2, 3, State.STATE3)
                .addTransition(State.STATE3, 1, State.STATE1)
                .addTransition(State.STATE2, 1, State.STATE1)
                .initialState(State.STATE1)
                .build();
        assertEquals(State.STATE1,FSM.state());
        FSM.accept(2);
        assertEquals(State.STATE2,FSM.state());
        FSM.accept(3);
        assertEquals(State.STATE3,FSM.state());
        FSM.accept(1);
        assertEquals(State.STATE1,FSM.state());
        FSM.accept(2);
        assertEquals(State.STATE2,FSM.state());
        FSM.accept(1);
        assertEquals(State.STATE1,FSM.state());
    }

    @Test
    void transitionCallbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.OnEnterStateCallback<State, Integer> callback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<State, Integer> FSM = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 1, 3)
                .addTransition(State.STATE1, 2, State.STATE2, callback)
                .addTransition(State.STATE2, 3, State.STATE3, callback)
                .addTransition(State.STATE3, 1, State.STATE1, callback)
                .addTransition(State.STATE2, 1, State.STATE1, callback)
                .initialState(State.STATE1)
                .build();
        FSM.accept(2);
        assertEquals(2, tester.get());
        FSM.accept(3);
        assertEquals(3, tester.get());
        FSM.accept(1);
        assertEquals(1, tester.get());
        FSM.accept(2);
        assertEquals(2, tester.get());
        FSM.accept(1);
        assertEquals(1, tester.get());
    }

    @Test
    void backtrackTest() {
        FiniteStateMachine<State, Integer> FSM = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 1, 3)
                .addTransition(State.STATE1, 2, State.STATE2)
                .addTransition(State.STATE2, 3, State.STATE3)
                .addTransition(State.STATE3, 1, State.STATE1)
                .addTransition(State.STATE2, 1, State.STATE1)
                .initialState(State.STATE1)
                .build();
        FSM.accept(2);
        FSM.accept(3);
        FSM.accept(1);
        FSM.accept(2);
        FSM.accept(1);
        State[] expectedStates = {State.STATE1, State.STATE2, State.STATE1, State.STATE3, State.STATE2, State.STATE1};
        int[] expectedInputs = {1,2,1,3,2};
        for (int i = 0; i < 5; i++) {
            FiniteStateMachine.FSMBacklogPair<State,Integer> pair = FSM.backtrack();
            assertNotNull(pair);
            assertEquals(expectedStates[i], pair.state());
            assertEquals(expectedInputs[i], pair.input());
        }
    }

    @Test
    void rollbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.Rollback<State, Integer> rollback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<State, Integer> FSM = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 1, 3)
                .addTransition(State.STATE1, 2, State.STATE2, rollback)
                .addTransition(State.STATE2, 3, State.STATE3, rollback)
                .addTransition(State.STATE3, 1, State.STATE1, rollback)
                .addTransition(State.STATE2, 1, State.STATE1, rollback)
                .initialState(State.STATE1)
                .build();
        FSM.accept(2);
        FSM.accept(3);
        FSM.accept(1);
        FSM.accept(2);
        FSM.accept(1);
        int[] expectedInputs = {1,2,1,3,2};
        for (int i = 0; i < 5; i++) {
            FSM.backtrack();
            assertEquals(expectedInputs[i], tester.get());
        }
    }

    @Test
    void errorTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.ErrorCallback<State,Integer> errorCallback = (state, input) -> tester.set(input);
        FiniteStateMachine<State, Integer> FSM = FiniteStateMachine.Builder.enumIntStateMachine(State.class, 1, 3)
                .addTransition(State.STATE1, 2, State.STATE2)
                .addTransition(State.STATE2, 3, State.STATE3)
                .addTransition(State.STATE3, 1, State.STATE1)
                .addTransition(State.STATE2, 1, State.STATE1)
                .initialState(State.STATE1).error(errorCallback)
                .build();
        FSM.accept(2);
        FSM.accept(3);
        FSM.accept(2);
        assertEquals(2, tester.get());
        FSM.accept(1);
        FSM.accept(2);
        FSM.accept(1);
        FSM.accept(3);
        assertEquals(3, tester.get());
    }

    private enum State {
        STATE1,STATE2,STATE3;
    }
}

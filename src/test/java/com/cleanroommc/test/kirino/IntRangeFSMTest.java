package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class IntRangeFSMTest {
    @Test
    void transitionTest() {
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                4,6).addTransition(1,5,2)
                .addTransition(2,4,1)
                .addTransition(2,6,3)
                .addTransition(3,4,1)
                .initialState(1)
                .build();
        assertEquals(1, FSM.state());
        FSM.accept(5);
        assertEquals(2, FSM.state());
        FSM.accept(6);
        assertEquals(3, FSM.state());
        FSM.accept(4);
        assertEquals(1, FSM.state());
        FSM.accept(5);
        assertEquals(2, FSM.state());
        FSM.accept(4);
        assertEquals(1, FSM.state());
    }

    @Test
    void entryCallbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.OnEnterStateCallback<Integer,Integer> callback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2,callback)
                .addTransition(2,4,1,callback)
                .addTransition(2,6,3,callback)
                .addTransition(3,4,1,callback)
                .initialState(1)
                .build();
        FSM.accept(5);
        assertEquals(5, tester.get());
        FSM.accept(6);
        assertEquals(6, tester.get());
        FSM.accept(4);
        assertEquals(4, tester.get());
        FSM.accept(5);
        assertEquals(5, tester.get());
        FSM.accept(4);
        assertEquals(4, tester.get());
    }

    @Test
    void exitCallbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.OnExitStateCallback<Integer,Integer> callback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2,callback)
                .addTransition(2,4,1,callback)
                .addTransition(2,6,3,callback)
                .addTransition(3,4,1,callback)
                .initialState(1)
                .build();
        FSM.accept(5);
        assertEquals(5, tester.get());
        FSM.accept(6);
        assertEquals(6, tester.get());
        FSM.accept(4);
        assertEquals(4, tester.get());
        FSM.accept(5);
        assertEquals(5, tester.get());
        FSM.accept(4);
        assertEquals(4, tester.get());
    }

    @Test
    void backtrackTest() {
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2)
                .addTransition(2,4,1)
                .addTransition(2,6,3)
                .addTransition(3,4,1)
                .initialState(1)
                .build();
        FSM.accept(5);
        FSM.accept(6);
        FSM.accept(4);
        FSM.accept(5);
        FSM.accept(4);
        int[] expectedStates = {1,2,1,3,2};
        int[] expectedInputs = {4,5,4,6,5};
        for(int i = 0;i<5;i++) {
            Optional<FiniteStateMachine.FSMBacklogPair<Integer, Integer>> backlog = FSM.backtrack();
            assertTrue(backlog.isPresent());
            assertEquals(expectedStates[i], backlog.get().state());
            assertEquals(expectedInputs[i], backlog.get().input());
        }
    }

    @Test
    void rollbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.Rollback<Integer,Integer> rollback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6)
                .addTransition(1,5,2, rollback)
                .addTransition(2,4,1, rollback)
                .addTransition(2,6,3, rollback)
                .addTransition(3,4,1, rollback)
                .initialState(1)
                .build();
        FSM.accept(5);
        FSM.accept(6);
        FSM.accept(4);
        FSM.accept(5);
        FSM.accept(4);
        int[] expectedInputs = {4,5,4,6,5};
        for (int i = 0; i < 5; i++) {
            FSM.backtrack();
            assertEquals(expectedInputs[i], tester.get());
        }
    }

    @Test
    void errorTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.ErrorCallback<Integer,Integer> errorCallback = (state, input) -> tester.set(input);
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2)
                .addTransition(2,4,1)
                .addTransition(2,6,3)
                .addTransition(3,4,1)
                .initialState(1).error(errorCallback)
                .build();
        FSM.accept(5);
        FSM.accept(6);
        FSM.accept(4);
        FSM.accept(6);
        assertEquals(6, tester.get());
        FSM.accept(5);
        FSM.accept(4);
        FSM.accept(4);
        assertEquals(4, tester.get());
    }

    @Test
    void validateTest() {
        assertTrue(FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2)
                .addTransition(2,4,1)
                .addTransition(2,6,3)
                .addTransition(3,4,1)
                .initialState(1).validate());
        assertFalse(FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2)
                .addTransition(2,4,1)
                .addTransition(2,6,3)
                .addTransition(3,4,1)
                .initialState(1).validate());
    }
}

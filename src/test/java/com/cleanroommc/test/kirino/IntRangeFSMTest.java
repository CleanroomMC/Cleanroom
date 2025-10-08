package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IntRangeFSMTest {
    @Test
    void transitionTest() {
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                4,6).addTransition(1,5,2,null,null)
                .addTransition(2,4,1,null,null)
                .addTransition(2,6,3,null,null)
                .addTransition(3,4,1,null,null)
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
    void transitionCallbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.StateTransitionCallback<Integer,Integer> callback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6).addTransition(1,5,2,callback,null)
                .addTransition(2,4,1,callback,null)
                .addTransition(2,6,3,callback,null)
                .addTransition(3,4,1,callback,null)
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
                        4,6).addTransition(1,5,2,null,null)
                .addTransition(2,4,1,null,null)
                .addTransition(2,6,3,null,null)
                .addTransition(3,4,1,null,null)
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
            FiniteStateMachine.FSMBacklogPair<Integer,Integer> backlog = FSM.backtrack();
            assertEquals(expectedStates[i], backlog.state());
            assertEquals(expectedInputs[i], backlog.input());
        }
    }

    @Test
    void rollbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.Rollback<Integer,Integer> rollback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer,Integer> FSM = FiniteStateMachine.Builder.intRangeStateMachine(1,3,
                        4,6)
                .addTransition(1,5,2,null,rollback)
                .addTransition(2,4,1,null,rollback)
                .addTransition(2,6,3,null,rollback)
                .addTransition(3,4,1,null,rollback)
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
                        4,6).addTransition(1,5,2,null,null)
                .addTransition(2,4,1,null,null)
                .addTransition(2,6,3,null,null)
                .addTransition(3,4,1,null,null)
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
}

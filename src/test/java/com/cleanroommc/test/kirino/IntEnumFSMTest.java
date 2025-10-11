package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class IntEnumFSMTest {
    @Test
    void transitionTest() {
        FiniteStateMachine<Integer, Input> FSM = FiniteStateMachine.Builder.enumIntStateMachine(1,3,Input.class)
                .addTransition(1,Input.SECOND,2)
                .addTransition(2,Input.THIRD,3)
                .addTransition(3,Input.FIRST,1)
                .addTransition(2,Input.FIRST,1)
                .initialState(1)
                .build();
        assertEquals(1,FSM.state());
        FSM.accept(Input.SECOND);
        assertEquals(2,FSM.state());
        FSM.accept(Input.THIRD);
        assertEquals(3,FSM.state());
        FSM.accept(Input.FIRST);
        assertEquals(1,FSM.state());
        FSM.accept(Input.SECOND);
        assertEquals(2,FSM.state());
        FSM.accept(Input.FIRST);
        assertEquals(1,FSM.state());
    }

    @Test
    void transitionCallbackTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.OnEnterStateCallback<Integer,Input> callback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer, Input> FSM = FiniteStateMachine.Builder.enumIntStateMachine(1,3,Input.class)
                .addTransition(1,Input.SECOND,2, callback)
                .addTransition(2,Input.THIRD,3, callback)
                .addTransition(3,Input.FIRST,1, callback)
                .addTransition(2,Input.FIRST,1, callback)
                .initialState(1)
                .build();
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND, tester.get());
        FSM.accept(Input.THIRD);
        assertEquals(Input.THIRD, tester.get());
        FSM.accept(Input.FIRST);
        assertEquals(Input.FIRST, tester.get());
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND, tester.get());
        FSM.accept(Input.FIRST);
        assertEquals(Input.FIRST, tester.get());
    }

    @Test
    void backtrackTest() {
        FiniteStateMachine<Integer, Input> FSM = FiniteStateMachine.Builder.enumIntStateMachine(1,3,Input.class)
                .addTransition(1,Input.SECOND,2)
                .addTransition(2,Input.THIRD,3)
                .addTransition(3,Input.FIRST,1)
                .addTransition(2,Input.FIRST,1)
                .initialState(1)
                .build();
        FSM.accept(Input.SECOND);
        FSM.accept(Input.THIRD);
        FSM.accept(Input.FIRST);
        FSM.accept(Input.SECOND);
        FSM.accept(Input.FIRST);
        int[] expectedStates = {1,2,1,3,2};
        Input[] expectedInputs = {Input.FIRST,Input.SECOND,Input.FIRST,Input.THIRD,Input.SECOND};
        for(int i = 0; i < 5; i++) {
            FiniteStateMachine.FSMBacklogPair<Integer,Input> backlog = FSM.backtrack();
            assertNotNull(backlog);
            assertEquals(expectedStates[i], backlog.state());
            assertEquals(expectedInputs[i], backlog.input());
        }
    }

    @Test
    void rollbackTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.Rollback<Integer,Input> rollback = (_, input, _) -> tester.set(input);
        FiniteStateMachine<Integer, Input> FSM = FiniteStateMachine.Builder.enumIntStateMachine(1,3,Input.class)
                .addTransition(1,Input.SECOND,2, rollback)
                .addTransition(2,Input.THIRD,3, rollback)
                .addTransition(3,Input.FIRST,1, rollback)
                .addTransition(2,Input.FIRST,1, rollback)
                .initialState(1)
                .build();
        FSM.accept(Input.SECOND);
        FSM.accept(Input.THIRD);
        FSM.accept(Input.FIRST);
        FSM.accept(Input.SECOND);
        FSM.accept(Input.FIRST);
        Input[] expectedInputs = {Input.FIRST,Input.SECOND,Input.FIRST,Input.THIRD,Input.SECOND};
        for(int i = 0; i < 5; i++) {
            FSM.backtrack();
            assertEquals(expectedInputs[i], tester.get());
        }
    }

    @Test
    void errorTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.ErrorCallback<Integer,Input> errorCallback = (_, input) -> tester.set(input);
        FiniteStateMachine<Integer, Input> FSM = FiniteStateMachine.Builder.enumIntStateMachine(1,3,Input.class)
                .addTransition(1,Input.SECOND,2)
                .addTransition(2,Input.THIRD,3)
                .addTransition(3,Input.FIRST,1)
                .addTransition(2,Input.FIRST,1)
                .initialState(1).error(errorCallback)
                .build();
        FSM.accept(Input.SECOND);
        FSM.accept(Input.THIRD);
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND, tester.get());
        FSM.accept(Input.FIRST);
        FSM.accept(Input.SECOND);
        FSM.accept(Input.FIRST);
        FSM.accept(Input.THIRD);
        assertEquals(Input.THIRD, tester.get());
    }

    private enum Input {
        FIRST,SECOND,THIRD;
    }
}

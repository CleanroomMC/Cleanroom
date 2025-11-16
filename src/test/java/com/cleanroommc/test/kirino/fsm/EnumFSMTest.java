package com.cleanroommc.test.kirino.fsm;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

public class EnumFSMTest {

    @Test
    void transitionTest() {
        FiniteStateMachine<State, Input> FSM = FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1)
                .initialState(State.STATE1)
                .build();
        assertEquals(State.STATE1,FSM.state());
        FSM.accept(Input.SECOND);
        assertEquals(State.STATE2,FSM.state());
        FSM.accept(Input.THIRD);
        assertEquals(State.STATE3,FSM.state());
        FSM.accept(Input.FIRST);
        assertEquals(State.STATE1,FSM.state());
        FSM.accept(Input.SECOND);
        assertEquals(State.STATE2,FSM.state());
        FSM.accept(Input.FIRST);
        assertEquals(State.STATE1,FSM.state());
    }

    @Test
    void entryCallbackTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.OnEnterStateCallback<State,Input> callback = (_ignored0, input, _ignored1) -> {
            tester.set(input);
        };
        FiniteStateMachine<State, Input> FSM = FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2,callback)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3,callback)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1,callback)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1,callback)
                .initialState(State.STATE1)
                .build();
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND,tester.get());
        FSM.accept(Input.THIRD);
        assertEquals(Input.THIRD,tester.get());
        FSM.accept(Input.FIRST);
        assertEquals(Input.FIRST,tester.get());
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND,tester.get());
        FSM.accept(Input.FIRST);
    }

    @Test
    void exitCallbackTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.OnExitStateCallback<State,Input> callback = (_ignored0, input, _ignored1) -> {
            tester.set(input);
        };
        FiniteStateMachine<State, Input> FSM = FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2,callback)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3,callback)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1,callback)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1,callback)
                .initialState(State.STATE1)
                .build();
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND,tester.get());
        FSM.accept(Input.THIRD);
        assertEquals(Input.THIRD,tester.get());
        FSM.accept(Input.FIRST);
        assertEquals(Input.FIRST,tester.get());
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND,tester.get());
        FSM.accept(Input.FIRST);
    }

    @Test
    void backtrackTest() {
        FiniteStateMachine<State, Input> FSM = FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1)
                .initialState(State.STATE1)
                .build();
        //System.out.println(FSM.state());
        FSM.accept(Input.SECOND); // backlog: STATE1
        //System.out.println(FSM.state());
        FSM.accept(Input.THIRD); // backlog: STATE1, STATE2
        //System.out.println(FSM.state());
        FSM.accept(Input.FIRST); // backlog: STATE1, STATE2, STATE3
        //System.out.println(FSM.state());
        FSM.accept(Input.SECOND); // backlog: STATE1, STATE2, STATE3, STATE1
        //System.out.println(FSM.state());
        FSM.accept(Input.FIRST); // backlog: STATE1, STATE2, STATE3, STATE1, STATE2
        //System.out.println(FSM.state());
        State[] expectedStates = {State.STATE1,State.STATE2,State.STATE1,State.STATE3,State.STATE2};
        Input[] expectedInputs = {Input.FIRST, Input.SECOND, Input.FIRST, Input.THIRD, Input.SECOND};
        for (int i = 0; i < 5; i++) {
            Optional<FiniteStateMachine.FSMBacklogPair<State, Input>> pair = FSM.backtrack();
            assertTrue(pair.isPresent());
            //System.out.println(pair);
            assertEquals(expectedStates[i], pair.get().state());
            assertEquals(expectedInputs[i], pair.get().input());
        }
    }

    @Test
    void rollbackTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.Rollback<State,Input> rollback = (_ignored0, input, _ignored1) -> {
            tester.set(input);
        };
        FiniteStateMachine<State, Input> FSM = FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2,rollback)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3,rollback)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1,rollback)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1,rollback)
                .initialState(State.STATE1)
                .build();
        FSM.accept(Input.SECOND);
        FSM.accept(Input.THIRD);
        FSM.accept(Input.FIRST);
        FSM.accept(Input.SECOND);
        FSM.accept(Input.FIRST);
        Input[] expectedInputs = {Input.FIRST, Input.SECOND, Input.FIRST, Input.THIRD, Input.SECOND};
        for (int i = 0; i < 5; i++) {
            FSM.backtrack();
            assertEquals(expectedInputs[i], tester.get());
        }
    }

    @Test
    void errorTest() {
        AtomicReference<Input> tester = new AtomicReference<>();
        FiniteStateMachine.ErrorCallback<State,Input> errorCallback = (_ignored0, input) -> tester.set(input);
        FiniteStateMachine<State, Input> FSM = FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1)
                .initialState(State.STATE1).error(errorCallback)
                .build();
        FSM.accept(Input.SECOND);
        FSM.accept(Input.THIRD);
        FSM.accept(Input.SECOND);
        assertEquals(Input.SECOND,tester.get());
        FSM.accept(Input.FIRST);
        FSM.accept(Input.THIRD);
        assertEquals(Input.THIRD,tester.get());
        FSM.accept(Input.SECOND);
        FSM.accept(Input.FIRST);
    }

    @Test
    void validateTest() {
        assertTrue(FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2)
                .addTransition(State.STATE2,Input.THIRD,State.STATE3)
                .addTransition(State.STATE3,Input.FIRST,State.STATE1)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1)
                .initialState(State.STATE1).validate());
        assertFalse(FiniteStateMachine.Builder.
                        <State,Input>enumStateMachine(State.class,Input.class)
                .addTransition(State.STATE1,Input.SECOND,State.STATE2)
                .addTransition(State.STATE2,Input.FIRST,State.STATE1)
                .initialState(State.STATE1).validate());
    }

    private enum State {
        STATE1,STATE2,STATE3;
    }

    private enum Input {
        FIRST,SECOND,THIRD;
    }
}

package com.cleanroommc.test.kirino;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TableFSMTest {

    @Test
    void transitionTest() {
        FiniteStateMachine<String, Integer> FSM = FiniteStateMachine.Builder.<String, Integer>tableStateMachine()
                .addTransition("state1",2,"state2",null,null)
                .addTransition("state2",1,"state1",null,null)
                .addTransition("state2",3,"state3",null,null)
                .addTransition("state3",1,"state1",null,null)
                .initialState("state1")
                .build();
        assertEquals("state1",FSM.state());
        FSM.accept(2);
        assertEquals("state2",FSM.state());
        FSM.accept(3);
        assertEquals("state3",FSM.state());
        FSM.accept(1);
        assertEquals("state1",FSM.state());
        FSM.accept(2);
        assertEquals("state2",FSM.state());
        FSM.accept(1);
        assertEquals("state1",FSM.state());
    }

    @Test
    void transitionCallbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.StateTransitionCallback<String, Integer> callback = (currState, input, nextState) -> {
            tester.set(input);
        };
        FiniteStateMachine<String, Integer> FSM = FiniteStateMachine.Builder.<String, Integer>tableStateMachine()
                .addTransition("state1",2,"state2",callback,null)
                .addTransition("state2",1,"state1",callback,null)
                .addTransition("state2",3,"state3",callback,null)
                .addTransition("state3",1,"state1",callback,null)
                .initialState("state1")
                .build();
        FSM.accept(2);
        assertEquals(2,tester.get());
        FSM.accept(3);
        assertEquals(3,tester.get());
        FSM.accept(1);
        assertEquals(1,tester.get());
        FSM.accept(2);
        assertEquals(2,tester.get());
        FSM.accept(1);
        assertEquals(1,tester.get());
    }

    @Test
    void backtrackTest() {
        FiniteStateMachine<String, Integer> FSM = FiniteStateMachine.Builder.<String, Integer>tableStateMachine()
                .addTransition("state1",2,"state2",null,null)
                .addTransition("state2",1,"state1",null,null)
                .addTransition("state2",3,"state3",null,null)
                .addTransition("state3",1,"state1",null,null)
                .initialState("state1")
                .build();
        FSM.accept(2);
        FSM.accept(3);
        FSM.accept(1);
        FSM.accept(2);
        FSM.accept(1);
        String[] expectedStates = {"state1","state2","state1","state3","state2"};
        int[] inputs = {1,2,1,3,2};
        for (int i = 0; i < 5; i++) {
            FiniteStateMachine.FSMBacklogPair<String, Integer> pair = FSM.backtrack();
            assertEquals(expectedStates[i],pair.state());
            assertEquals(inputs[i],pair.input());
        }
    }

    @Test
    void rollbackTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.Rollback<String,Integer> rollback = (prevState, input, currState) -> {
            tester.set(input);
        };
        FiniteStateMachine<String, Integer> FSM = FiniteStateMachine.Builder.<String, Integer>tableStateMachine()
                .addTransition("state1",2,"state2",null,rollback)
                .addTransition("state2",1,"state1",null,rollback)
                .addTransition("state2",3,"state3",null,rollback)
                .addTransition("state3",1,"state1",null,rollback)
                .initialState("state1")
                .build();
        FSM.accept(2);
        FSM.accept(3);
        FSM.accept(1);
        FSM.accept(2);
        FSM.accept(1);
        int[] inputs = {1,2,1,3,2};
        for (int i = 0; i < 5; i++) {
            FSM.backtrack();
            assertEquals(inputs[i],tester.get());
        }
    }

    @Test
    void errorTest() {
        AtomicInteger tester = new AtomicInteger(0);
        FiniteStateMachine.ErrorCallback<String,Integer> errorCallback = (state, input) -> tester.set(input);
        FiniteStateMachine<String, Integer> FSM = FiniteStateMachine.Builder.<String, Integer>tableStateMachine()
                .addTransition("state1",2,"state2",null,null)
                .addTransition("state2",1,"state1",null,null)
                .addTransition("state2",3,"state3",null,null)
                .addTransition("state3",1,"state1",null,null)
                .initialState("state1").error(errorCallback)
                .build();
        FSM.accept(2);
        FSM.accept(3);
        FSM.accept(2);
        assertEquals(2,tester.get());
        FSM.accept(1);
        FSM.accept(3);
        assertEquals(3,tester.get());
        FSM.accept(2);
        FSM.accept(1);
    }
}

/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.splash.pre;

import com.cleanroommc.kirino.schemata.fsm.FiniteStateMachine;

import com.google.common.base.Preconditions;

final class PreSplashPhaseFSM {

    private final FiniteStateMachine<PreSplashPhase, Integer> fsm;

    private boolean finished = false;

    PreSplashPhaseFSM() {
        fsm = FiniteStateMachine.BuilderImpl
            .enumIntStateMachine(PreSplashPhase.class, 0, 0)
            .initialState(PreSplashPhase.FML_SETUP)
            .addTransition(PreSplashPhase.FML_SETUP, 0, PreSplashPhase.INITIAL_MOD_SCAN)
            .addTransition(PreSplashPhase.INITIAL_MOD_SCAN, 0, PreSplashPhase.CORE_MOD_LAUNCH)
            .addTransition(PreSplashPhase.CORE_MOD_LAUNCH, 0, PreSplashPhase.MINECRAFT_INIT)
            .addTransition(PreSplashPhase.MINECRAFT_INIT, 0, PreSplashPhase.RIGHT_BEFORE_SPLASH)
            .error((state, input) -> {
                throw new RuntimeException(String.format(
                        "An error occurred inside PreSplashPhaseFSM. The input=%d leads to a non-existent route. Current state=%s.", input, state));
            })
            .validate()
            .build();
    }

    public PreSplashPhase getState() {
        return fsm.state();
    }

    public void next() {
        Preconditions.checkState(!finished);

        if (fsm.state() == PreSplashPhase.RIGHT_BEFORE_SPLASH) {
            finished = true;
        } else {
            fsm.accept(0);
        }
    }
}

/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanmix.service;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class CleanMixBootstrap implements IMixinServiceBootstrap {

    private static final String OWN_SERVICE = "com.cleanroommc.cleanmix.service.CleanMixService";

    @Override
    public String getName() {
        return "CleanMix";
    }

    @Override
    public String getServiceClassName() {
        return OWN_SERVICE;
    }

    @Override
    public void bootstrap() { }

}

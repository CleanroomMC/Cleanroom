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

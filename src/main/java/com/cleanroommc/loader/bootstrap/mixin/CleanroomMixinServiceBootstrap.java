package com.cleanroommc.loader.bootstrap.mixin;

import org.spongepowered.asm.service.IMixinServiceBootstrap;

public class CleanroomMixinServiceBootstrap implements IMixinServiceBootstrap {

    @Override
    public String getName() {
        return "Cleanroom";
    }

    @Override
    public String getServiceClassName() {
        return this.getClass().getPackageName() + ".CleanroomMixinService";
    }

    @Override
    public void bootstrap() { }

}


package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public final class MixinContainer extends DummyModContainer{
    public MixinContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "mixinbooter";
        meta.name = "MixinBooter";
        meta.description = "A Mixin library and loader.";
        meta.version = ForgeEarlyConfig.CUSTOM_BUILT_IN_MOD_VERSION ? ForgeEarlyConfig.MIXIN_BOOTER_VERSION : "10.7";
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}

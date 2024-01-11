package net.minecraftforge.fml.common;

import com.google.common.eventbus.EventBus;

public final class MixinContainer extends DummyModContainer{
    public MixinContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "mixinbooter";
        meta.name = "MixinBooter";
        meta.description = "A Mixin library and loader.";
        meta.version = "100.0";
        meta.logoFile = "/icon.png";
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        //do nothing to avoid unnecessary cost. Return true will turn available.
        return true;
    }
}

package net.minecraftforge.fml.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.asm.FMLSanityChecker;

import java.io.File;

public final class MixinContainer extends DummyModContainer{
    public MixinContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "mixinbooter";
        meta.name = "MixinBooter";
        meta.description = "A Mixin library and loader.";
        meta.version = "10.0";
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

    @Override
    public File getSource()
    {
        return FMLSanityChecker.fmlLocation;
    }
}

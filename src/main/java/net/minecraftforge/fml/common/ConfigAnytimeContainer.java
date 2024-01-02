package net.minecraftforge.fml.common;

import com.google.common.eventbus.EventBus;

public final class ConfigAnytimeContainer extends DummyModContainer {

    public ConfigAnytimeContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "configanytime";
        meta.name = "ConfigAnytime";
        meta.description = "Allows Forge configurations to be setup at any point in time.";
        meta.version = "100.0";
        meta.logoFile = "/icon.png";
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }

}

package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.util.Arrays;

public class ConfigAnytimeContainer extends DummyModContainer {
    public ConfigAnytimeContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "configanytime";
        meta.name = "ConfigAnytime";
        meta.description = "Allows Forge configurations to be setup at any point in time.";
        meta.version = "3.0";
        meta.authorList.add("Rongmario");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}

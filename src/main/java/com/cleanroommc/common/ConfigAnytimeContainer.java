package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class ConfigAnytimeContainer extends DummyModContainer {
    public ConfigAnytimeContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "configanytime";
        meta.name = "ConfigAnytime";
        meta.description = "Allows Forge configurations to be setup at any point in time.";
        meta.version = ForgeEarlyConfig.CUSTOM_BUILT_IN_MOD_VERSION ? ForgeEarlyConfig.CONFIG_ANY_TIME_VERSION : "3.0";
        meta.authorList.add("Rongmario");
        meta.logoFile = "/configanytime_icon.png";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }
}

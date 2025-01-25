package com.cleanroommc.common;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

public class ControllingContainer extends DummyModContainer {
    public ControllingContainer() {
        super(new ModMetadata());
        ModMetadata meta = this.getMetadata();
        meta.modId = "controlling";
        meta.name = "Controlling";
        meta.description = "Gives people more Control of the key binding screen.";
        meta.version = "3.0.12";
        meta.authorList.add("Jaredlll08");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        bus.register(this);
        return true;
    }
}

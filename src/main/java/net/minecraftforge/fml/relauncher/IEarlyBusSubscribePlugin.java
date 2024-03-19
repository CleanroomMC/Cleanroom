package net.minecraftforge.fml.relauncher;


import com.google.common.eventbus.EventBus;

/**
 * A service for common mod to subscribe {@link com.cleanroommc.event.EarlyBus#BUS}
 * **/
public interface IEarlyBusSubscribePlugin {
    void addToBus(EventBus bus);
}

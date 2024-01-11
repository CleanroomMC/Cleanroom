package com.cleanroommc.event;

import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.discovery.ModDiscoverer;

import java.util.List;
import java.util.function.Supplier;
/**
 * FMLDiscoverModEvent is fired when fml have discovered mods but have not loaded them.
 */

public record FMLDiscoverModEvent(Supplier<ModDiscoverer> modDiscoverer, Supplier<List<ModContainer>> mods){
}

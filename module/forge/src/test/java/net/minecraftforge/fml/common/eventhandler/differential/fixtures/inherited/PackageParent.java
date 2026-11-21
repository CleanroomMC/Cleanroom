package net.minecraftforge.fml.common.eventhandler.differential.fixtures.inherited;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.differential.fixtures.LegacySubscriberScenarios.CallbackEvent;

public class PackageParent {
    @SubscribeEvent(priority = EventPriority.HIGH)
    void on(CallbackEvent event) {
        event.record("unexpected cross-package parent");
    }
}

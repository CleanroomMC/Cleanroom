package net.minecraftforge.debug.client;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.MouseTurnEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tests {@link MouseTurnEvent} and the frame clock.
 *
 * <p>With {@link #ENABLED}, yaw input is halved and {@link ForgeHooksClient#getFrameDeltaSeconds()} is logged once per second.
 *
 * <p>With {@link #CANCEL} as well, every turn is canceled, so moving the mouse must not change the view.
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = MouseTurnEventTest.MODID)
@Mod(modid = MouseTurnEventTest.MODID, name = "Mouse Turn Event Test", version = "1.0", clientSideOnly = true)
public class MouseTurnEventTest
{
    public static final String MODID = "mouseturneventtest";

    static final boolean ENABLED = false;
    static final boolean CANCEL = false;

    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static float secondAccumulator = 0.0F;

    @SubscribeEvent
    public static void onMouseTurn(MouseTurnEvent event)
    {
        if (!ENABLED) return;

        secondAccumulator += event.getFrameDeltaSeconds();
        if (secondAccumulator >= 1.0F)
        {
            secondAccumulator = 0.0F;
            LOGGER.info("Frame delta: event {} s, clock {} s", event.getFrameDeltaSeconds(), ForgeHooksClient.getFrameDeltaSeconds());
        }

        if (CANCEL)
        {
            event.setCanceled(true);
            return;
        }

        event.setYaw(event.getYaw() * 0.5F);
    }
}

/*
 * Minecraft Forge
 * Copyright (c) 2016-2020.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.minecraftforge.debug.client;

import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tests {@link InputEvent.MouseTurnEvent} and the frame clock.<br>
 * With {@link #ENABLED}, yaw input is halved and {@link ForgeHooksClient#getFrameDeltaSeconds()} is logged once per second.<br>
 * With {@link #CANCEL} as well, every turn is canceled, so moving the mouse must not change the view.
 */
@Mod.EventBusSubscriber(value = Side.CLIENT, modid = MouseTurnEventTest.MODID)
@Mod(modid = MouseTurnEventTest.MODID, name = "Mouse Turn Event Test", version = "1.0", clientSideOnly = true)
public class MouseTurnEventTest
{
    public static final String MODID = "mouseturneventtest";

    static final boolean ENABLED = false;
    static final boolean CANCEL = false;

    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static double secondAccumulator = 0.0D;

    @SubscribeEvent
    public static void onMouseTurn(InputEvent.MouseTurnEvent event)
    {
        if (!ENABLED) return;

        secondAccumulator += event.getFrameDeltaSeconds();
        if (secondAccumulator >= 1.0D)
        {
            secondAccumulator = 0.0D;
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

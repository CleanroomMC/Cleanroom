/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.test.debug.client;

import com.cleanroommc.client.input.MouseTurnEvent;
import com.cleanroommc.client.input.MouseTurnHooks;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Tests {@link MouseTurnEvent} and the frame clock.
 *
 * <p>With {@link #ENABLED}, yaw input is halved and {@link MouseTurnHooks#getFrameDeltaSeconds()} is logged once per second.
 *
 * <p>With {@link #CANCEL} as well, every turn is canceled, so moving the mouse must not change the view.
 */
@Mod(modid = MouseTurnEventTest.MODID, name = "Mouse Turn Event Test", version = "1.0", clientSideOnly = true)
public class MouseTurnEventTest {

    public static final String MODID = "mouseturneventtest";

    static final boolean ENABLED = false;
    static final boolean CANCEL = false;

    private static final Logger LOGGER = LogManager.getLogger(MODID);
    private static float secondAccumulator = 0.0F;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (ENABLED) {
            MinecraftForge.EVENT_BUS.register(MouseTurnEventTest.class);
        }
    }

    @SubscribeEvent
    public static void onMouseTurn(MouseTurnEvent event) {
        secondAccumulator += event.getFrameDeltaSeconds();
        if (secondAccumulator >= 1.0F) {
            secondAccumulator = 0.0F;
            LOGGER.info("Frame delta: event {} s, clock {} s", event.getFrameDeltaSeconds(), MouseTurnHooks.getFrameDeltaSeconds());
        }

        if (CANCEL) {
            event.setCanceled(true);
            return;
        }

        event.setYaw(event.getYaw() * 0.5F);
    }

}

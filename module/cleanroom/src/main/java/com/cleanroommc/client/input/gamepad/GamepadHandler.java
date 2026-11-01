/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.client.input.gamepad;

import com.cleanroommc.client.sdl.SDL;
import com.cleanroommc.client.sdl.events.GamepadEvent;
import com.cleanroommc.client.sdl.input.Gamepad;
import com.cleanroommc.client.sdl.input.GamepadAxis;
import com.cleanroommc.client.sdl.input.GamepadButton;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.MouseHelper;
import net.minecraft.util.MovementInputFromOptions;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class GamepadHandler {

    private static final Logger LOGGER = LogManager.getLogger("Gamepad handler");

    private static boolean isInit;

    private static Gamepad gamepad;

    private static Object2BooleanArrayMap<GamepadButton> pressedButton;
    private static float leftAxisX;
    private static float leftAxisY;
    private static float rightAxisX;
    private static float rightAxisY;
    private static float leftTrigger;
    private static float rightTrigger;

    private GamepadHandler() { }

    public static void init() {
        if (isInit) {
            return;
        }

        gamepad = SDL.gamepads().first();

        if (gamepad != null) {
            LOGGER.info("Controller {} connected.", gamepad.name());
        }

        SDL.events().register(new GamepadListener());

        pressedButton = new Object2BooleanArrayMap<>(GamepadButton.values().length);
        for (GamepadButton button : GamepadButton.values()) {
            pressedButton.put(button, false);
        }

        GamepadMapping.resetMapping();

        isInit = true;
    }

    public static void runTickGamepad() {
        if (gamepad != null) {
            // event postGamepad
            // if (com.cleanroommc.client.CleanroomHooks.postGamepadEvent()) continue;

            poll();
            GuiScreen screen = Minecraft.getMinecraft().currentScreen;
            if (screen != null) {
                screen.handleGamepadInput();
            }

            //            net.minecraftforge.fml.common.FMLCommonHandler.instance().fireMouseInput();
        }

        processGamepadInput();
    }

    public static void updatePlayerMoveState(MovementInputFromOptions input) {
        if (gamepad != null) {
            if (input.moveForward == 0 && input.moveStrafe == 0) {
                input.moveForward = -GamepadHandler.getLeftAxisY();
                input.forwardKeyDown = input.moveForward > 0;
                input.backKeyDown = input.moveForward < 0;
                input.moveStrafe = -GamepadHandler.getLeftAxisX();
                input.leftKeyDown = input.moveStrafe > 0;
                input.rightKeyDown = input.moveStrafe < 0;
            }
        }
    }

    public static boolean hookMouseXYChange(MouseHelper mouseHelper) {
        if (gamepad == null || mouseHelper.deltaX != 0 || mouseHelper.deltaY != 0) {
            return false;
        }

        mouseHelper.deltaX = (int) (12 * GamepadHandler.getRightAxisX());
        mouseHelper.deltaY = (int) (-8 * GamepadHandler.getRightAxisY());
        return true;
    }

    public static float getLeftAxisX() {
        return leftAxisX;
    }

    public static float getLeftAxisY() {
        return leftAxisY;
    }

    public static float getRightAxisX() {
        return rightAxisX;
    }

    public static float getRightAxisY() {
        return rightAxisY;
    }

    public static float getLeftTrigger() {
        return leftTrigger;
    }

    public static float getRightTrigger() {
        return rightTrigger;
    }

    public static void reset() {
        pressedButton.replaceAll((_, _) -> false);
        leftAxisX = 0;
        leftAxisY = 0;
        rightAxisX = 0;
        rightAxisY = 0;
        leftTrigger = 0;
        rightTrigger = 0;
    }

    private static void poll() {
        pressedButton.replaceAll((k, _) -> gamepad.button(k));

        leftAxisX = gamepad.axis(GamepadAxis.LEFT_X);
        leftAxisY = gamepad.axis(GamepadAxis.LEFT_Y);

        rightAxisX = gamepad.axis(GamepadAxis.RIGHT_X);
        rightAxisY = gamepad.axis(GamepadAxis.RIGHT_Y);

        leftTrigger = gamepad.axis(GamepadAxis.LEFT_TRIGGER);
        rightTrigger = gamepad.axis(GamepadAxis.RIGHT_TRIGGER);
    }

    private static void processGamepadInput() {
        for (Object2BooleanMap.Entry<GamepadButton> button : pressedButton.object2BooleanEntrySet()) {
            if (!GamepadMapping.setMappingState(button.getKey(), button.getBooleanValue())) {
                switch (button.getKey()) {
                    case GamepadButton.LEFT_SHOULDER, GamepadButton.RIGHT_SHOULDER -> {
                        if (button.getBooleanValue()) {
                            Minecraft.getMinecraft()
                                .player
                                .inventory
                                .changeCurrentItem(button.getKey() == GamepadButton.RIGHT_SHOULDER ? 1 : -1);
                        }
                    }
                    case GamepadButton.BACK -> {
                        if (button.getBooleanValue() && Minecraft.getMinecraft().currentScreen == null) {
                            Minecraft.getMinecraft().displayInGameMenu();
                        }
                    }
                    default -> {
                        if (button.getBooleanValue()) {
                            LOGGER.info("Button pressed: {}", button.getKey().name());
                        }
                    }
                }
            }
        }

        //        GamepadMapping.setMappingState(button.getKey(), button.getBooleanValue())
    }

    private static final class GamepadListener {

        @SubscribeEvent
        public void onGamepadAdded(GamepadEvent.Added e) {
            if (gamepad == null) {
                GamepadHandler.reset();
                gamepad = e.gamepad();
                LOGGER.info("Controller connected: {}", gamepad.name());
            }
        }

        @SubscribeEvent
        public void onGamepadRemove(GamepadEvent.Removed e) {
            if (gamepad != null && gamepad.id() == e.instanceId()) {
                GamepadHandler.reset();
                LOGGER.info("Controller disconnected. Try find another one.");
                gamepad = SDL.gamepads().first();
                if (gamepad != null) {
                    LOGGER.info("New controller detected: {}", gamepad.name());
                } else {
                    LOGGER.info("No controllers detected.");
                }
            }
        }

        @SubscribeEvent
        public void onGamepadRemapped(GamepadEvent.Remapped e) { }

        @SubscribeEvent
        public void onGamepadTouchpadUpdate(GamepadEvent.Touchpad e) { }

        @SubscribeEvent
        public void onGamepadSensorUpdate(GamepadEvent.Sensor e) { }

    }

}

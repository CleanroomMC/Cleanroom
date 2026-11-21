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

import com.cleanroommc.client.sdl.input.GamepadButton;

import it.unimi.dsi.fastutil.objects.Object2BooleanArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jspecify.annotations.NonNull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.Map;

public final class GamepadMapping implements Comparable<GamepadMapping> {

    private static final Object2BooleanArrayMap<GamepadButton> pressedState = new Object2BooleanArrayMap<>();

    private static final Map<KeyBinding, GamepadButton> mappingList = new Object2ObjectArrayMap<>();
    private static final Map<GamepadButton, KeyBinding> reverseMappingList = new Object2ObjectArrayMap<>();

    private GamepadMapping() { }

    public static void updateMapping(KeyBinding forwardKey, GamepadButton newButton) {
        pressedState.put(newButton, false);
        reverseMappingList.remove(mappingList.get(forwardKey));
        mappingList.put(forwardKey, newButton);
        if (newButton != null) {
            reverseMappingList.put(newButton, forwardKey);
        }
    }

    public static boolean setMappingState(GamepadButton button, boolean pressed) {
        if (button != null && reverseMappingList.containsKey(button) && (pressed != pressedState.getBoolean(button))) {
            pressedState.put(button, pressed);
            KeyBinding.setKeyBindState(reverseMappingList.get(button).getKeyCode(), pressed);
            if (pressed) {
                KeyBinding.onTick(reverseMappingList.get(button).getKeyCode());
            }
            return true;
        }
        return false;
    }

    public static void resetMapping() {
        //        updateMapping(Minecraft.getMinecraft().gameSettings.keyBindAttack, GamepadButton.);
        updateMapping(Minecraft.getMinecraft().gameSettings.keyBindJump, GamepadButton.SOUTH);
        updateMapping(Minecraft.getMinecraft().gameSettings.keyBindSneak, GamepadButton.RIGHT_STICK);
        updateMapping(Minecraft.getMinecraft().gameSettings.keyBindDrop, GamepadButton.EAST);
        updateMapping(Minecraft.getMinecraft().gameSettings.keyBindInventory, GamepadButton.WEST);
        //        updateMapping();
        //        updateMapping();
        //        updateMapping();
        //        updateMapping();
    }

    @Override
    public int compareTo(@NonNull GamepadMapping o) {
        return 0;
    }

}

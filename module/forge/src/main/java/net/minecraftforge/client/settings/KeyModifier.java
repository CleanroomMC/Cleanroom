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

package net.minecraftforge.client.settings;

import javax.annotation.Nullable;

import com.cleanroommc.lwjgly.rt.Scancodes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.input.Keyboard;

public enum KeyModifier {
    CONTROL {
        @Override
        public boolean matches(int keyCode)
        {
            if (Minecraft.IS_RUNNING_ON_MAC)
            {
                return keyCode == Keyboard.KEY_LMETA || keyCode == Keyboard.KEY_RMETA;
            }
            else
            {
                return keyCode == Keyboard.KEY_LCONTROL || keyCode == Keyboard.KEY_RCONTROL;
            }
        }

        @Override
        public boolean isActive()
        {
            return GuiScreen.isCtrlKeyDown();
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            return GuiScreen.isCtrlKeyDown();
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            String keyName = GameSettings.getKeyDisplayString(keyCode);
            String localizationFormatKey = Minecraft.IS_RUNNING_ON_MAC ? "forge.controlsgui.control.mac" : "forge.controlsgui.control";
            return I18n.format(localizationFormatKey, keyName);
        }
    },
    SHIFT {
        @Override
        public boolean matches(int keyCode)
        {
            return keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT;
        }

        @Override
        public boolean isActive()
        {
            return GuiScreen.isShiftKeyDown();
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            return GuiScreen.isShiftKeyDown();
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            String keyName = GameSettings.getKeyDisplayString(keyCode);
            return I18n.format("forge.controlsgui.shift", keyName);
        }
    },
    ALT {
        @Override
        public boolean matches(int keyCode)
        {
            return keyCode == Keyboard.KEY_LMENU || keyCode == Keyboard.KEY_RMENU;
        }

        @Override
        public boolean isActive()
        {
            return GuiScreen.isAltKeyDown();
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            return GuiScreen.isAltKeyDown();
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            String keyName = GameSettings.getKeyDisplayString(keyCode);
            return I18n.format("forge.controlsgui.alt", keyName);
        }
    },
    NONE {
        @Override
        public boolean matches(int keyCode)
        {
            return false;
        }

        @Override
        public boolean isActive()
        {
            return true;
        }

        @Override
        public boolean isActive(@Nullable IKeyConflictContext conflictContext)
        {
            if (conflictContext != null && !conflictContext.conflicts(KeyConflictContext.IN_GAME))
            {
                for (KeyModifier keyModifier : MODIFIER_VALUES)
                {
                    if (keyModifier.isActive(conflictContext))
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public String getLocalizedComboName(int keyCode)
        {
            return GameSettings.getKeyDisplayString(keyCode);
        }
    };

    public static final int MASK_NONE = 0;
    public static final int MASK_CONTROL = 1;
    public static final int MASK_ALT = 2;
    public static final int MASK_SHIFT = 4;

    public static final KeyModifier[] MODIFIER_VALUES = {CONTROL, ALT, SHIFT};

    @Deprecated
    public static KeyModifier getActiveModifier()
    {
        for (KeyModifier keyModifier : MODIFIER_VALUES)
        {
            if (keyModifier.isActive(null))
            {
                return keyModifier;
            }
        }
        return NONE;
    }

    public static int getActiveModifierMask()
    {
        int mask = 0;
        for (KeyModifier keyModifier : MODIFIER_VALUES)
        {
            if (keyModifier.isActive(null))
            {
                mask |= keyModifier.mask();
            }
        }
        return mask;
    }

    public static boolean isKeyCodeModifier(int keyCode)
    {
        for (KeyModifier keyModifier : MODIFIER_VALUES)
        {
            if (keyModifier.matches(keyCode))
            {
                return true;
            }
        }
        return false;
    }

    public static KeyModifier getKeyModifier(int keyCode)
    {
        for (KeyModifier keyModifier : MODIFIER_VALUES)
        {
            if (keyModifier.matches(keyCode))
            {
                return keyModifier;
            }
        }
        return NONE;
    }

    public static KeyModifier getKeyModifierSDL(int sdlKeyCode)
    {
        return getKeyModifier(Scancodes.toLwjgl(sdlKeyCode));
    }

    public static int toMask(KeyModifier modifier)
    {
        return switch (modifier) {
            case CONTROL    -> MASK_CONTROL;
            case ALT        -> MASK_ALT;
            case SHIFT      -> MASK_SHIFT;
            default         -> MASK_NONE;
        };
    }

    public static int filterMask(int mask, int keyCode) {
        return mask == MASK_NONE ? MASK_NONE : mask & ~toMask(getKeyModifier(keyCode));
    }

    public static boolean isActive(int mask, @Nullable IKeyConflictContext conflictContext) {
        if (mask == MASK_NONE) return NONE.isActive(conflictContext);

        boolean isActive = true;
        for (KeyModifier modifier: MODIFIER_VALUES) {
            if (modifier.has(mask)) {
                isActive = modifier.isActive(conflictContext);
            }
        }
        return isActive;
    }

    public static int maskFromString(String modifiers)
    {
        if (modifiers == null)
        {
            return MASK_NONE;
        }
        int mask = MASK_NONE;
        for (String part : modifiers.split("\\+"))
        {
            mask |= valueFromString(part.trim()).mask();
        }
        return mask;
    }

    public static String maskToString(int mask)
    {
        if (mask == 0) return "";
        StringBuilder sb = new StringBuilder();
        for (KeyModifier modifier: MODIFIER_VALUES) {
            if (modifier.has(mask)) {
                if (!sb.isEmpty()) {
                    sb.append('+');
                }
                sb.append(modifier);
            }
        }
        return ":" + sb;
    }

    public static String getLocalizedComboName(int mask, int keyCode)
    {
        String name = GameSettings.getKeyDisplayString(keyCode);
        if ((mask & MASK_SHIFT) != 0)   name = I18n.format("forge.controlsgui.shift", name);
        if ((mask & MASK_ALT) != 0)     name = I18n.format("forge.controlsgui.alt", name);
        if ((mask & MASK_CONTROL) != 0) name = I18n.format(Minecraft.IS_RUNNING_ON_MAC ? "forge.controlsgui.control.mac" : "forge.controlsgui.control", name);
        return name;
    }

    public static KeyModifier valueFromString(String stringValue)
    {
        try
        {
            return valueOf(stringValue);
        }
        catch (NullPointerException | IllegalArgumentException ignored)
        {
            return NONE;
        }
    }

    public abstract boolean matches(int keyCode);

    /**
     * @deprecated use {@link #isActive(IKeyConflictContext)}
     */
    @Deprecated
    public abstract boolean isActive();

    public abstract boolean isActive(@Nullable IKeyConflictContext conflictContext);

    @Deprecated
    public abstract String getLocalizedComboName(int keyCode);

    public int mask()
    {
        return KeyModifier.toMask(this);
    }

    public boolean has(int mask)
    {
        return (mask & toMask(this)) != MASK_NONE;
    }
}

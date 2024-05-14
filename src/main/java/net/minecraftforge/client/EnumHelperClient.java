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

package net.minecraftforge.client;

import com.cleanroommc.hackery.enums.EnumHackery;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util.EnumOS;
import net.minecraft.world.GameType;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraftforge.common.util.EnumHelper;

import javax.annotation.Nullable;

/**
 * This class extends EnumHelper and provides methods to add custom entries to Minecraft's Enums.
 * It uses EnumHackery to add new entries without modifying Minecraft's source code.
 */
public class EnumHelperClient extends EnumHelper {

    /**
     * Array of Class arrays representing the types of parameters for each Enum type.
     */
    private static Class<?>[][] clientTypes = {
        {GameType.class, int.class, String.class, String.class},
        {Options.class, String.class, boolean.class, boolean.class},
        {EnumOS.class},
        {MusicTicker.MusicType.class, SoundEvent.class, int.class, int.class}
    };

    /**
     * Adds a new custom GameType to Minecraft's GameType enum.
     *
     * @param name The name of the new GameType.
     * @param id The ID of the new GameType.
     * @param displayName The register name of the new GameType, used at {@link net.minecraft.command.CommandGameMode}, displayed with a translatable key of `gameMode.${displayName}`
     * @param shortName The short name of the new GameType, used at {@link net.minecraft.command.CommandGameMode}.
     * @return The newly added GameType, or null if the addition failed.
     */
    @Nullable
    public static GameType addGameType(String name, int id, String displayName, String shortName) {
        return EnumHackery.addEnumEntry(GameType.class, name,
                new Class<?>[] { int.class, String.class, String.class },
                new Object[] { id, displayName, shortName });
    }

    /**
     * Adds a new custom Options to Minecraft's Options enum.
     *
     * @param name The name of the new Options.
     * @param langName The language name of the new Options.
     * @param isSlider Whether the new Options should be a slider.
     * @param isToggle Whether the new Options should be a toggle.
     * @return The newly added Options, or null if the addition failed.
     */
    @Nullable
    public static Options addOptions(String name, String langName, boolean isSlider, boolean isToggle) {
        return EnumHackery.addEnumEntry(Options.class, name,
                new Class<?>[] { String.class, boolean.class, boolean.class },
                new Object[] { langName, isSlider, isToggle });
    }

    /**
     * Adds a new custom Options to Minecraft's Options enum with additional slider parameters.
     *
     * @param name The name of the new Options.
     * @param langName The language name of the new Options.
     * @param isSlider Whether the new Options should be a slider.
     * @param isToggle Whether the new Options should be a toggle.
     * @param valMin The minimum value of the slider.
     * @param valMax The maximum value of the slider.
     * @param valStep The step value of the slider.
     * @return The newly added Options, or null if the addition failed.
     */
    @Nullable
    public static Options addOptions(String name, String langName, boolean isSlider, boolean isToggle, float valMin, float valMax, float valStep) {
        return EnumHackery.addEnumEntry(Options.class, name,
                new Class<?>[] { String.class, boolean.class, boolean.class, float.class, float.class, float.class },
                new Object[] { langName, isSlider, isToggle, valMin, valMax, valStep });
    }

    /**
     * Adds a new custom OS to Minecraft's EnumOS enum.
     *
     * @param name The name of the new OS.
     * @return The newly added OS, or null if the addition failed.
     */
    @Nullable
    public static EnumOS addOS2(String name) {
        return EnumHackery.addEnumEntry(EnumOS.class, name);
    }

    /**
     * Adds a new custom MusicType to Minecraft's MusicTicker.MusicType enum.
     *
     * @param name The name of the new MusicType.
     * @param sound The SoundEvent associated with the new MusicType.
     * @param minDelay The minimum delay between music changes.
     * @param maxDelay The maximum delay between music changes.
     * @return The newly added MusicType, or null if the addition failed.
     */
    @Nullable
    public static MusicTicker.MusicType addMusicType(String name, SoundEvent sound, int minDelay, int maxDelay) {
        return EnumHackery.addEnumEntry(MusicTicker.MusicType.class, name,
                new Class<?>[] { SoundEvent.class, int.class, int.class },
                new Object[] { sound, minDelay, maxDelay });
    }

}
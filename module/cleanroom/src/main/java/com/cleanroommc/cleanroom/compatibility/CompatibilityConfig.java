/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.cleanroom.compatibility;

import net.minecraftforge.common.config.Config;

@Config(modid = "cleanroom", name = "cleanroom/compatibility")
@Config.RequiresMcRestart
public final class CompatibilityConfig {

    @Config.Comment("Allow Field.get/set calls to work again when they hit final/inaccessible fields.")
    public static boolean reflectFields = true;

    @Config.Comment(
        "Allow URLClassLoader casts followed by getURLs() to work on loaders that are no longer URLClassLoaders."
    ) public static boolean classLoaderUrls = true;

    @Config.Comment("new ScriptEngineManager() returns a manager that knows the bundled Nashorn.")
    public static boolean scriptEngine = true;

    @Config.Comment("UUID::fromString accepts malformed UUIDs that Java 8 accepted.")
    public static boolean malformedUuid = true;

    @Config.Comment("Redirect removed javax (Java EE) packages to their new jakarta replacements.")
    public static boolean jakarta = true;

    @Config.Comment("Reflection on LaunchClassLoader fields resolves against Foundation's classloader.")
    public static boolean launchClassLoaderFields = true;

    @Config.Comment("Redirect removed internal sun.reflect.Reflection class to a mocked class.")
    public static boolean sunReflection = true;

    @Config.Comment("Redirect oshi.SystemInfo to the new bundled FFM-based oshi.") public static boolean oshi = true;

    @Config.Comment("getCodeSource().getLocation().toURI() yields the jar file for classes loaded from a jar.")
    public static boolean codeSourceUri = true;

    @Config.Comment("HashMap.computeIfAbsent tolerates mapping functions that modify the map, as Java 8 did.")
    public static boolean computeIfAbsent = true;

    @Config.Comment("Redirect old Futures.addCallback overload that newer Guava removed.")
    public static boolean futuresAddCallback = true;

    @Config.Comment("(X[]) collection.toArray() becomes collection.toArray(new X[0]).")
    public static boolean toArrayCast = true;

    @Config.Comment("Compatibility patches for mods") public static final Mods mods = new Mods();

    public static final class Mods {

        @Config.Comment({ "Patch Better Sprinting", "https://www.curseforge.com/minecraft/mc-mods/better-sprinting" })
        public boolean betterSprinting = true;

        @Config.Comment({ "Patch Ears", "https://www.curseforge.com/minecraft/mc-mods/ears" })
        public boolean ears = true;

        @Config.Comment(
            { "Patch Enchantments Control", "https://www.curseforge.com/minecraft/mc-mods/enchantments-control" }
        ) public boolean enchantmentsControl = true;

        @Config.Comment({ "Patch Ender Core", "https://www.curseforge.com/minecraft/mc-mods/endercore" })
        public boolean enderCore = true;

        @Config.Comment({ "Patch Future MC", "https://www.curseforge.com/minecraft/mc-mods/future-mc" })
        public boolean futureMC = true;

        @Config.Comment({ "Patch More Sound Config", "https://www.curseforge.com/minecraft/mc-mods/more-sound-config" })
        public boolean moreSoundConfig = true;

        @Config.Comment({ "Patch Serene Seasons", "https://www.curseforge.com/minecraft/mc-mods/serene-seasons" })
        public boolean sereneSeasons = true;

        @Config.Comment({ "Patch Subaquatic", "https://modrinth.com/project/GjqoNelE" })
        public boolean subaquatic = true;

        @Config.Comment({ "Patch Better Records", "https://www.curseforge.com/minecraft/mc-mods/better-records" })
        public boolean betterRecords = true;

        @Config.Comment({ "Patch CodeChickenLib", "https://www.curseforge.com/minecraft/mc-mods/codechicken-lib-1-8" })
        public boolean codeChickenLib = true;

        @Config.Comment({ "Patch Colytra", "https://www.curseforge.com/minecraft/mc-mods/colytra" })
        public boolean colytra = true;

        @Config.Comment({ "Patch CreativeCore", "https://www.curseforge.com/minecraft/mc-mods/creativecore" })
        public boolean creativeCore = true;

        @Config.Comment({ "Patch Crossbow", "https://modrinth.com/mod/crossbow" }) public boolean crossbow = true;

        @Config.Comment({ "Patch Custom Main Menu", "https://www.curseforge.com/minecraft/mc-mods/custom-main-menu" })
        public boolean customMainMenu = true;

        @Config.Comment({ "Patch Howling Moon", "https://www.curseforge.com/minecraft/mc-mods/howling-moon" })
        public boolean howlingMoon = true;

        @Config.Comment({ "Patch Lockdown", "https://www.curseforge.com/minecraft/mc-mods/lockdown" })
        public boolean lockdown = true;

        @Config.Comment({ "Patch McJtyLib", "https://www.curseforge.com/minecraft/mc-mods/mcjtylib" })
        public boolean mcjtyLib = true;

        @Config.Comment({ "Patch Project Red - Core", "https://www.curseforge.com/minecraft/mc-mods/project-red-core" })
        public boolean projectRedCore = true;

        @Config.Comment({ "Patch Water Power", "https://www.curseforge.com/minecraft/mc-mods/water-power" })
        public boolean waterPower = true;

        @Config.Comment({ "Patch XaeroPlus", "https://www.curseforge.com/minecraft/mc-mods/xaeroplus" })
        public boolean xaeroPlus = true;

        private Mods() { }

    }

    private CompatibilityConfig() { }

}

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

    @Config.Comment("Allow Field.get/set calls to work again when they hit final/inaccessible fields."
    ) public static boolean reflectFields = true;

    @Config.Comment("Allow URLClassLoader casts followed by getURLs() to work on loaders that are no longer URLClassLoaders."
    ) public static boolean classLoaderUrls = true;

    @Config.Comment("new ScriptEngineManager() returns a manager that knows the bundled Nashorn."
    ) public static boolean scriptEngine = true;

    @Config.Comment("UUID::fromString accepts malformed UUIDs that Java 8 accepted."
    ) public static boolean malformedUuid = true;

    @Config.Comment("Redirect removed javax (Java EE) packages to their new jakarta replacements."
    ) public static boolean jakarta = true;

    @Config.Comment("Reflection on LaunchClassLoader fields resolves against Foundation's classloader."
    ) public static boolean launchClassLoaderFields = true;

    @Config.Comment("Redirect removed internal sun.reflect.Reflection class to a mocked class."
    ) public static boolean sunReflection = true;

    @Config.Comment("Redirect oshi.SystemInfo to the new bundled FFM-based oshi.") public static boolean oshi = true;

    @Config.Comment("getCodeSource().getLocation().toURI() yields the jar file for classes loaded from a jar."
    ) public static boolean codeSourceUri = true;

    @Config.Comment("HashMap.computeIfAbsent tolerates mapping functions that modify the map, as Java 8 did."
    ) public static boolean computeIfAbsent = true;

    @Config.Comment("Redirect old Futures.addCallback overload that newer Guava removed."
    ) public static boolean futuresAddCallback = true;

    @Config.Comment("(X[]) collection.toArray() becomes collection.toArray(new X[0])."
    ) public static boolean toArrayCast = true;

    private CompatibilityConfig() { }

}

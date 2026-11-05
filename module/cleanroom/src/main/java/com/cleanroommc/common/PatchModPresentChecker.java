/*
 * Copyright (c) 2026 CleanroomMC contributors
 *
 * This file is licensed under the CleanroomMC License Version 1.0.
 * See the applicable LICENSE file in this directory or a parent directory
 * for the full licence terms.
 *
 * This is visible-source software and is not open-source software.
 */

package com.cleanroommc.common;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.FMLLog;

public final class PatchModPresentChecker {

    private static final String FUGUE_COREMOD_CLASS = "com.cleanroommc.fugue.common.FugueLoadingPlugin";

    private static boolean fuguePresent;
    private static boolean checkPerformed;

    private PatchModPresentChecker() { }

    public static void performCheck() {
        if (ForgeEarlyConfig.DISABLE_PATCH_MOD_CHECK) {
            fuguePresent = true;
            return;
        }

        if (checkPerformed) {
            return;
        }
        checkPerformed = true;

        fuguePresent = Launch.classLoader.isClassExist(FUGUE_COREMOD_CLASS);
        if (!fuguePresent) {
            FMLLog.log.warn("Fugue is missing");
        }
    }

    public static boolean isNotPresent() {
        if (CleanroomEnvironment.isDev()) {
            return false;
        }
        if (ForgeEarlyConfig.DISABLE_PATCH_MOD_CHECK) {
            return false;
        }
        if (!checkPerformed) {
            performCheck();
        }
        return !fuguePresent;
    }

    public static String getWarningMessage() {
        if (!checkPerformed) {
            performCheck();
        }
        if (fuguePresent) {
            return "";
        }
        return "WARNING\nFugue is not installed\nYour pack may crash without it.\n" +
            "To disable this warning, check forge_early.cfg";
    }

}

package com.cleanroommc.common;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.FMLLog;

public class PatchModPresentChecker {

    private static final String FUGUE_COREMOD_CLASS = "com.cleanroommc.fugue.common.FugueLoadingPlugin";
    private static final String SCALAR_COREMOD_CLASS = "com.cleanroommc.scalar.ScalarLoadingPlugin";

    private static boolean fuguePresent;
    private static boolean scalarPresent;
    private static boolean checkPerformed;

    public static void performCheck() {
        if (checkPerformed) return;
        checkPerformed = true;

        if (ForgeEarlyConfig.DISABLE_PATCH_MOD_CHECK) {
            fuguePresent = true;
            scalarPresent = true;
            return;
        }

        fuguePresent = Launch.classLoader.isClassExist(FUGUE_COREMOD_CLASS);
        scalarPresent = Launch.classLoader.isClassExist(SCALAR_COREMOD_CLASS);

        if (fuguePresent && scalarPresent) {
            FMLLog.log.info("Patch mod check passed: Fugue and Scalar are present.");
        } else {
            FMLLog.log.warn("Patch mod check failed:");
            if (!fuguePresent) FMLLog.log.warn("  Fugue ({}) is missing", FUGUE_COREMOD_CLASS);
            if (!scalarPresent) FMLLog.log.warn("  Scalar ({}) is missing", SCALAR_COREMOD_CLASS);
        }
    }

    public static boolean isPatchModPresent() {
        if (!checkPerformed) performCheck();
        if (ForgeEarlyConfig.DISABLE_PATCH_MOD_CHECK) return true;
        return fuguePresent && scalarPresent;
    }

    public static boolean isFuguePresent() {
        if (!checkPerformed) performCheck();
        return fuguePresent;
    }

    public static boolean isScalarPresent() {
        if (!checkPerformed) performCheck();
        return scalarPresent;
    }

    public static String getWarningMessage() {
        if (!checkPerformed) performCheck();
        if (fuguePresent && scalarPresent) return "";
        String missing;
        if (!fuguePresent && !scalarPresent)
            missing = "Fugue and Scalar not installed";
        else if (!fuguePresent)
            missing = "Fugue not installed";
        else
            missing = "Scalar not installed";
        return "WARNING\n"
            + missing + "\n"
            + "Your modpack may crash without these mods.\n"
            + "To disable this warning, edit config/forge_early.cfg";
    }
}

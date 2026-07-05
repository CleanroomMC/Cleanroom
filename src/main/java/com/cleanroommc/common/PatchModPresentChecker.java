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
        if (ForgeEarlyConfig.DISABLE_PATCH_MOD_CHECK) {
            fuguePresent = true;
            scalarPresent = true;
            return;
        }

        if (checkPerformed) return;
        checkPerformed = true;

        fuguePresent = Launch.classLoader.isClassExist(FUGUE_COREMOD_CLASS);
        scalarPresent = Launch.classLoader.isClassExist(SCALAR_COREMOD_CLASS);

        if (!fuguePresent || !scalarPresent) {
            FMLLog.log.warn("These mods are missing:");
            if (!fuguePresent) FMLLog.log.warn("  Fugue is missing");
            if (!scalarPresent) FMLLog.log.warn("  Scalar is missing");
        }
    }

    public static boolean isNotPresent() {
        if (ForgeEarlyConfig.DISABLE_PATCH_MOD_CHECK) return false;
        if (!checkPerformed) performCheck();
        return !fuguePresent || !scalarPresent;
    }

    public static String getWarningMessage() {
        if (!checkPerformed) performCheck();
        if (fuguePresent && scalarPresent) return "";
        String missing;
        if (!fuguePresent && !scalarPresent)
            missing = "Fugue and Scalar are not installed";
        else if (!fuguePresent)
            missing = "Fugue is not installed";
        else
            missing = "Scalar is not installed";
        return "WARNING\n"
            + missing + "\n"
            + "Your pack may crash without these mods.\n"
            + "To disable this warning, check forge_early.cfg";
    }
}

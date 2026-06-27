package com.cleanroommc.common;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeEarlyConfig;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

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

    public static boolean hasUserMods() {
        return Loader.instance().getActiveModList().stream()
            .map(ModContainer::getModId)
            .anyMatch(id -> !id.equals("minecraft") && !id.equals("mcp")
                && !id.equals("fml") && !id.equals("cleanroom")
                && !id.equals("mixinbooter") && !id.equals("configanytime"));
    }

    public static String getMissingModsMessage() {
        StringBuilder sb = new StringBuilder();
        if (!fuguePresent) sb.append("  - Fugue (").append(FUGUE_COREMOD_CLASS).append(")\n");
        if (!scalarPresent) sb.append("  - Scalar (").append(SCALAR_COREMOD_CLASS).append(")\n");
        return sb.toString();
    }
}

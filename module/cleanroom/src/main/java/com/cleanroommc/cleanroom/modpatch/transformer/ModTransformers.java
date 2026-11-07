package com.cleanroommc.cleanroom.modpatch.transformer;

public final class ModTransformers {

    private ModTransformers() {}

    public static byte[] transform(String name, byte[] bytes) {
        if (name.startsWith("zmaster587.advancedRocketry")) return AdvancedRocketry.transform(name, bytes);
        if (name.startsWith("chylex.bettersprinting.")) return BetterSprinting.transform(name, bytes);
        if (name.startsWith("c4.colytra.")) return Colytra.transform(name, bytes);
        if (name.startsWith("de.maxhenkel.corpse.")) return Corpse.transform(name, bytes);
        if (name.startsWith("git.jbredwards.crossbow.")) return Crossbow.transform(name, bytes);
        if (name.startsWith("customskinloader.")) return CustomSkinLoader.transform(name, bytes);
        if (name.startsWith("com.codetaylor.mc.dropt.")) return Dropt.transform(name, bytes);
        if (name.startsWith("com.unascribed.ears.")) return Ears.transform(name, bytes);
        if (name.startsWith("austeretony.enchcontrol.")) return EnchantmentControl.transform(name, bytes);
        if (name.startsWith("com.enderio.core.")) return EnderCore.transform(name, bytes);
        if (name.startsWith("farseek.")) return Farseek.transform(name, bytes);
        if (name.startsWith("com.endertech.")) return ForgeEnderTech.transform(name, bytes);
        if (name.startsWith("thedarkcolour.futuremc.")) return FutureMC.transform(name, bytes);
        if (name.startsWith("kpan.heavy_fallings.")) return HeavyFallings.transform(name, bytes);
        if (name.startsWith("trinsdar.ic2c_extras.")) return IC2CExtras.transform(name, bytes);
        if (name.startsWith("journeymap.")) return JourneyMap.transform(name, bytes);
        if (name.startsWith("lumien.loadingprofiler.")) return LoadingProfiler.transform(name, bytes);
        if (name.startsWith("noppes.mpm.")) return MorePlayerModels.transform(name, bytes);
        if (name.startsWith("ichttt.mods.moresoundconfig.")) return MoreSoundConfig.transform(name, bytes);
        if (name.startsWith("nl.makertim.nbtperipheral.")) return NBTPeripheral.transform(name, bytes);
        if (name.startsWith("sereneseasons.")) return SereneSeasons.transform(name, bytes);
        if (name.startsWith("com.teamderpy.shouldersurfing.")) return ShoulderSurfing.transform(name, bytes);
        if (name.startsWith("pl.asie.splashanimation.")) return SplashAnimations.transform(name, bytes);
        if (name.startsWith("git.jbredwards.subaquatic.")) return Subaquatic.transform(name, bytes);
        if (name.startsWith("techguns.")) return Techguns.transform(name, bytes);
        if (name.startsWith("thecodex6824.thaumcraftfix.")) return ThaumcraftFix.transform(name, bytes);
        if (name.startsWith("org.valkyrienskies.mod.")) return ValkyrienSkies.transform(name, bytes);
        if (name.startsWith("de.teamlapen.vampirism")) return Vampirism.transform(name, bytes);
        if (name.startsWith("mcjty.xnet.")) return XNet.transform(name, bytes);
        if (name.startsWith("it.zerono.mods.zerocore.")) return ZeroCore.transform(name, bytes);
        return bytes;
    }
}

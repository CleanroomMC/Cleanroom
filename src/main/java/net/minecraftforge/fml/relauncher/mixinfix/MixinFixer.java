package net.minecraftforge.fml.relauncher.mixinfix;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.MixinBooterPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.mixin.transformer.ClassInfo;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.util.AbstractList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class MixinFixer {

    static Unsafe unsafe;
    static boolean registered = false;
    static Set<String> queuedLateMixinConfigs = new ObjectOpenHashSet<>();

    /**
     * For internal usage
     */
    public static Set<String> retrieveLateMixinConfigs() {
        Set<String> ret = queuedLateMixinConfigs;
        queuedLateMixinConfigs = null;
        return ret;
    }

    public static void patchAncientModMixinsLoadingMethod() {
        if (registered) {
            return;
        }
        registered = true;
        ClassInfo.registerCallback(ci -> {
            if (!ci.isMixin() && "net/minecraftforge/fml/common/Loader".equals(ci.getName())) {
                try {
                    // OpenJ9 Compatibility
                    Field unsafe$theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
                    unsafe$theUnsafe.setAccessible(true);
                    unsafe = (Unsafe) unsafe$theUnsafe.get(null);
                    Field classInfo$mixinsField = ClassInfo.class.getDeclaredField("mixins");
                    classInfo$mixinsField.setAccessible(true);
                    unsafe.putObject(ci, unsafe.objectFieldOffset(classInfo$mixinsField), new NotifiableMixinSet());
                } catch (ReflectiveOperationException e) {
                    throw new RuntimeException("Unable to patch for compatibility with older mixin mods", e);
                }
            }
        });
    }

    private static class NotifiableMixinSet extends HashSet<IMixinInfo> {

        private static Field mixinInfo$targetClassNames;
        static long mixinInfo$targetClassNames$offset = 0L;

        @Override
        public boolean add(IMixinInfo mixinInfo) {
            if (mixinInfo$targetClassNames == null) {
                try {
                    mixinInfo$targetClassNames = mixinInfo.getClass().getDeclaredField("targetClassNames");
                    mixinInfo$targetClassNames.setAccessible(true);
                    mixinInfo$targetClassNames$offset = unsafe.objectFieldOffset(mixinInfo$targetClassNames);
                } catch (NoSuchFieldException e) {
                    throw new RuntimeException("Unable to patch for compatibility with older mixin mods", e);
                }
            }
            switch (mixinInfo.getConfig().getName()) {
                // Integrated Proxy compatibility
                case "mixins.integrated_proxy.loader.json":
                    MixinFixer.queuedLateMixinConfigs.add("mixins.integrated_proxy.mod.json");
                    unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                    return true;
                // Just Enough IDs/Roughly Enough IDs compatibility
                case "mixins.jeid.init.json":
                    MixinFixer.queuedLateMixinConfigs.add("mixins.jeid.modsupport.json");
                    MixinFixer.queuedLateMixinConfigs.add("mixins.jeid.twilightforest.json");
                    unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                    return true;
                // DJ2 Addons compatibility
                case "mixins.dj2addons.bootstrap.json":
                    MixinFixer.queuedLateMixinConfigs.add("mixins.dj2addons.def.api.json");
                    MixinFixer.queuedLateMixinConfigs.add("mixins.dj2addons.def.custom.json");
                    MixinFixer.queuedLateMixinConfigs.add("mixins.dj2addons.def.optimizations.json");
                    MixinFixer.queuedLateMixinConfigs.add("mixins.dj2addons.def.patches.json");
                    MixinFixer.queuedLateMixinConfigs.add("mixins.dj2addons.def.tweaks.json");
                    unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                    correctingDj2Addons();
                    return true;
                case "mixins.dj2addons.init.json": // Backwards Compat
                    MixinFixer.queuedLateMixinConfigs.add("mixins.dj2addons.json");
                    unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                    correctingDj2Addons();
                    return true;
                case "mixins.thaumicfixes.init.json":
                    MixinFixer.queuedLateMixinConfigs.add("mixins.thaumicfixes.modsupport.json");
                    unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                    return true;
                // ErebusFix compatibility
                case "mixins.loader.json":
                    switch (mixinInfo.getConfig().getMixinPackage()) {
                        case "noobanidus.mods.erebusfix.mixins.":
                            MixinFixer.queuedLateMixinConfigs.add("mixins.erebusfix.json");
                            unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                            return true;
                        case "doomanidus.mods.uncraftingblacklist.mixins.":
                            MixinFixer.queuedLateMixinConfigs.add("mixins.uncraftingblacklist.json");
                            unsafe.putObject(mixinInfo, mixinInfo$targetClassNames$offset, new EmptyAbsorbingList());
                            return true;
                    }
            }
            return super.add(mixinInfo);
        }

        private void correctingDj2Addons() {
            // If you're bored and want to be enlightened, start here:
            // https://discord.com/channels/926486493562814515/926783373232447509/1168057816691507260
            try {
                // New (unreleased)
                Class.forName("btpos.dj2addons.common.CoreInfo", true, Launch.classLoader).getMethod("onLoadCore").invoke(null);
            } catch (ReflectiveOperationException e1) {
                try {
                    // CurseForge release (older)
                    Class.forName("org.btpos.dj2addons.core.DJ2AddonsCore", true, Launch.classLoader).getMethod("onLoadCore").invoke(null);
                } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                    // Skip when no classes are found, that cannot be the case unless an old dj2addons is installed
                } catch (ReflectiveOperationException e2) {
                    MixinBooterPlugin.LOGGER.fatal("DJ2Addons compatibility patch failed.", e2);
                }
            }
        }

    }

    private static class EmptyAbsorbingList extends AbstractList<String> {

        @Override
        public boolean addAll(Collection<? extends String> c) {
            return true;
        }

        @Override
        public String get(int index) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

    }

}
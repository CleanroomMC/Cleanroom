package net.minecraftforge.fml.relauncher;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLLog;

import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.IEarlyMixinLoader;
import zone.rong.mixinbooter.IMixinConfigHijacker;

import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;

@SuppressWarnings("deprecation")
@IFMLLoadingPlugin.Name("MixinBooter")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1)
public final class MixinBooterPlugin implements IFMLLoadingPlugin {

    static Set<IEarlyMixinLoader> earlyMixinLoaders = new HashSet<>();

    public MixinBooterPlugin() {
    }

    @Override
    public String[] getASMTransformerClass() {
        return null;
    }

    @Override
    public String getModContainerClass() {
        return "net.minecraftforge.fml.common.MixinContainer";
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        loadEarlyLoaders(earlyMixinLoaders);
        earlyMixinLoaders = null;
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    static void queneEarlyMixinLoader(IFMLLoadingPlugin plugin) {
        if (plugin instanceof IEarlyMixinLoader earlyMixinLoader) earlyMixinLoaders.add(earlyMixinLoader);
        if (plugin instanceof IMixinConfigHijacker hijacker) {
            Collection<String> disabledConfigs = GlobalProperties.get(GlobalProperties.Keys.CLEANROOM_DISABLE_MIXIN_CONFIGS);
            Context context = new Context(null);
            FMLLog.log.info("Loading config hijacker {}.", interceptor.getClass().getName());
            for (String hijacked : interceptor.getHijackedMixinConfigs(context)) {
                disabledConfigs.add(hijacked);
                FMLLog.log.info("{} will hijack the mixin config {}", interceptor.getClass().getName(), hijacked);
            }
        }
    }

    private void loadEarlyLoaders(Collection<IEarlyMixinLoader> queuedLoaders) {
        for (IEarlyMixinLoader queuedLoader : queuedLoaders) {
            FMLLog.log.info("Loading early loader {} for its mixins.", queuedLoader.getClass().getName());
            for (String mixinConfig : queuedLoader.getMixinConfigs()) {
                Context context = new Context(mixinConfig);
                if (queuedLoader.shouldMixinConfigQueue(context)) {
                    FMLLog.log.info("Adding {} mixin configuration.", mixinConfig);
                    Mixins.addConfiguration(mixinConfig);
                    queuedLoader.onMixinConfigQueued(context);
                }
            }
        }
    }



}

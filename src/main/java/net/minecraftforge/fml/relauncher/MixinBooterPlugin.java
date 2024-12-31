package net.minecraftforge.fml.relauncher;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLLog;
import org.spongepowered.asm.launch.GlobalProperties;
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

    static final Set<IEarlyMixinLoader> earlyMixinLoaders = new HashSet<>();
    static final Map<String, IMixinConfigHijacker> configHijackers = new HashMap<>();

    public MixinBooterPlugin() {
        GlobalProperties.put(GlobalProperties.Keys.CLEANROOM_DISABLE_MIXIN_CONFIGS, new HashSet<>());
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
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    static void loadEarlyMixinLoader(IFMLLoadingPlugin plugin) {
        if (plugin instanceof IEarlyMixinLoader earlyMixinLoader) earlyMixinLoaders.add(earlyMixinLoader);
        if (plugin instanceof IMixinConfigHijacker hijacker) {
            for (String hijacked : hijacker.getHijackedMixinConfigs()) {
                configHijackers.put(hijacked, hijacker);
            }
        }
    }

    private void loadEarlyLoaders(Collection<IEarlyMixinLoader> queuedLoaders) {
        for (IEarlyMixinLoader queuedLoader : queuedLoaders) {
            FMLLog.log.info("Loading early loader {} for its mixins.", queuedLoader.getClass().getName());
            for (String mixinConfig : queuedLoader.getMixinConfigs()) {
                Context context = new Context(mixinConfig);
                if (queuedLoader.shouldMixinConfigQueue(context)) {
                    IMixinConfigHijacker hijacker = getHijacker(mixinConfig);
                    if (hijacker != null) {
                        FMLLog.log.info("Mixin configuration {} intercepted by {}.", mixinConfig, hijacker.getClass().getName());
                    } else {
                        FMLLog.log.info("Adding {} mixin configuration.", mixinConfig);
                        Mixins.addConfiguration(mixinConfig);
                        queuedLoader.onMixinConfigQueued(context);
                    }
                }
            }
        }
    }

    public static IMixinConfigHijacker getHijacker(String configName) {
        return configHijackers.get(configName);
    }



}

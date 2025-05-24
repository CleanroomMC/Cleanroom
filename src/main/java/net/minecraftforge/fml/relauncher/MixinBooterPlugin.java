package net.minecraftforge.fml.relauncher;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLLog;

import com.google.gson.*;

import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.launch.GlobalProperties;
import zone.rong.mixinbooter.Context;
import zone.rong.mixinbooter.IEarlyMixinLoader;
import zone.rong.mixinbooter.IMixinConfigHijacker;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.Enumeration;
import java.io.InputStreamReader;
import java.net.URL;


@SuppressWarnings("deprecation")
@IFMLLoadingPlugin.Name("MixinBooter")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1)
public final class MixinBooterPlugin implements IFMLLoadingPlugin {

    static Set<IEarlyMixinLoader> earlyMixinLoaders = new HashSet<>();

    @Override
    public void injectData(Map<String, Object> data) {
        loadEarlyLoaders(earlyMixinLoaders);
        earlyMixinLoaders = null;
    }


    static void queneEarlyMixinLoader(IFMLLoadingPlugin plugin) {
        if (plugin instanceof IEarlyMixinLoader earlyMixinLoader) earlyMixinLoaders.add(earlyMixinLoader);
        if (plugin instanceof IMixinConfigHijacker hijacker) {
            Collection<String> disabledConfigs = GlobalProperties.get(GlobalProperties.Keys.CLEANROOM_DISABLE_MIXIN_CONFIGS);
            Context context = new Context(null, Collections.emptySet());
            FMLLog.log.info("Loading config hijacker {}.", hijacker.getClass().getName());
            for (String hijacked : hijacker.getHijackedMixinConfigs(context)) {
                disabledConfigs.add(hijacked);
                FMLLog.log.info("{} will hijack the mixin config {}", hijacker.getClass().getName(), hijacked);
            }
        }
    }

    private static void loadEarlyLoaders(Collection<IEarlyMixinLoader> queuedLoaders) {
        Set<String> modlist = speculatedModList();
        for (IEarlyMixinLoader queuedLoader : queuedLoaders) {
            FMLLog.log.info("Loading early loader {} for its mixins.", queuedLoader.getClass().getName());
            for (String mixinConfig : queuedLoader.getMixinConfigs()) {
                Context context = new Context(mixinConfig, modlist);
                if (queuedLoader.shouldMixinConfigQueue(context)) {
                    FMLLog.log.info("Adding {} mixin configuration.", mixinConfig);
                    Mixins.addConfiguration(mixinConfig);
                    queuedLoader.onMixinConfigQueued(context);
                }
            }
        }
    }

    public static Set<String> speculatedModList() {
        HashSet<String> presentMods = new HashSet<>();

        // buildIn mods :
        presentMods.add("minecraft");
        presentMods.add("fml");
        presentMods.add("forge");
        presentMods.add("mixinbooter");
        presentMods.add("configanytime");

        // mcmod.info
        try {
            Enumeration<URL> resources = Launch.classLoader.getResources("mcmod.info");
            while (resources.hasMoreElements()) {
                presentMods.addAll(parseMcmodInfo(resources.nextElement()));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to gather present mods from mcmod.info (s)", e);
        }

        // optifine :
        if (Launch.classLoader.isClassExist("optifine.OptiFineTweaker")) {
            presentMods.add("optifine");
        }

        return presentMods;

    }

    private static Set<String> parseMcmodInfo(URL url) {
        try {
            HashSet<String> ids = new HashSet<>();
            JsonElement root = JsonParser.parseReader(new InputStreamReader(url.openStream()));
            if (root.isJsonArray()) {
                for (JsonElement element : root.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        ids.add(element.getAsJsonObject().get("modid").getAsString());
                    }
                }
            } else {
                for (JsonElement element : root.getAsJsonObject().get("modList").getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        ids.add(element.getAsJsonObject().get("modid").getAsString());
                    }
                }
            }
            return ids;
        } catch (Throwable t) {
            FMLLog.log.error("Failed to parse mcmod.info for " + url, t);
        }
        return Collections.emptySet();
    }



}

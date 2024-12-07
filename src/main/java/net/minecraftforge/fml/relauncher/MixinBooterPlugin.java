package net.minecraftforge.fml.relauncher;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixins;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("MixinBooter")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1)
public final class MixinBooterPlugin implements IFMLLoadingPlugin {

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
        Object coremodList = data.get("coremodList");
        if (coremodList instanceof List) {
            Field fmlPluginWrapper$coreModInstance = null;
            for (Object coremod : (List) coremodList) {
                try {
                    if (fmlPluginWrapper$coreModInstance == null) {
                        fmlPluginWrapper$coreModInstance = coremod.getClass().getField("coreModInstance");
                        fmlPluginWrapper$coreModInstance.setAccessible(true);
                    }
                    Object theMod = fmlPluginWrapper$coreModInstance.get(coremod);
                    if (theMod instanceof IEarlyMixinLoader loader) {
                        FMLLog.log.info("Grabbing {} for its mixins.", loader.getClass());
                        for (String mixinConfig : loader.getMixinConfigs()) {
                            if (loader.shouldMixinConfigQueue(mixinConfig)) {
                                FMLLog.log.info("Adding {} mixin configuration.", mixinConfig);
                                Mixins.addConfiguration(mixinConfig);
                                loader.onMixinConfigQueued(mixinConfig);;
                            }
                        }
                    }
                } catch (Throwable t) {
                    FMLLog.log.error("Unexpected error handling early mixins", t);
                }
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }



}

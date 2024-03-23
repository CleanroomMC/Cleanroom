package net.minecraftforge.fml.relauncher;

import com.cleanroommc.bouncepad.Bouncepad;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("MixinBooter")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1)
public final class MixinBooterPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("MixinBooter");

    static {
        Launch.classLoader.addTransformerExclusion("scala.");
    }

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
        return "net.minecraftforge.fml.relauncher.MixinSetup";
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
                    Class<?> clazz = Bouncepad.classLoader.loadClass("zone.rong.mixinbooter.IEarlyMixinLoader");
                    Method get = clazz.getDeclaredMethod("getMixinConfigs");
                    Method should = clazz.getDeclaredMethod("shouldMixinConfigQueue", String.class);
                    Method on = clazz.getDeclaredMethod("onMixinConfigQueued", String.class);
                    if (clazz.isInstance(theMod)) {
                        Object loader = clazz.cast(theMod);
                        LOGGER.info("Grabbing {} for its mixins.", loader.getClass());
                        for (String mixinConfig : (List<String>)get.invoke(loader)) {
                            if ((Boolean) should.invoke(loader, mixinConfig)) {
                                LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                                Mixins.addConfiguration(mixinConfig);
                                on.invoke(loader, mixinConfig);
                            }
                        }
                    } else if ("org.spongepowered.mod.SpongeCoremod".equals(theMod.getClass().getName())) {
                        Launch.classLoader.registerTransformer("zone.rong.mixinbooter.fix.spongeforge.SpongeForgeFixer");
                    }
                } catch (Throwable t) {
                    LOGGER.error("Unexpected error", t);
                }
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }



}

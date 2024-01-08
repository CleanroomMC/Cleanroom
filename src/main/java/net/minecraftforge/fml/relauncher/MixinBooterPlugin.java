package net.minecraftforge.fml.relauncher;

import com.cleanroommc.bouncepad.Bouncepad;
import com.cleanroommc.event.EarlyBus;
import com.cleanroommc.event.MixinBootEvent;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
        return new String[0];
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
        Class<?> clazz;
        Method get;
        Method should;
        Method on;
        Field fmlPluginWrapper$coreModInstance;
        try{
            clazz = Bouncepad.classLoader.loadClass("zone.rong.mixinbooter.IEarlyMixinLoader");
            get = clazz.getDeclaredMethod("getMixinConfigs");
            should = clazz.getDeclaredMethod("shouldMixinConfigQueue", String.class);
            on = clazz.getDeclaredMethod("onMixinConfigQueued", String.class);
        } catch (ClassNotFoundException|NoSuchMethodException e) {
            throw new RuntimeException("can not launch mixinbooter",e);
        }
        List<Object> earlyMixinLoaders=new LinkedList<>();
        if (coremodList instanceof List) {
            for (CoreModManager.FMLPluginWrapper coremod : (List<CoreModManager.FMLPluginWrapper>) coremodList) {
                try {
                    if (clazz.isInstance(coremod.coreModInstance)) {
                        earlyMixinLoaders.add(clazz.cast(coremod.coreModInstance));
                    } else if ("org.spongepowered.mod.SpongeCoremod".equals(coremod.coreModInstance.getClass().getName())) {
                        Launch.classLoader.registerTransformer("zone.rong.mixinbooter.fix.spongeforge.SpongeForgeFixer");
                    }
                } catch (Throwable t) {
                    LOGGER.error("Unexpected error", t);
                }
            }
        }
        EarlyBus.BUS.post(new MixinBootEvent.Early(earlyMixinLoaders));
        for (Object loader:earlyMixinLoaders){
            try {
                LOGGER.info("Grabbing {} for its mixins.", loader.getClass());
                for (String mixinConfig : (List<String>) get.invoke(loader)) {
                    if ((Boolean) should.invoke(loader, mixinConfig)) {
                        LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                        Mixins.addConfiguration(mixinConfig);
                        on.invoke(loader, mixinConfig);
                    }
                }
            }catch (Throwable throwable){
                LOGGER.error("Unexpected error", throwable);
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }



}

package net.minecraftforge.fml.relauncher;

import com.cleanroommc.bouncepad.Bouncepad;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.mixinfix.MixinFixer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@IFMLLoadingPlugin.Name("MixinBooter")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 1)
public final class MixinBooterPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("MixinBooter");

    static {
        Launch.classLoader.addTransformerExclusion("scala.");
    }

    // (0.1.1-rc.2) Apply @WrapOperations in an IExtension to make sure they're the last injectors to run.
    // (0.1.1-rc.4) Add extensions to Extensions#activeExtensions in case they've already been selected
    @SuppressWarnings("unchecked")
    private static void registerExtension(IExtension extension) {
        IMixinTransformer transformer = (IMixinTransformer) MixinEnvironment.getDefaultEnvironment().getActiveTransformer();
        Extensions extensions = (Extensions) transformer.getExtensions();
        // Because the extensions have already been selected by MixinBooters, so we have to hack extension in.
        // If we haven't passed selection yet, it doesn't matter, because the list is re-created then.
        try {
            Field activeExtensionsField = Extensions.class.getDeclaredField("activeExtensions");
            activeExtensionsField.setAccessible(true);
            List<IExtension> activeExtensions = new ArrayList<>((List<IExtension>) activeExtensionsField.get(extensions));
            activeExtensions.add(extension);
            activeExtensionsField.set(extensions, Collections.unmodifiableList(activeExtensions));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            // Fail-fast so people report this and I can fix it
            throw new RuntimeException(
                String.format("Failed to inject extension %s. Please inform Rongmario!", extension),
                e
            );
        }
    }

    public MixinBooterPlugin() {
        addTransformationExclusions();
        initialize();
        LOGGER.info("Initializing Mixins...");
        MixinBootstrap.init();
        LOGGER.info("Initializing MixinExtras...");
        MixinExtrasBootstrap.init();
        MixinFixer.patchAncientModMixinsLoadingMethod();
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

    private void addTransformationExclusions() {
        Launch.classLoader.addTransformerExclusion("scala.");
        Launch.classLoader.addTransformerExclusion("com.llamalad7.mixinextras.");
    }

    private void initialize() {
        GlobalProperties.put(GlobalProperties.Keys.CLEANROOM_DISABLE_MIXIN_CONFIGS, new HashSet<>());
    }

}

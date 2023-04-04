package net.minecraftforge.fml.relauncher;

import com.google.common.eventbus.EventBus;
import com.llamalad7.mixinextras.injector.ModifyExpressionValueInjectionInfo;
import com.llamalad7.mixinextras.injector.ModifyReceiverInjectionInfo;
import com.llamalad7.mixinextras.injector.ModifyReturnValueInjectionInfo;
import com.llamalad7.mixinextras.injector.WrapWithConditionInjectionInfo;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperationApplicatorExtension;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperationInjectionInfo;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.injection.struct.InjectionInfo;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.mixin.transformer.ext.Extensions;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import zone.rong.mixinbooter.IEarlyMixinLoader;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.Name("MixinBooter")
@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(Integer.MIN_VALUE + 1)
public final class MixinBooterPlugin implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger("MixinBooter");

    static {
        Launch.classLoader.addTransformerExclusion("scala.");
    }

    // Initialize MixinExtras
    public static void initMixinExtra(boolean runtime) {
        InjectionInfo.register(ModifyExpressionValueInjectionInfo.class);
        InjectionInfo.register(ModifyReceiverInjectionInfo.class);
        InjectionInfo.register(ModifyReturnValueInjectionInfo.class);
        InjectionInfo.register(WrapWithConditionInjectionInfo.class);
        InjectionInfo.register(WrapOperationInjectionInfo.class);
        // Make sure it is not running in build-time
        if (runtime) {
            registerExtension(new WrapOperationApplicatorExtension());
        }
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
        LOGGER.info("MixinBootstrap Initializing...");
        MixinBootstrap.init();
        initMixinExtra(true);
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
            for (Object coremod : (List) coremodList) {
                try {
                    Field field = coremod.getClass().getField("coreModInstance");
                    field.setAccessible(true);
                    Object theMod = field.get(coremod);
                    if (theMod instanceof IEarlyMixinLoader) {
                        IEarlyMixinLoader loader = (IEarlyMixinLoader) theMod;
                        for (String mixinConfig : loader.getMixinConfigs()) {
                            if (loader.shouldMixinConfigQueue(mixinConfig)) {
                                LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                                Mixins.addConfiguration(mixinConfig);
                                loader.onMixinConfigQueued(mixinConfig);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error("Unexpected error", e);
                }
            }
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}

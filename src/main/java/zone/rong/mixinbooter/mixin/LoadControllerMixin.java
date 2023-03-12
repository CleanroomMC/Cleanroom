package zone.rong.mixinbooter.mixin;

import com.cleanroommc.bouncepad.Bouncepad;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.discovery.ASMDataTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.transformer.Proxy;
import zone.rong.mixinbooter.ILateMixinLoader;
import zone.rong.mixinbooter.MixinBooterPlugin;
import zone.rong.mixinbooter.MixinLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mixin(value = LoadController.class, remap = false)
public class LoadControllerMixin {

    @Shadow private Loader loader;

    @Inject(method = "distributeStateMessage(Lnet/minecraftforge/fml/common/LoaderState;[Ljava/lang/Object;)V", at = @At("HEAD"))
    private void beforeConstructing(LoaderState state, Object[] eventData, CallbackInfo ci) throws Throwable {
        if (state == LoaderState.CONSTRUCTING) { // This state is where Forge adds mod files to ModClassLoader

            ModClassLoader modClassLoader = (ModClassLoader) eventData[0];
            ASMDataTable asmDataTable = (ASMDataTable) eventData[1];

            MixinBooterPlugin.LOGGER.info("Instantiating all MixinLoader annotated classes...");

            for (ASMDataTable.ASMData asmData : asmDataTable.getAll(MixinLoader.class.getName())) {
                modClassLoader.addFile(asmData.getCandidate().getModContainer()); // Add to path before `newInstance`
                Class<?> clazz = Class.forName(asmData.getClassName());
                MixinBooterPlugin.LOGGER.info("Instantiating {} for its mixins.", clazz);
                clazz.newInstance();
            }

            MixinBooterPlugin.LOGGER.info("Instantiating all ILateMixinLoader implemented classes...");

            for (ASMDataTable.ASMData asmData : asmDataTable.getAll(ILateMixinLoader.class.getName().replace('.', '/'))) {
                modClassLoader.addFile(asmData.getCandidate().getModContainer()); // Add to path before `newInstance`
                Class<?> clazz = Class.forName(asmData.getClassName().replace('/', '.'));
                MixinBooterPlugin.LOGGER.info("Instantiating {} for its mixins.", clazz);
                ILateMixinLoader loader = (ILateMixinLoader) clazz.newInstance();
                for (String mixinConfig : loader.getMixinConfigs()) {
                    if (loader.shouldMixinConfigQueue(mixinConfig)) {
                        MixinBooterPlugin.LOGGER.info("Adding {} mixin configuration.", mixinConfig);
                        Mixins.addConfiguration(mixinConfig);
                        loader.onMixinConfigQueued(mixinConfig);
                    }
                }
            }

            for (ModContainer container : this.loader.getActiveModList()) {
                modClassLoader.addFile(container.getSource());
            }

            Field transformerField = Proxy.class.getDeclaredField("transformer");
            transformerField.setAccessible(true);
            Object transformer = transformerField.get(Bouncepad.classLoader.getTransformers().stream().filter(t -> t instanceof Proxy).findFirst().get());

            Class<?> mixinTransformerClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");

            Field processorField = mixinTransformerClass.getDeclaredField("processor");
            processorField.setAccessible(true);
            Object processor = processorField.get(transformer);

            Class<?> mixinProcessorClass = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");

            Method selectConfigsMethod = mixinProcessorClass.getDeclaredMethod("selectConfigs", MixinEnvironment.class);
            selectConfigsMethod.setAccessible(true);

            MixinEnvironment env = MixinEnvironment.getCurrentEnvironment();
            selectConfigsMethod.invoke(processor, env);

            try {
                Method prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class);
                prepareConfigsMethod.setAccessible(true);
                prepareConfigsMethod.invoke(processor, env);
            } catch (NoSuchMethodException e) { // 0.8.3+
                Class<?> extensionsClass = Class.forName("org.spongepowered.asm.mixin.transformer.ext.Extensions");
                Method prepareConfigsMethod = mixinProcessorClass.getDeclaredMethod("prepareConfigs", MixinEnvironment.class, extensionsClass);
                prepareConfigsMethod.setAccessible(true);

                Field extensionsField = mixinProcessorClass.getDeclaredField("extensions");
                extensionsField.setAccessible(true);
                Object extensions = extensionsField.get(processor);

                prepareConfigsMethod.invoke(processor, env, extensions);
            }

        }
    }

}

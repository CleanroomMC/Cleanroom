package net.minecraftforge.fml.relauncher;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.mixinfix.MixinFixer;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.util.HashSet;
import java.util.Map;

public class MixinSetup implements IFMLCallHook {

    @Override
    public void injectData(Map<String, Object> data) {
        addTransformationExclusions();
        initialize();
        MixinBooterPlugin.LOGGER.info("Initializing Mixins...");
        MixinBootstrap.realInit();
        MixinBooterPlugin.LOGGER.info("Initializing MixinExtras...");
        MixinExtrasBootstrap.init();
        MixinFixer.patchAncientModMixinsLoadingMethod();
    }

    @Override
    public Void call() throws Exception {
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
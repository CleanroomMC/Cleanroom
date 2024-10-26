package net.minecraftforge.fml.relauncher;

import com.cleanroommc.bouncepad.Bouncepad;
import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.relauncher.mixinfix.MixinFixer;
import org.spongepowered.asm.launch.GlobalProperties;
import org.spongepowered.asm.launch.MixinBootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;

public class MixinSetup implements IFMLCallHook {

    @Override
    public void injectData(Map<String, Object> data) {
        addTransformationExclusions();
        initialize();
        try {
            MixinBooterPlugin.LOGGER.info("Initializing Mixins...");
            Class<?> clazz = Bouncepad.classLoader.findClass(MixinBootstrap.class.getName());
            Method init = clazz.getMethod("realInit", new Class[0]);
            init.invoke(null, new Object[0]);
            //MixinBootstrap.realInit();
            MixinBooterPlugin.LOGGER.info("Initializing MixinExtras...");
            clazz = Bouncepad.classLoader.findClass(MixinExtrasBootstrap.class.getName());
            init = clazz.getMethod("init", new Class[0]);
            init.invoke(null, new Object[0]);
            //MixinExtrasBootstrap.init();
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException ignored) {}
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
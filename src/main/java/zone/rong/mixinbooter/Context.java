package zone.rong.mixinbooter;

import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;

import java.util.Collection;

/**
 * This class contains loading context for callers
 *
 * 
 * @since 10.0
 * @deprecated We forked Mixin.
 * <br>New approach:
 * Check <a href=https://github.com/CleanroomMC/Fugue/tree/master>Fugue</a>.<br>
 * Summary:<br>
 * If you are coremod, just call {@link org.spongepowered.asm.mixin.Mixins#addConfigurations(String...)} in loadingPluging or Tweaker, and handle shouldApply in IMixinConfigPlugin<br>
 * If you aren't coremod:<br>
 * Group mixins by phase, add target env in config, use @env(MOD) for mod mixins.<br>
 * Add {"MixinConfigs": "modid.mod.mixin.json,modid.default.mixin.json"} to your jar manifest.<br>
 * Handle shouldApply in IMixinConfigPlugin. You can call {@link net.minecraftforge.fml.common.Loader#isModLoaded(String)} for {@link org.spongepowered.asm.mixin.MixinEnvironment.Phase#MOD} mixin.<br>
 * Recommend to group target mod name by package name. You can also get config instance from {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin#injectConfig(org.spongepowered.asm.mixin.transformer.Config)}.
 */
@Deprecated
public final class Context {

    public enum ModLoader {
        FORGE,
        CLEANROOM;
    }

    private final String mixinConfig;
    private final Collection<String> presentMods;

    public Context(String mixinConfigIn, Collection<String> presentModsIn) {
        this.mixinConfig = mixinConfigIn;
        this.presentMods = presentModsIn;
    }

    public Context(String mixinConfigIn) {
        this(mixinConfigIn, null);
    }

    /**
     * @return the current mod loader
     */
    public ModLoader modLoader() {
        return ModLoader.CLEANROOM;
    }

    /**
     * @return if the current environment is in-dev
     */
    public boolean inDev() {
        return FMLLaunchHandler.isDeobfuscatedEnvironment();
    }

    /**
     * @return name of the mixin config that is currently being processed
     */
    public String mixinConfig() {
        return mixinConfig;
    }

    /**
     * <p>For early contexts, the list of mods are gathered from culling the classloader
     * for any jars that has the mcmod.info file. The mod IDs are obtained from the mcmod.info file.
     * This means mostly, if not only coremods are queryable here,
     * make sure to test a normal mod's existence in your mixin plugin or in the mixin itself.</p>
     *
     * <p>For late contexts, it comes from {@link net.minecraftforge.fml.common.Loader#getActiveModList}
     * akin to {@link net.minecraftforge.fml.common.Loader#isModLoaded(String)}</p>
     * @param modId to check against the list of present mods in the context
     * @return whether the mod is present
     */
    public boolean isModPresent(String modId) {
        return presentMods == null ? Loader.isModLoaded(modId) : presentMods.contains(modId);
    }

}
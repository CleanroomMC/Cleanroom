package zone.rong.mixinbooter;

import java.util.Set;

/**
 * Hijackers are used to stop certain mixin configurations from ever being applied.
 * Usage is similar to {@link IEarlyMixinLoader}, implement it in your coremod class.
 * Requested by: @Desoroxxx
 *
 * @since 9.0
 * @deprecated We forked Mixin.
 * <br>New approach:
 * Check <a href=https://github.com/CleanroomMC/Fugue/tree/master>Fugue</a>.<br>
 * Summary:<br>
 * If you are coremod, just call {@link org.spongepowered.asm.mixin.Mixins#addConfigurations(String...)} in loadingPlugin or Tweaker, and handle shouldApply in IMixinConfigPlugin<br>
 * If you aren't coremod:<br>
 * Group mixins by phase, add target env in config, use @env(MOD) for mod mixins.<br>
 * Add {"MixinConfigs": "modid.mod.mixin.json,modid.default.mixin.json"} to your jar manifest.<br>
 * Handle shouldApply in IMixinConfigPlugin. You can call {@link net.minecraftforge.fml.common.Loader#isModLoaded(String)} for {@link org.spongepowered.asm.mixin.MixinEnvironment.Phase#MOD} mixin.<br>
 * Recommend to group target mod name by package name. You can also get config instance from {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin#injectConfig(org.spongepowered.asm.mixin.transformer.Config)}.
 * 
 * If you what to block a mixin config, {@code GlobalProperties.get(GlobalProperties.Keys.CLEANROOM_DISABLE_MIXIN_CONFIGS)#add(String)}
 */

@Deprecated
public interface IMixinConfigHijacker {

    /**
     * Return a set of mixin config names to not be loaded by the mixin environment.
     *
     * @since 9.0
     */
    Set<String> getHijackedMixinConfigs();

    /**
     * Return a set of mixin config names to not be loaded by the mixin environment.
     *
     * @since 10.3
     * @param context current context of the loading process. Mixin config will be null as it is not applicable.
     */
    default Set<String> getHijackedMixinConfigs(Context context) {
        return getHijackedMixinConfigs();
    }

}
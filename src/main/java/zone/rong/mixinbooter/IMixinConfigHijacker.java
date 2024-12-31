package zone.rong.mixinbooter;

import org.spongepowered.asm.mixin.transformer.Config;

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
 * Recommend to group target mod name by package name. You can also get config instance from {@link org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin#injectConfig(Config)}.
 */

@Deprecated
public interface IMixinConfigHijacker {

    Set<String> getHijackedMixinConfigs();

}
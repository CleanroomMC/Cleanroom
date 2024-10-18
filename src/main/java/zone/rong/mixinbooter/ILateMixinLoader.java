package zone.rong.mixinbooter;

import net.minecraftforge.fml.common.Loader;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.transformer.Config;

import java.util.List;

/**
 * @deprecated We forked Mixin.
 * <br>New approach:
 * Check <a href=https://github.com/CleanroomMC/Fugue/tree/master>Fugue</a>.<br>
 * Summary:<br>
 * If you are coremod, just call {@link org.spongepowered.asm.mixin.Mixins#addConfigurations(String...)} in loadingPluging or Tweaker, and handle shouldApply in IMixinConfigPlugin<br>
 * If you aren't coremod:<br>
 * Group mixins by phase, add target env in config, use @env(MOD) for mod mixins.<br>
 * Add {"MixinConfigs": "modid.mod.mixin.json,modid.default.mixin.json"} to your jar manifest.<br>
 * Handle shouldApply in IMixinConfigPlugin. You can call {@link Loader#isModLoaded(String)} for {@link MixinEnvironment.Phase#MOD} mixin.<br>
 * Recommend to group target mod name by package name. You can also get config instance from {@link IMixinConfigPlugin#injectConfig(Config)}.
 */
@Deprecated
public interface ILateMixinLoader {

    /**
     * @return mixin configurations to be queued and sent to Mixin library.
     */
    List<String> getMixinConfigs();

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     *
     * @param mixinConfig mixin config name, queried via {@link ILateMixinLoader#getMixinConfigs()}.
     * @return true if the mixinConfig should be queued, false if it should not.
     */
    default boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     * @param mixinConfig mixin config name, queried via {@link ILateMixinLoader#getMixinConfigs()}.
     */
    default void onMixinConfigQueued(String mixinConfig) { }

}

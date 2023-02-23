package zone.rong.mixinbooter;

import java.util.List;

/**
 * Early mixins are defined as mixins that affects vanilla or forge classes.
 * Or technically, classes that can be queried via the current state of {@link net.minecraft.launchwrapper.LaunchClassLoader}
 *
 * If you want to add mixins that affect mods, use {@link ILateMixinLoader}
 *
 * Implement this in your {@link net.minecraftforge.fml.relauncher.IFMLLoadingPlugin}.
 * Return all early mixin configs you want MixinBooter to queue and send to Mixin library.
 */
public interface IEarlyMixinLoader {

    /**
     * @return mixin configurations to be queued and sent to Mixin library.
     */
    List<String> getMixinConfigs();

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     *
     * @param mixinConfig mixin config name, queried via {@link IEarlyMixinLoader#getMixinConfigs()}.
     * @return true if the mixinConfig should be queued, false if it should not.
     */
    default boolean shouldMixinConfigQueue(String mixinConfig) {
        return true;
    }

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     * @param mixinConfig mixin config name, queried via {@link IEarlyMixinLoader#getMixinConfigs()}.
     */
    default void onMixinConfigQueued(String mixinConfig) { }

}

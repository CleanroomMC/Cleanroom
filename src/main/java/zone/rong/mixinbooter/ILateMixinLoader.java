package zone.rong.mixinbooter;

import java.util.List;

/**
 * Late mixins are defined as mixins that affects mod classes.
 * Or technically, classes that can be queried via the current state of
 * {@link net.minecraft.launchwrapper.LaunchClassLoader} and {@link net.minecraftforge.fml.common.ModClassLoader}
 *
 * Majority if not all vanilla and forge classes would have been loaded here.
 * If you want to add mixins that affect vanilla or forge, use and consult {@link IEarlyMixinLoader}
 *
 * Implement this in any arbitrary class. Said class will be constructed when mixins are ready to be queued.
 * Return all late mixin configs you want MixinBooter to queue and send to Mixin library.
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

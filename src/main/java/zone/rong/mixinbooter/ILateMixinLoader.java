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
 *
 * @deprecated as of 11.0, the line of "early" and "late" mixin loading no longer is present, use
 *             {@code MixinConfigs} manifest entry to list your configs or {@code MixinConnector} to denote
 *             a class implementing {@link org.spongepowered.asm.mixin.connect.IMixinConnector} and call
 *             {@link org.spongepowered.asm.mixin.Mixins#addConfiguration(String)} (or related methods) there
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
     * @since 10.0
     * @param context current context of the loading process.
     * @return true if the mixinConfig should be queued, false if it should not.
     */
    default boolean shouldMixinConfigQueue(Context context) {
        return this.shouldMixinConfigQueue(context.mixinConfig());
    }

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
     * @since 10.0
     * @param context current context of the loading process.
     */
    default void onMixinConfigQueued(Context context) {
        this.onMixinConfigQueued(context.mixinConfig());
    }

    /**
     * Runs when a mixin config is successfully queued and sent to Mixin library.
     * @param mixinConfig mixin config name, queried via {@link ILateMixinLoader#getMixinConfigs()}.
     */
    default void onMixinConfigQueued(String mixinConfig) { }

}

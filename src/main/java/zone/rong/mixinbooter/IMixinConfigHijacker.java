package zone.rong.mixinbooter;

import java.util.Set;

/**
 * Hijackers are used to stop certain mixin configurations from ever being applied.
 * Usage is similar to {@link IEarlyMixinLoader}, implement it in your coremod class.
 * Requested by: @Desoroxxx
 *
 * @since 9.0
 * @deprecated since 11.0, use {@link org.spongepowered.asm.mixin.transformer.Config#blacklist(String)}
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

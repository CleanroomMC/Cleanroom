package zone.rong.mixinbooter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Purely a marker annotation. Use it in classes and said classes will be instantiated.
 * Make sure there is an empty-arg constructor as LoaderMixin will call newInstance on it.
 * Feel free to do any Mixin related things in the constructor. But, most importantly, add (mod mixin) configs there.
 *
 * @since 4.2 this class was deprecated.
 * @since 9.2 this class was reinstated because of Forge not gathering interfaces in 1.8.x.
 * When used and paired with {@link ILateMixinLoader}, it will act the same way as it would in a 1.9+ setting.
 * Consult the usage of {@link ILateMixinLoader} for mod mixins, and for vanilla/forge mixins, consult the usage of {@link IEarlyMixinLoader}
 * @deprecated since 11.0, use manifest entries MixinConfigs and MixinConnector instead, as original Mixin intended
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
@Deprecated
public @interface MixinLoader { }

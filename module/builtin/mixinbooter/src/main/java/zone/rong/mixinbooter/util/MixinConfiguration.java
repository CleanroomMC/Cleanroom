package zone.rong.mixinbooter.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @since 12.0
 * @deprecated use {@link com.cleanroommc.cleanmix.MixinConfiguration}
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MixinConfiguration {

    String name();

    Dependency[] depends() default { };

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ })
    @interface Dependency {

        String modid();

        String version() default "";

        boolean crash() default true;

        String reason() default "";

    }

}

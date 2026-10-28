package fermiumbooter.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface MixinConfig {

    String name();

    boolean classloadEarly() default false;

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface MixinToggle {

        String mixinJson() default "";

        @Deprecated
        String earlyMixin() default "";

        @Deprecated
        String lateMixin() default "";

        boolean defaultValue();

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @Repeatable(CompatHandlingContainer.class)
    @interface CompatHandling {

        String modid();

        String modName() default "";

        String targetVersionRange() default "";

        boolean desired();

        boolean disableMixin() default true;

        boolean warnIngame() default true;

        String reason() default "Undefined";

    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    @interface CompatHandlingContainer {

        CompatHandling[] value();

    }

}

package zone.rong.mixinextras.injector.wrapoperation;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Slice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WrapOperation {
  String[] method();

  /**
   * Selector for targeting method calls and field gets/sets.
   */
  At[] at() default {};

  /**
   * Selector for targeting `instanceof`s.
   */
  Constant[] constant() default {};

  Slice[] slice() default {};

  boolean remap() default true;

  int require() default -1;

  int expect() default 1;

  int allow() default -1;
}
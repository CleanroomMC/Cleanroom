package zone.rong.mixinextras.injector.wrapoperation;

/**
 * Represents an operation (method call or field get/set) that has been wrapped by {@link WrapOperation}.
 * This may either be the operation the user targeted originally, or a wrapped version of it, allowing for chaining.
 * @param <R> the return type of the operation.
 */
@FunctionalInterface
public interface Operation<R> {
  R call(Object... args);
}
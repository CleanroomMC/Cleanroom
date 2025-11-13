package com.cleanroommc.kirino.ecs.job;

import org.jspecify.annotations.NonNull;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface JobDataQuery {
    @NonNull Class<?> componentClass();
    @NonNull String @NonNull [] fieldAccessChain();
}

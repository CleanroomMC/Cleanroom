package com.cleanroommc;

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(CleanroomTestExtension.class)
public @interface CleanroomTest {
}

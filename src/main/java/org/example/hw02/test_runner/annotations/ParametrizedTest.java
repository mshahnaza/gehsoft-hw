package org.example.hw02.test_runner.annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ParametrizedTest {
    int[] intValues() default {};
    String[] stringValues() default {};
}

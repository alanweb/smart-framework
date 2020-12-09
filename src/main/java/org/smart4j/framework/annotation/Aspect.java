package org.smart4j.framework.annotation;

import java.lang.annotation.*;

/**
 * 切面注解  对指定的注解进行切面
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Aspect {
    /**
     * 注解
     * */
    Class<? extends Annotation> value();
}

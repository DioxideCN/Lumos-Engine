package cn.dioxide.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 挂载
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LoopThis {
    String configKey() default "";
    long delay() default 0L;
    long period() default 20L;
}

package cn.dioxide.common.annotation;

import cn.dioxide.common.infra.CustomType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Custom {
    String value();
    CustomType type() default CustomType.ITEM_TYPE;
    String skillName() default "";
}

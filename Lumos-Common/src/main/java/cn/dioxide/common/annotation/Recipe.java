package cn.dioxide.common.annotation;

import org.bukkit.inventory.ShapedRecipe;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 被@Recipe修饰的方法必须返回{@link ShapedRecipe}类
 * 这个类会被自动注入到Bukkit的合成配方中
 *
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Recipe {
}

package cn.dioxide.spigot.custom;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.LoopThis;
import cn.dioxide.common.annotation.Recipe;
import cn.dioxide.common.extension.ReflectFactory;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;

/**
 * 注册中心，所有CustomItem将被初始化后的容器自动注册到itemMap中
 * 这些注册物件会通过{@link #get(String)}发布给其他广义类调用
 *
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
public class CustomRegister {

    private static final HashMap<String, CustomItem> itemMap = new HashMap<>(200);

    public static void init() {
        // 使用之前的BeanHolder对@Custom自动注入到注册表
        // 具体还是参考BeanHolder的注册过程和生命周期管理
        for (Class<?> clazz : ReflectFactory.use().getClassSet()) {
            Custom custom = clazz.getAnnotation(Custom.class);
            if (custom != null) {
                try { // 强制反射
                    String key = custom.value();
                    Field itemField = clazz.getDeclaredField("item");
                    itemField.setAccessible(true);
                    CustomItem item = (CustomItem) itemField.get(null);
                    // 将物品自动注册到HashMap中
                    itemMap.put(key, item);
                    // 尝试注册Recipe解析@Recipe注解
                    for (Method method : clazz.getMethods()) {
                        if (method.isAnnotationPresent(Recipe.class) &&
                                Modifier.isPublic(method.getModifiers()) &&
                                Modifier.isStatic(method.getModifiers()) &&
                                method.getParameterCount() == 0) {
                            try {
                                // 获取方法的返回值
                                Object recipe = method.invoke(null);
                                // 检查返回值是否为 Recipe 类型
                                if (recipe instanceof ShapedRecipe result) {
                                    // 添加合成配方到服务器
                                    Bukkit.getServer().addRecipe(result);
                                }
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace(); // 隐式抛出异常
                            }
                        }
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace(); // 隐式抛出异常
                }
            }
        }
    }

    public static ItemStack get(String key) {
        return itemMap.get(key).build();
    }

    public static List<String> getKeySet() {
        return itemMap.keySet().stream().toList();
    }

}

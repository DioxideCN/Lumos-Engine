package cn.dioxide.spigot.custom;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Recipe;
import cn.dioxide.common.extension.Pair;
import cn.dioxide.common.extension.ReflectFactory;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.spigot.custom.structure.ICustomSkillHandler;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.*;
import java.util.*;

/**
 * 注册中心，所有CustomItem将被初始化后的容器自动注册到itemMap中
 * 这些注册物件会通过{@link #get(String)}发布给其他广义类调用
 *
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
public class CustomRegister {

    private static final HashMap<String, ItemStack> itemMap = new HashMap<>(200);
    private static final HashMap<String, Pair<String, ICustomSkillHandler>> skillMap = new HashMap<>(200);

    public static void init() {
        // 使用之前的BeanHolder对@Custom自动注入到注册表
        // 具体还是参考BeanHolder的注册过程和生命周期管理
        for (Class<?> clazz : ReflectFactory.use().getClassSet()) {
            Custom custom = clazz.getAnnotation(Custom.class);
            if (custom != null) {
                try { // 强制反射
                    CustomType type = custom.type();
                    String key = custom.value();
                    if (type == CustomType.SKILL_TYPE) {
                        // 是否是ICustomSkillHandler接口
                        Class<?>[] interfaces = clazz.getInterfaces();
                        if (Arrays.asList(interfaces).contains(ICustomSkillHandler.class)) {
                            // 如果clazz实际上是Class<ICustomSkillHandler>的一个实例
                            if (ICustomSkillHandler.class.isAssignableFrom(clazz)) {
                                // 在这里，我们安全地将clazz转换为Class<ICustomSkillHandler>
                                @SuppressWarnings("unchecked") // 我们已经检查过类型，所以这是安全的
                                ICustomSkillHandler instance = getiCustomSkillHandler((Class<ICustomSkillHandler>) clazz);
                                skillMap.put(key, Pair.of(custom.skillName(), instance));
                            }
                        }
                        continue;
                    }

                    Field itemField = clazz.getDeclaredField("item");
                    itemField.setAccessible(true);
                    ItemStack item = (ItemStack) itemField.get(null);
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
                } catch (NoSuchFieldException |
                         IllegalAccessException |
                         NoSuchMethodException |
                         InvocationTargetException |
                         InstantiationException e) {
                    e.printStackTrace(); // 隐式抛出异常
                }
            }
        }
    }

    @NotNull
    private static ICustomSkillHandler getiCustomSkillHandler(Class<ICustomSkillHandler> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<ICustomSkillHandler> constructor = clazz.getConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    public @Nullable static ItemStack get(String key) {
        return itemMap.get(key);
    }

    public static Map<String, Pair<String, ICustomSkillHandler>> get() {
        return skillMap;
    }

    public static List<String> getKeySet() {
        return itemMap.keySet().stream().toList();
    }

}

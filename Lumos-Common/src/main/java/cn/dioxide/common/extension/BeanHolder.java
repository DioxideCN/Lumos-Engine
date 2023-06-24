package cn.dioxide.common.extension;

import cn.dioxide.common.annotation.Executor;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.annotation.LoopThis;
import cn.dioxide.common.annotation.ScanPackage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class BeanHolder {

    private static JavaPlugin plugin;

    public static void init(@NotNull JavaPlugin instance) {
        plugin = instance;
        Format.use().plugin().info("&aInjecting command and event...");
        Format.use().plugin().info("&7============&f[&3inject&f]&7============");

        // 包扫描
        ReflectFactory.use().scanAllPackage(instance.getClass().getPackageName());
        ScanPackage scanner = instance.getClass().getAnnotation(ScanPackage.class);
        if (scanner != null) {
            for (String pkg : scanner.value()) {
                if (!pkg.isEmpty()) {
                    ReflectFactory.use().scanAllPackage(pkg);
                }
            }
        }

        BeanHolder.use().loadBean();
        BeanHolder.use().loadLoopTask();
        Format.use().plugin().info("&7==============================");
    }

    protected void loadBean() {
        int successCommand = 0, failCommand = 0, successEvent = 0, failEvent = 0;
        for (Class<?> clazz : ReflectFactory.use().getClassSet()) {
            Executor executor = clazz.getDeclaredAnnotation(Executor.class);
            Event event = clazz.getDeclaredAnnotation(Event.class);
            if (executor == null && event == null) {
                continue;
            }
            try {
                // 反射轴承
                Constructor<?> beanConstructor = clazz.getDeclaredConstructor();
                beanConstructor.setAccessible(true);
                Object beanInstance = beanConstructor.newInstance();
                Class<?>[] interfaces = clazz.getInterfaces();
                if (executor != null) {
                    boolean isCommandExecutor = Arrays.asList(interfaces).contains(CommandExecutor.class);
                    boolean isTabExecutor = Arrays.asList(interfaces).contains(TabExecutor.class);
                    // 只有CommandExecutor没有TabExecutor
                    if (isCommandExecutor && !isTabExecutor) {
                        Objects.requireNonNull(Bukkit.getPluginCommand(executor.name())).setExecutor((CommandExecutor)beanInstance);
                        successCommand++;
                        continue;
                    }
                    // 包含TabExecutor
                    if (isTabExecutor) {
                        Objects.requireNonNull(Bukkit.getPluginCommand(executor.name())).setExecutor((CommandExecutor)beanInstance);
                        Objects.requireNonNull(Bukkit.getPluginCommand(executor.name())).setTabCompleter((TabCompleter)beanInstance);
                        successCommand++;
                        continue;
                    }
                    failCommand++;
                }
                if (event != null) {
                    if (Boolean.TRUE.equals(Config.FEATURE_KEY_MAP.get(event.configKey())) || event.configKey().isEmpty()) {
                        boolean isListener = Arrays.asList(interfaces).contains(Listener.class);
                        if (isListener) {
                            Bukkit.getPluginManager().registerEvents((Listener) beanInstance, plugin);
                            successEvent++;
                            continue;
                        }
                        failEvent++;
                    }
                }
            } catch (NoSuchMethodException |
                     InvocationTargetException |
                     InstantiationException |
                     IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        Format.use().plugin().info("&3Injecting commands &asuccess&7: &f" + successCommand + " &cfail&7: &f" + failCommand);
        Format.use().plugin().info("&3Injecting events &asuccess&7: &f" + successEvent + " &cfail&7: &f" + failEvent);
    }

    private void loadLoopTask() {
        int success = 0, fail = 0;
        for (Class<?> clazz : ReflectFactory.use().getClassSet()) {
            for (Method method : clazz.getMethods()) {
                if (method.isAnnotationPresent(LoopThis.class) &&
                        Modifier.isPublic(method.getModifiers()) &&
                        Modifier.isStatic(method.getModifiers()) &&
                        method.getParameterCount() == 0) {
                    LoopThis loopThis = method.getAnnotation(LoopThis.class);
                    if (Boolean.TRUE.equals(Config.FEATURE_KEY_MAP.get(loopThis.configKey())) || loopThis.configKey().isEmpty()) {
                        try {
                            Runnable task = () -> {
                                try {
                                    method.invoke(null);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            };
                            plugin.getServer().getScheduler().runTaskTimer(plugin, task, loopThis.delay(), loopThis.period());
                            success++;
                        } catch (Exception e) {
                            e.printStackTrace();
                            fail++;
                        }
                    }
                }
            }
        }
        Format.use().plugin().info("&3Injecting loop tasks &asuccess&7: &f" + success + " &cfail&7: &f" + fail);
    }

    protected volatile static BeanHolder INSTANCE = null;
    protected BeanHolder() {}
    public static BeanHolder use() {
        if (INSTANCE == null) {
            synchronized (BeanHolder.class) {
                if (INSTANCE == null) INSTANCE = new BeanHolder();
            }
        }
        return INSTANCE;
    }

}

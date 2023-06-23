package cn.dioxide.common.extension;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * 反射工厂主要集中处理具体包下的类文件，通过反射工厂可以拿到所有类文件。反射工厂先于所有工厂被使用和创建
 *
 * @author Dioxide.CN
 * @date 2023/1/17 12:13
 * @since 1.3
 */
public class ReflectFactory {

    private final Reflect reflect = new Reflect();

    private final Set<Class<?>> sourceClassSet = new HashSet<>();

    public void scanAllPackage(@NotNull String packagePath) {
        // 装载所有Class文件到该集合中
        sourceClassSet.addAll(reflect.getClasses(packagePath));
    }

    public Set<Class<?>> getClassSet() {
        return sourceClassSet;
    }

    private volatile static ReflectFactory INSTANCE = null;
    private ReflectFactory() {}

    public static ReflectFactory use() {
        if (INSTANCE == null) {
            synchronized (ReflectFactory.class) {
                if (INSTANCE == null) INSTANCE = new ReflectFactory();
            }
        }
        return INSTANCE;
    }

}

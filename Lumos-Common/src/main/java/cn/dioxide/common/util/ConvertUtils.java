package cn.dioxide.common.util;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

/**
 * 将字符串转换为ItemDisplayTransform枚举
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class ConvertUtils {

    public static ItemDisplay.ItemDisplayTransform getTransformType(String s) {
        try {
            return ItemDisplay.ItemDisplayTransform.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            // 如果输入的字符串没有与任何枚举常量匹配，valueOf() 方法会抛出一个 IllegalArgumentException。
            return null;
        }
    }

    /**
     * 将String转换为TrimPattern枚举
     * @param name String Key
     * @return TrimPattern
     */
    public static TrimPattern getTrimPatternByName(String name) {
        try {
            return (TrimPattern) TrimPattern.class.getField(name.toUpperCase()).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid TrimPattern name: " + name);
        }
    }

    /**
     * 将String转换为TrimMaterial枚举
     * @param name String Key
     * @return TrimMaterial
     */
    public static TrimMaterial getTrimMaterialByName(String name) {
        try {
            return (TrimMaterial) TrimMaterial.class.getField(name.toUpperCase()).get(null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid TrimPattern name: " + name);
        }
    }

}

package cn.dioxide.common.infra;

import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * TrimUpgradeStore存储每一个配置项，需要用集合来存储全部的
 * @author Dioxide.CN
 * @date 2023/6/22
 * @since 1.0
 */
public class TrimUpgradeStore {
    // 纹饰类型，一个枚举类
    public TrimPattern trimPattern;
    // 纹饰的材质，一个枚举类
    public List<TrimMaterial> trimMaterials;
    // 支持的盔甲类型
    public List<String> armorMaterial;
    // 套件增益
    public List<Buff> buffList;

    public static class Buff {
        // 套件的数量
        public int count;
        // 药水效果，可能不存在
        public @Nullable PotionEffectType effect;
        // 药水效果等级
        public int effectLevel;
        // 药水持续时间
        public int effectDuration;
        // 药水作用于谁
        public EffectTarget effectTarget;
        // 事件，可能不存在
        public @Nullable EventType event;
        // 事件发生概率
        public double chance;
    }
}

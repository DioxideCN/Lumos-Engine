package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.common.util.PlayerUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.entity.Player;

/**
 * 影响因子，这分为两部分，第一部分决定产出的副产物，第二部分决定获得的强力附魔效果
 * 影响因子的构造由skill类的selfFactor方法提供并在CustomRegister中自动注册到容器中
 *
 * @author Dioxide.CN
 * @date 2023/7/19
 * @since 1.0
 */
@Getter
@Setter
public class Factor {

    private final Quality quality; // 品质 -> 根据品质进行分区偏移
    // 以下数值均为0-100的值
    private final float loyalty; // 忠诚 -> 由祈愿宝盒的内容物的数量、品质、位置决定
    private final float temperature; // 温度 -> 由群系决定 -> 反应附魔属性所需要的温度
    private final float dimension; // 维度性 -> 由所在维度决定 -> 越大越偏向末地越小越偏向下届

    public static Factor with(Quality quality, float loyalty, float temperature, World.Environment dimension) {
        float de = -1.0F;
        switch (dimension) {
            case NORMAL -> de = 0.0F;
            case NETHER -> de = 5.0F;
            case THE_END -> de = 10.0F;
        }
        return new Factor(quality, loyalty, temperature, de);
    }

    private Factor(Quality quality, float loyalty, float temperature, float dimension) {
        this.quality = quality;
        this.loyalty = loyalty;
        this.temperature = temperature;
        this.dimension = dimension;
    }

    public float distance(Factor f) {
        return calcPow(this.loyalty, f.loyalty) +
                calcPow(this.temperature, f.temperature) +
                calcPow(this.dimension, f.dimension);
    }

    // Quality为均值为10的正太分布函数
    public static Factor getFromPlayer(Player player, Altar altar) {
        return new Factor(Quality.random(),
                altar.getLoyalty(),
                getTemperatureFactor(player),
                getDimensionFactor(player));
    }

    @Unsafe(proposer = "Dioxide_CN")
    private float calcPow(float i1, float i2) {
        return (float) Math.pow(i1 - i2, 2);
    }

    // NMS获取群系温度
    public static float getTemperatureFactor(Player player) {
        Biome biome = PlayerUtils.getBiome(player);
        if (biome == null) return -1.0F;
        return biome.getBaseTemperature();
    }

    // 获取维度影响因子
    private static float getDimensionFactor(Player player) {
        World world = player.getLocation().getWorld();
        if (world == null) return -1.0F;
        World.Environment environment = world.getEnvironment();
        return switch (environment) {
            case NORMAL -> 0.0F;
            case NETHER -> 5.0F;
            case THE_END -> 10.0F;
            default -> -1.0F;
        };
    }

    @Override
    public String toString() {
        return "Factor{" +
                "quality=" + quality +
                ", loyalty=" + loyalty +
                ", temperature=" + temperature +
                ", dimension=" + dimension +
                '}';
    }
}

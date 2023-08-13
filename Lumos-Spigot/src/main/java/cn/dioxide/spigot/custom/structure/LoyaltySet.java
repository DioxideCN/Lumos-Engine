package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.extension.Pair;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/8/6
 * @since 1.0
 */
public class LoyaltySet {

    public static final Map<Material, Float> CHART = Map.ofEntries(
            // 对于Treasure每个虔诚值属于(0, 0.8]区间中 avg=0.33
            Map.entry(Material.NETHERITE_INGOT, 0.69F),
            Map.entry(Material.DIAMOND, 0.31F),
            Map.entry(Material.EMERALD, 0.36F),
            Map.entry(Material.GOLD_INGOT, 0.16F),
            Map.entry(Material.IRON_INGOT, 0.13F),
            Map.entry(Material.COAL, 0.22F),
            Map.entry(Material.LAPIS_LAZULI, 0.07F),
            Map.entry(Material.COPPER_INGOT, 0.04F),
            Map.entry(Material.SLIME_BALL, 0.02F),
            Map.entry(Material.QUARTZ, 0.02F),
            Map.entry(Material.AMETHYST_SHARD, 0.18F),
            Map.entry(Material.NETHER_STAR, 0.8F),
            Map.entry(Material.TORCHFLOWER_CROP, 0.07F),
            Map.entry(Material.PITCHER_PLANT, 0.07F),
            Map.entry(Material.SCUTE, 0.37F),
            Map.entry(Material.ECHO_SHARD, 0.41F),
            Map.entry(Material.GOLDEN_APPLE, 0.51F),
            Map.entry(Material.ENCHANTED_GOLDEN_APPLE, 0.71F),
            Map.entry(Material.NAUTILUS_SHELL, 0.31F),
            Map.entry(Material.HEART_OF_THE_SEA, 0.8F),
            // 对于Junk每个虔诚值属于[-0.8, 0)区间中 avg=-0.34
            Map.entry(Material.ROTTEN_FLESH, -0.8F),
            Map.entry(Material.SKELETON_SKULL, -0.8F),
            Map.entry(Material.PUFFERFISH, -0.8F),
            Map.entry(Material.NETHER_WART, -0.76F),
            Map.entry(Material.STICK, -0.21F),
            Map.entry(Material.FEATHER, -0.08F),
            Map.entry(Material.BRICK, -0.08F),
            Map.entry(Material.STRING, -0.12F),
            Map.entry(Material.GLOWSTONE_DUST, -0.41F),
            Map.entry(Material.GUNPOWDER, -0.41F),
            Map.entry(Material.BLAZE_POWDER, -0.37F),
            Map.entry(Material.SNOWBALL, -0.29F),
            Map.entry(Material.POISONOUS_POTATO, -0.17F),
            Map.entry(Material.SPIDER_EYE, -0.33F),
            Map.entry(Material.FLINT, -0.03F),
            Map.entry(Material.BOWL, -0.19F),
            Map.entry(Material.REDSTONE, -0.53F),
            Map.entry(Material.CLAY_BALL, -0.19F),
            Map.entry(Material.INK_SAC, -0.19F),
            Map.entry(Material.BONE_MEAL, -0.29F),
            Map.entry(Material.EGG, -0.29F),
            Map.entry(Material.BONE, -0.29F)
    );

}

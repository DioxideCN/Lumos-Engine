package cn.dioxide.common.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * 计算类
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class CalcUtils {

    private static final Random RANDOM = new Random();

    public static boolean isOutOfRange(Player p, double x, double y, double z, int between) {
        Location playerLocation = p.getLocation();
        double distance = Math.sqrt(Math.pow(x - playerLocation.getX(), 2) +
                Math.pow(y - playerLocation.getY(), 2) +
                Math.pow(z - playerLocation.getZ(), 2));

        return !(distance <= between);
    }

    public static boolean checkProbability(double percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100.");
        }
        return RANDOM.nextInt(100) < percentage;
    }

    public static double getBaseDamage(ItemStack weapon) {
        return switch (weapon.getType()) {
            case WOODEN_SWORD, WOODEN_AXE -> 4;
            case STONE_SWORD, STONE_AXE -> 5;
            case IRON_SWORD, IRON_AXE -> 6;
            case DIAMOND_SWORD, DIAMOND_AXE -> 7;
            case NETHERITE_SWORD, NETHERITE_AXE -> 8;
            case TRIDENT -> 9;
            default -> 1;
        };
    }

}
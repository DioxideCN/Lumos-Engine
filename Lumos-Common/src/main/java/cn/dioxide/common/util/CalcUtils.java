package cn.dioxide.common.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * 计算类
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class CalcUtils {

    public static boolean isOutOfRange(Player p, double x, double y, double z, int between) {
        Location playerLocation = p.getLocation();
        double distance = Math.sqrt(Math.pow(x - playerLocation.getX(), 2) +
                Math.pow(y - playerLocation.getY(), 2) +
                Math.pow(z - playerLocation.getZ(), 2));

        return !(distance <= between);
    }

}
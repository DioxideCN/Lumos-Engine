package cn.dioxide.common.extension;

import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author Dioxide.CN
 * @date 2023/7/27
 * @since 1.0
 */
public record Position(int x, int y, int z) {
    // 将 Position 对象转换为 Bukkit Location 对象
    public Location toLocation(World world) {
        return new Location(world, x, y, z);
    }
}

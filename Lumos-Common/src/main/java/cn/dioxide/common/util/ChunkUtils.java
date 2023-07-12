package cn.dioxide.common.util;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/7/10
 * @since 1.0
 */
public class ChunkUtils {

    /**
     * 从对角loc1到对角loc2对称区域内的所有方块
     */
    public static List<Block> getRegionBlocks(Location location, int fx, int fy, int fz) {
        Location loc1 = location.clone().add(fx, fy, fz);
        Location loc2 = location.clone().subtract(fx, fy, fz);
        return getRegionBlocks(loc1, loc2);
    }

    /**
     * 从对角loc1到对角loc2区域内的所有方块
     */
    public static List<Block> getRegionBlocks(Location loc1, Location loc2) {
        List<Block> blocks = new ArrayList<>();
        // 获取坐标xyz
        int x1 = loc1.getBlockX();
        int y1 = loc1.getBlockY();
        int z1 = loc1.getBlockZ();
        int x2 = loc2.getBlockX();
        int y2 = loc2.getBlockY();
        int z2 = loc2.getBlockZ();
        // 遍历区域坐标
        for (int x = Math.min(x1, x2); x <= Math.max(x1, x2); x++) {
            for (int y = Math.min(y1, y2); y <= Math.max(y1, y2); y++) {
                for (int z = Math.min(z1, z2); z <= Math.max(z1, z2); z++) {
                    if (loc1.getWorld() == null) return blocks;
                    blocks.add(loc1.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

}

package cn.dioxide.spigot.custom.structure;

import cn.dioxide.spigot.custom.EnableCustomAltar;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/7/10
 * @since 1.0
 */
public class Altar {

    /**
     * 采取优化：
     * 判断block所在的位置是否是Altar中的一部分
     * 该方法先获取距离block最近磁石，这个过程的遍历次数为1-12次
     * 再判断最近的磁石周围的24个方块是否满足altar的结构要求，这个过程遍历次数为24次
     * 时间复杂度为O(n) -> [12, 36]
     * 如不采取优化：
     * 获取距离block最近磁石的遍历次数的最大值会翻倍到24次，最终时间复杂度趋于[24, 48]
     * 在调用本方法时，更推荐开发者使用响应式流+lambda进行开发
     */
    @Nullable
    public static Altar isAltar(Block block) {
        if (block == null) return null;
        Block nearestLodeStone = block.getType() == Material.LODESTONE ? block : findNearestLodeStone(block);
        if (nearestLodeStone == null) {
            return null;
        }
        World world = nearestLodeStone.getWorld();
        int x = nearestLodeStone.getX(), y = nearestLodeStone.getY(), z = nearestLodeStone.getZ();
        // 判断磁石附近的方块
        for (int i = 0; i < EnableCustomAltar.offsets.length; i++) {
            Block currentBlock = world.getBlockAt(x + EnableCustomAltar.offsets[i][0], y, z + EnableCustomAltar.offsets[i][1]);
            if (i < 4) { // [0,4)
                if (currentBlock.getType() != Material.END_STONE) {
                    return null;
                }
            } else if (i < 12) { // [4,12)
                if (currentBlock.getType() != Material.CRYING_OBSIDIAN) {
                    return null;
                }
            } else { // [12,24]
                if (currentBlock.getType() != Material.SCULK) {
                    return null;
                }
            }
        }
        return new Altar(nearestLodeStone);
    }

    @Nullable
    private static Block findNearestLodeStone(Block block) {
        Material type = block.getType();
        World world = block.getWorld();
        int x = block.getX(), y = block.getY(), z = block.getZ(), start = 0, end = 0;
        // 优化判断逻辑减少循环次数
        if (type == Material.END_STONE) {
            end = start + 4;
        } else if (type == Material.CRYING_OBSIDIAN) {
            start = 4;
            end = start + 8;
        } else if (type == Material.SCULK) {
            start = 12;
            end = start + 12;
        }
        // 检查是否为磁石附近的位置
        for (; start < end; start++) {
            Block possibleLodeStone = world.getBlockAt(x + EnableCustomAltar.offsets[start][0], y, z + EnableCustomAltar.offsets[start][1]);
            if (possibleLodeStone.getType() == Material.LODESTONE) {
                return possibleLodeStone;
            }
        }
        return null;
    }

    private @NotNull final Block lodeStone;

    private Altar(Block block) {
        if (block.getType() != Material.LODESTONE) {
            throw new RuntimeException("Illegal lode stone");
        }
        this.lodeStone = block;
    }

    // 末影轮
    public @NotNull List<ItemStack> getEndGear() {
        int[][] offsets = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        return getGear(Material.END_STONE, offsets);
    }

    // 下届轮
    public @NotNull List<ItemStack> getNetherGear() {
        int[][] offsets = {{-4, 0}, {4, 0}, {0, -4}, {0, 4}, {3, 3}, {3, -3}, {-3, 3}, {-3, -3}};
        return getGear(Material.CRYING_OBSIDIAN, offsets);
    }

    // 潜声轮
    public @NotNull List<ItemStack> getSculkGear() {
        int[][] offsets = {{-6, 0}, {6, 0}, {0, -6}, {0, 6}, {3, 5}, {3, -5}, {-3, 5}, {-3, -5}, {5, 3}, {5, -3}, {-5, 3}, {-5, -3}};
        return getGear(Material.SCULK, offsets);
    }

    private @NotNull List<ItemStack> getGear(Material blockType, int[][] offsets) {
        ArrayList<ItemStack> result = new ArrayList<>();
        for (int[] offset : offsets) {
            Block block = lodeStone.getWorld().getBlockAt(lodeStone.getX() + offset[0], lodeStone.getY(), lodeStone.getZ() + offset[1]);
            if (block.getType() == blockType) {
                for (Entity entity : getEntityAbove(block)) {
                    if (entity instanceof ItemDisplay display) {
                        ItemStack itemStack = display.getItemStack();
                        result.add(itemStack);
                        break;
                    }
                }
            }
        }
        return result;
    }

    private List<Entity> getEntityAbove(Block block) {
        return block.getWorld()
                .getNearbyEntities(
                        block.getLocation().add(0, 1, 0), 1, 1, 1)
                .stream()
                .toList();
    }

    @Override
    public String toString() {
        return "{world: " + this.lodeStone.getWorld() +
                ", x: " + this.lodeStone.getX() +
                ", y: " + this.lodeStone.getY() +
                ", z: " + this.lodeStone.getZ() + "}";
    }

}

package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.common.util.MatrixUtils;
import cn.dioxide.spigot.custom.EnableCustomAltar;
import cn.dioxide.spigot.custom.ICustomSkillHandler;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
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
    public static Altar with(Block block) {
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

    // 祭坛被破坏
    public void destroy() {
        removeAllItem();
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

    public boolean match(ICustomSkillHandler recipe) {
        boolean result = matchItemStackList(this.getEndGear(), recipe.endGearRecipe())
                && matchItemStackList(this.getNetherGear(), recipe.netherGearRecipe())
                && matchItemStackList(this.getSculkGear(), recipe.sculkGearRecipe());
        if (result) removeAllItem(false);
        return result;
    }

    private boolean matchItemStackList(List<ItemStack> playerItems, List<ItemStack> targetItems) {
        if (playerItems.size() != targetItems.size()) {
            return false;
        }
        return playerItems.stream()
                .allMatch(playerItem -> targetItems.stream()
                        .anyMatch(targetItem -> isItemEqual(playerItem, targetItem)));
    }

    private boolean isItemEqual(ItemStack item1, ItemStack item2) {
        if (item1.getType() != item2.getType()) return false;
        if (item1.getType() == Material.AIR || item2.getType() == Material.AIR) return false;
        if (item1.getAmount() != item2.getAmount()) return false;
        ItemMeta meta1 = item1.getItemMeta();
        ItemMeta meta2 = item2.getItemMeta();
        if (meta1 == null || meta2 == null) return false;
        if (!meta1.getDisplayName().equals(meta2.getDisplayName())) return false;
        List<String> lore1 = meta1.getLore();
        List<String> lore2 = meta2.getLore();
        // 如果目标物品没有Lore但是玩家物品有Lore
        if (lore2 == null) return lore1 == null;
        // 如果此时玩家物品没有Lore，但是目标物品有Lore，那么物品不匹配
        if (lore1 == null) return false;
        // 如果两个物品的Lore都不为空，但是大小不同，那么物品不匹配
        if (lore1.size() != lore2.size()) return false;
        // 如果两个物品的Lore都不为空且大小相同，但是内容不完全一致，那么物品不匹配
        return new HashSet<>(lore1).containsAll(lore2);
    }

    private @NotNull final Block lodeStone;

    private Altar(Block block) {
        if (block.getType() != Material.LODESTONE) {
            throw new RuntimeException("Illegal lode stone");
        }
        this.lodeStone = block;
    }

    public Location getLoc() {
        return this.lodeStone.getLocation();
    }

    // 将武器放置到Altar的激活座上
    public boolean putWeapon(ItemStack item, Block block, double angle) {
        if (block == null || item == null || item.getType() == Material.AIR) return false;
        if (!getEntityAbove(block).isEmpty()) {
            removeItem(block);
        }
        ItemStack cloneItem = item.clone();
        Location spawnLocation = block.getLocation().add(0.5, 1.3, 0.5);
        ItemDisplay weaponDisplay = block.getWorld().spawn(spawnLocation, ItemDisplay.class);
        Matrix4f matrix;
        if (item.getType() == Material.TRIDENT) {
            matrix = MatrixUtils.getMatrix(angle, 0, 0, 1, 1, 1, -0.5, 0.7, -0.5);
        } else {
            matrix = MatrixUtils.getMatrix(0, 0, angle, 1, 1, 1);
        }
        weaponDisplay.setTransformationMatrix(matrix);
        weaponDisplay.addScoreboardTag("altar.item");
        weaponDisplay.setItemStack(cloneItem);
        return true;
    }

    @Nullable
    public ItemStack getWeapon() {
        List<ItemDisplay> displays = getEntityAbove(this.lodeStone);
        if (displays.isEmpty()) return null;
        for (ItemDisplay display : displays) {
            ItemStack weapon = display.getItemStack();
            if (weapon != null) {
                return weapon;
            }
        }
        return null;
    }

    @Unsafe(proposer = "Dioxide_CN")
    public void setWeapon(ItemStack weapon) {
        List<ItemDisplay> displays = getEntityAbove(this.lodeStone);
        if (displays.isEmpty()) return;
        for (ItemDisplay display : displays) {
            display.setItemStack(weapon);
        }
    }

    // 将物品放置到Altar的基座上
    public boolean putItem(ItemStack item, Block block) {
        if (block == null || item == null || item.getType() == Material.AIR) return false;
        if (!getEntityAbove(block).isEmpty()) {
            removeItem(block);
        }
        ItemStack cloneItem = item.clone();
        cloneItem.setAmount(1);
        Location spawnLocation = block.getLocation().add(0.5, 1.05, 0.5);
        // 创建一个itemDisplay
        ItemDisplay itemDisplay = block.getWorld().spawn(spawnLocation, ItemDisplay.class);
        itemDisplay.addScoreboardTag("altar.item");
        // 创建一个无敌的、不会消失的、无法拾起的掉落物Item
        Item droppedItem = block.getWorld().dropItem(spawnLocation, cloneItem);
        droppedItem.setGravity(false);
        droppedItem.setPickupDelay(Integer.MAX_VALUE);
        droppedItem.setInvulnerable(true);
        // 然后将这个Item骑在armorStand上
        itemDisplay.addPassenger(droppedItem);
        return true;
    }

    // 从Altar基座上移除物品 移除物品不需要考虑是否是祭坛
    public static void removeItem(Block block) {
        removeItem(block, true);
    }

    public static void removeItem(Block block, boolean shouldDrop) {
        if (block == null) return;
        List<ItemDisplay> displays = getEntityAbove(block);
        if (displays.isEmpty()) return;
        for (ItemDisplay display : displays) {
            ItemStack weapon = display.getItemStack();
            if (block.getType() == Material.LODESTONE && weapon != null) {
                display.remove();
                World world = display.getWorld();
                world.dropItemNaturally(display.getLocation(), weapon);
                world // 播放音效
                        .playSound(display.getLocation(),
                                Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
                break;
            }
            for (Entity passenger : display.getPassengers()) {
                if (passenger instanceof Item item) {
                    item.remove();
                    World world = item.getWorld();
                    if (shouldDrop) {
                        world.dropItemNaturally(item.getLocation(), item.getItemStack());
                        world // 播放音效
                                .playSound(item.getLocation(),
                                        Sound.ENTITY_ITEM_FRAME_REMOVE_ITEM, 1.0F, 1.0F);
                    } else {
                        world.spawnParticle(Particle.END_ROD, item.getLocation(), 3);
                    }
                }
            }
            display.remove();
        }
    }

    // 移除所有祭坛上的物品
    private void removeAllItem() {
        removeAllItem(true);
    }

    private void removeAllItem(boolean shouldDrop) {
        World world = this.lodeStone.getWorld();
        int x = lodeStone.getX(), y = lodeStone.getY(), z = lodeStone.getZ();
        for (int i = 0; i < EnableCustomAltar.offsets.length; i++) {
            Block currentBlock = world.getBlockAt(x + EnableCustomAltar.offsets[i][0], y, z + EnableCustomAltar.offsets[i][1]);
            removeItem(currentBlock, shouldDrop);
        }
        if (shouldDrop) {
            removeItem(this.lodeStone);
        }
    }

    // 末影轮盘
    private @NotNull List<ItemStack> getEndGear() {
        int[][] offsets = {{2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        return getGear(Material.END_STONE, offsets);
    }

    // 下届轮盘
    private @NotNull List<ItemStack> getNetherGear() {
        int[][] offsets = {{-4, 0}, {4, 0}, {0, -4}, {0, 4}, {3, 3}, {3, -3}, {-3, 3}, {-3, -3}};
        return getGear(Material.CRYING_OBSIDIAN, offsets);
    }

    // 潜声轮盘
    private @NotNull List<ItemStack> getSculkGear() {
        int[][] offsets = {{-6, 0}, {6, 0}, {0, -6}, {0, 6}, {3, 5}, {3, -5}, {-3, 5}, {-3, -5}, {5, 3}, {5, -3}, {-5, 3}, {-5, -3}};
        return getGear(Material.SCULK, offsets);
    }

    private @NotNull List<ItemStack> getGear(Material blockType, int[][] offsets) {
        ArrayList<ItemStack> result = new ArrayList<>(1);
        for (int[] offset : offsets) {
            Block block = lodeStone.getWorld().getBlockAt(lodeStone.getX() + offset[0], lodeStone.getY(), lodeStone.getZ() + offset[1]);
            if (block.getType() == blockType) {
                for (ItemDisplay display : getEntityAbove(block)) {
                    if (!display.getScoreboardTags().contains("altar.item")) {
                        continue;
                    }
                    for (Entity passenger : display.getPassengers()) {
                        if (passenger instanceof Item item) {
                            result.add(item.getItemStack());
                            break;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static List<ItemDisplay> getEntityAbove(Block block) {
        return block.getWorld()
                .getNearbyEntities(
                        block.getLocation().add(0, 0, 0), 1, 2, 1)
                .stream()
                .filter(entity -> entity instanceof ItemDisplay)
                .map(entity -> (ItemDisplay) entity)
                .filter(display -> display.getScoreboardTags().contains("altar.item"))
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

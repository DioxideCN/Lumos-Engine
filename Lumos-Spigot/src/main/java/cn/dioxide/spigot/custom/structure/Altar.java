package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.common.util.MatrixUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * @author Dioxide.CN
 * @date 2023/7/10
 * @since 1.0
 */
public class Altar {

    private @NotNull final Block coreBlock;
    private @Nullable Chest awaitChest;

    public float getLoyalty() {
        Chest chest = getRandomValidChest();
        if (chest == null) return -1F;
        awaitChest = chest;
        float totalValue = 0.0F;
        int totalCount = 0;
        for (ItemStack item : chest.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalValue += LoyaltySet.CHART.getOrDefault(item.getType(), 0.0F) * item.getAmount();
                totalCount += item.getAmount();
            }
        }
        return totalCount == 0 ? 0F : totalValue / totalCount;
    }

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
        Block nearestCore = block.getType() == Material.LODESTONE ? block : findNearestCenterCore(block);
        if (nearestCore == null) return null;
        World world = nearestCore.getWorld();
        int x = nearestCore.getX(), y = nearestCore.getY(), z = nearestCore.getZ();
        // 检查核心方块附近的方块
        for (EnableCustomAltar.CoreBlock offset : EnableCustomAltar.offsetBlocks) {
            // 检查是否符合周围方块的需求
            if (offset.getBlock(world, x, y, z) == null) return null;
        }
        return new Altar(nearestCore);
    }

    // 祭坛被破坏
    public void destroy() {
        removeAllItem();
    }

    /**
     * 寻找距离最近的核心方块
     */
    @Nullable
    private static Block findNearestCenterCore(Block block) {
        Material type = block.getType();
        World world = block.getWorld();
        int x = block.getX(), y = block.getY(), z = block.getZ();
        EnableCustomAltar.CoreBlock offset = EnableCustomAltar.CoreBlock.isOne(type);
        if (offset != null) {
            if (offset.isThis(type)) {
                // 底下必须放置矿物块
                if (offset.getBelow(block) == null) return null;
                Block possibleCore = world.getBlockAt(
                        x - offset.getOffsetX(), y - offset.getOffsetY(), z - offset.getOffsetZ());
                if (possibleCore.getType() == EnableCustomAltar.offsetBlocks[4].getBlockType()) {
                    // 是Core类型的方块
                    return possibleCore;
                }
            }
        }
        return null;
    }

    // 比对合成表
    public boolean match(ICustomSkillHandler recipe) {
        return matchItemStackList(this.getEndGear(), recipe.endStoneOne())
                && matchItemStackList(this.getNetherGear(), recipe.netherWartOne())
                && matchItemStackList(this.getSculkGear(), recipe.sculkCatalystOne())
                && matchItemStackList(this.getAmethystGear(), recipe.amethystOne());
    }

    private boolean matchItemStackList(List<ItemStack> gearItems, ItemStack targetItem) {
        if (targetItem == null) return gearItems.isEmpty(); // 该位置不需要物品
        if (gearItems.isEmpty()) return false;
        for (ItemStack gearItem : gearItems) {
            if (!isItemEqual(gearItem, targetItem)) {
                return false;
            }
        }
        return true;
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

    private Altar(Block block) {
        if (block.getType() != Material.LODESTONE) {
            throw new RuntimeException("Illegal lode stone");
        }
        this.coreBlock = block;
    }

    public Location getLoc() {
        return this.coreBlock.getLocation();
    }

    // 将武器放置到Altar的激活座上
    public boolean putWeapon(ItemStack item, Block block, double angle, float yaw) {
        if (block == null || item == null || item.getType() == Material.AIR) return false;
        if (!getEntityAbove(block).isEmpty()) {
            removeItem(block);
        }
        ItemStack cloneItem = item.clone();
        Location spawnLocation = block.getLocation().add(0.5, 1.3, 0.5);
        ItemDisplay weaponDisplay = block.getWorld().spawn(spawnLocation, ItemDisplay.class);
        Matrix4f matrix;
        if (item.getType() == Material.TRIDENT) {
            matrix = MatrixUtils.getMatrix(angle, yaw, 0, 1, 1, 1, -0.5, 0.7, -0.5);
        } else {
            matrix = MatrixUtils.getMatrix(0, yaw, angle, 1, 1, 1);
        }
        weaponDisplay.setTransformationMatrix(matrix);
        weaponDisplay.addScoreboardTag("altar.item");
        weaponDisplay.setItemStack(cloneItem);
        return true;
    }

    @Nullable
    public ItemStack getWeapon() {
        List<ItemDisplay> displays = getEntityAbove(this.coreBlock);
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
        removeAllItem(false);
        if (!removeItem(this.coreBlock, false)) return;
        if (awaitChest != null) {
            awaitChest.getInventory().clear();
        }
        // 在coreBlock位置y+1.2的地方生成一个item实体
        Location spawnLocation = this.coreBlock.getLocation().add(0.5, 1.2, 0.5);
        Item itemEntity = this.coreBlock.getWorld().spawn(spawnLocation, Item.class, item -> {
            item.setItemStack(weapon);
            item.setGlowing(true);
            item.setGravity(false);
            item.addScoreboardTag("stellarity.boss_drop");
            item.setVelocity(new Vector(0, 0, 0));
        });
        if (Bukkit.getScoreboardManager() == null) return;
        Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = mainBoard.getTeam("stellarity.purple_glow");
        if (team == null) return;
        team.addEntry(itemEntity.getUniqueId().toString());
    }

    // 将物品放置到Altar的基座上
    public boolean putItem(ItemStack item, Block block) {
        if (block == null || item == null || item.getType() == Material.AIR) return false;
        // 获取核心方块的位置
        EnableCustomAltar.CoreBlock offsetBlock = EnableCustomAltar.CoreBlock.isOne(block.getType());
        if (offsetBlock == null) return false;
        if (!getEntityAbove(block).isEmpty()) { // 如果已经放置了那就替换
            removeItem(block);
        }
        // 替换方块
        offsetBlock.doReplace(block);
        // 扣除物品
        ItemStack cloneItem = item.clone();
        cloneItem.setAmount(1);
        Location spawnLocation = block.getLocation().add(0.5, 0.1, 0.5);
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
    public static boolean removeItem(Block block) {
        return removeItem(block, true);
    }

    public static boolean removeItem(Block block, boolean shouldDrop) {
        boolean result = false;
        if (block == null) return result;
        List<ItemDisplay> displays = getEntityAbove(block);
        if (displays.isEmpty()) return result;
        EnableCustomAltar.CoreBlock offsetBlock = EnableCustomAltar.CoreBlock.isOne(block.getType());
        if (offsetBlock == null) return result;
        for (ItemDisplay display : displays) {
            ItemStack weapon = display.getItemStack();
            if (block.getType() == Material.LODESTONE && weapon != null) {
                display.remove();
                World world = display.getWorld();
                if (shouldDrop) {
                    world.dropItemNaturally(display.getLocation(), weapon);
                }
                world.playSound(display.getLocation(),
                        Sound.BLOCK_BEACON_DEACTIVATE, 1.0F, 1.0F);
                result = true;
                break;
            }
            for (Entity passenger : display.getPassengers()) {
                if (passenger instanceof Item item) {
                    item.remove();
                    result = true;
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
        offsetBlock.doRollback(block);
        return result;
    }

    // 移除所有祭坛上的物品
    private void removeAllItem() {
        removeAllItem(true);
    }

    /**
     * 移除所有物品
     * @param shouldDrop 是否掉落
     */
    private void removeAllItem(boolean shouldDrop) {
        World world = this.coreBlock.getWorld();
        int x = coreBlock.getX(), y = coreBlock.getY(), z = coreBlock.getZ();
        for (EnableCustomAltar.CoreBlock offset : EnableCustomAltar.offsetBlocks) {
            if (offset.getBlockType() == Material.LODESTONE) continue;
            Block block = offset.getBlock(world, x, y, z);
            removeItem(block, shouldDrop);
        }
        if (shouldDrop) {
            removeItem(this.coreBlock);
        }
    }

    // 末地石基座
    private @NotNull List<ItemStack> getEndGear() {
        return getGear(Material.YELLOW_STAINED_GLASS);
    }

    // 下界疣基座
    private @NotNull List<ItemStack> getNetherGear() {
        return getGear(Material.RED_STAINED_GLASS);
    }

    // 幽匿基座
    private @NotNull List<ItemStack> getSculkGear() {
        return getGear(Material.CYAN_STAINED_GLASS);
    }

    // 紫水晶基座
    private @NotNull List<ItemStack> getAmethystGear() {
        return getGear(Material.PURPLE_STAINED_GLASS);
    }

    private @NotNull List<ItemStack> getGear(Material blockType) {
        ArrayList<ItemStack> result = new ArrayList<>(1);
        World world = coreBlock.getWorld();
        int x = coreBlock.getX(), y = coreBlock.getY(), z = coreBlock.getZ();
        for (EnableCustomAltar.CoreBlock offset : EnableCustomAltar.offsetBlocks) {
            if (offset.isThis(blockType)) {
                Block block = offset.getBlock(world, x, y, z);
                if (block == null) return result;
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
        return "{world: " + this.coreBlock.getWorld() +
                ", x: " + this.coreBlock.getX() +
                ", y: " + this.coreBlock.getY() +
                ", z: " + this.coreBlock.getZ() + "}";
    }

    @Nullable
    public Chest getRandomValidChest() {
        ArrayList<Block> validChests = new ArrayList<>();
        // 定义四个检查的位置
        Block[] potentialChests = new Block[]{
                this.coreBlock.getRelative(2, 0, 0), // x+2
                this.coreBlock.getRelative(-2, 0, 0), // x-2
                this.coreBlock.getRelative(0, 0, 2), // z+2
                this.coreBlock.getRelative(0, 0, -2) // z-2
        };
        for (Block block : potentialChests) {
            // 检查方块是否为箱子
            if (block.getType() == Material.CHEST) {
                BlockState state = block.getState();
                if (state instanceof Chest chest) {
                    // 检查是否为单个小箱子且包含物品
                    if (!(chest.getInventory().getHolder() instanceof DoubleChest) &&
                            chest.getInventory().getSize() == 27 && !chest.getInventory().isEmpty()) {
                        validChests.add(block);
                    }
                }
            }
        }
        // 随机选择一个箱子
        if (!validChests.isEmpty()) {
            int randomIndex = new Random().nextInt(validChests.size());
            Block randomBlock = validChests.get(randomIndex);
            return (Chest) randomBlock.getState();
        }
        return null;
    }

}

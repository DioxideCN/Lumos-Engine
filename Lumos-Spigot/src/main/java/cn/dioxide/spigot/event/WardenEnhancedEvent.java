package cn.dioxide.spigot.event;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Config;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.block.data.type.SculkShrieker;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Warden;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author Dioxide.CN
 * @date 2023/6/25
 * @since 1.0
 */
@Event
public class WardenEnhancedEvent implements Listener {

    private final Random random = new Random();

    /**
     * 监守者在死亡后会概率性产生爆炸并在爆炸中概率性生成附魔书 P(AB)=23.4%
     */
    @EventHandler
    public void onWardenDeath(EntityDeathEvent event) {
        // 检查死亡的实体是否是监守者
        if (event.getEntityType() == EntityType.WARDEN) {
            Warden warden = (Warden) event.getEntity();
            Location location = warden.getLocation();
            replaceBlocks(location);
            // 的概率触发爆炸 P(A)=65%
            if (random.nextInt(100) < Config.get().feature.wardenExplodeChance * 100) {
                // 创建爆炸，参数分别是位置、爆炸的力度、是否产生火焰、是否破坏方块
                if (location.getWorld() == null) return;
                location.getWorld().createExplosion(location, (float) Config.get().feature.wardenExplodeStrength, true, true);
                // 的概率在爆炸中心掉落附魔书 P(B)=36%
                if (random.nextInt(100) < Config.get().feature.wardenDropChance * 100) {
                    ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                    EnchantmentStorageMeta meta = (EnchantmentStorageMeta) enchantedBook.getItemMeta();
                    assert meta != null;
                    meta.addStoredEnchant(Enchantment.MENDING, 1, true);
                    enchantedBook.setItemMeta(meta);
                    // 在爆炸的位置掉落附魔书
                    Item item = location.getWorld().dropItemNaturally(location, enchantedBook);
                    // 使掉落的物品不受爆炸的影响
                    item.setInvulnerable(true);
                }
            }
            generateSculkShrieker(location);
        }
    }

    /**
     * 监守者死亡时同时会向17x9x17范围内播撒幽匿尖啸体
     */
    private void generateSculkShrieker(Location location) {
        Location loc1 = location.clone().add(8, 4, 8);
        Location loc2 = location.clone().subtract(8, 4, 8);
        List<Block> blocks = getRegionBlocks(loc1, loc2);
        World world = location.getWorld();
        if (world == null) {
            return;
        }
        List<Block> eligibleBlocks = new ArrayList<>();
        for (Block block : blocks) {
            if (isValidPlacement(block)) {
                eligibleBlocks.add(block);
            }
        }
        Random random = new Random();
        int blockCount = random.nextInt(3) + 1; // 1-3
        for (int i = 0; i < blockCount; i++) {
            if (eligibleBlocks.isEmpty()) break;
            int randomIndex = random.nextInt(eligibleBlocks.size());
            Block randomBlock = eligibleBlocks.remove(randomIndex);
            Block generator = world.getBlockAt(randomBlock.getLocation().add(0, 0, 0));
            generator.setType(Material.SCULK_SHRIEKER);
            if (generator.getBlockData() instanceof SculkShrieker sculkShrieker) {
                sculkShrieker.setCanSummon(true);
                generator.setBlockData(sculkShrieker);
            }
            world.getBlockAt(randomBlock.getLocation().add(0, -1, 0)).setType(Material.SCULK);
        }
        for (int i = 0; i < 25; i++) {
            if (eligibleBlocks.isEmpty()) break;
            int randomIndex = random.nextInt(eligibleBlocks.size());
            Block randomBlock = eligibleBlocks.remove(randomIndex);
            world.getBlockAt(randomBlock.getLocation().add(0, 0, 0)).setType(Material.SCULK);
        }
    }

    /**
     * 监守者在生成时会破坏以他为中心11x13x11范围内的幽匿尖啸体
     */
    @EventHandler
    public void onWardenSpawn(CreatureSpawnEvent event) {
        // 检查生成的实体是否为监守者
        if (event.getEntityType() == EntityType.WARDEN) {
            // 获取监守者的位置
            Location wardenLocation = event.getLocation();
            // 获取僵尸周围的方块
            Location loc1 = wardenLocation.clone().add(5, 6, 5);
            Location loc2 = wardenLocation.clone().subtract(5, 6, 5);
            List<Block> blocks = getRegionBlocks(loc1, loc2);
            // 获取世界以播放粒子
            World world = wardenLocation.getWorld();
            if (world == null) {
                return;
            }
            // 遍历方块并替换
            for (Block block : blocks) {
                if (block.getType() == Material.SCULK_SHRIEKER) {
                    block.setType(Material.AIR);
                }
            }
        }
    }

    private void replaceBlocks(Location location) {
        Location loc1 = location.clone().add(5, 6, 5);
        Location loc2 = location.clone().subtract(5, 6, 5);
        List<Block> blocks = getRegionBlocks(loc1, loc2);

        // 遍历方块并替换黑曜石、水、岩浆、含水方块
        for (Block block : blocks) {
            if (block.getType() == Material.OBSIDIAN || block.getType() == Material.CRYING_OBSIDIAN) {
                block.setType(Material.SCULK);
                continue;
            }
            if (block.getType() == Material.WATER) {
                block.setType(Material.AIR);
                continue;
            }
            if (block.getBlockData() instanceof Waterlogged waterlogged) {
                // 检查方块是否为含水方块
                if (waterlogged.isWaterlogged()) {
                    // 设置含水状态为false并更新方块
                    waterlogged.setWaterlogged(false);
                    block.setBlockData(waterlogged);
                }
            }
        }
    }

    private List<Block> getRegionBlocks(Location loc1, Location loc2) {
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

    private boolean isValidPlacement(Block block) {
        // 获取方块上方和下方的方块
        Block blockAbove = block.getRelative(BlockFace.UP);
        Block blockBelow = block.getRelative(BlockFace.DOWN);
        // 检查上方是否是 AIR
        boolean isAirAbove = blockAbove.getType() == Material.AIR;
        // 检查下方方块是否不是半透明方块
        boolean isOpaqueBelow = !blockBelow.getType().isTransparent();
        // 返回结果
        return isAirAbove && isOpaqueBelow;
    }

}

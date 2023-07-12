package cn.dioxide.spigot.custom;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.util.ChunkUtils;
import cn.dioxide.spigot.custom.skill.DefaultSkillHelper;
import cn.dioxide.spigot.custom.structure.Altar;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Optional;

/**
 * 在不同的生物群系会产生不同的意志，意志会影响祭坛的加成效果
 * 寒霜意志：冰川
 * 炙热意志：沙漠
 * 繁茂意志：繁茂洞穴
 * 腐坏意志：蘑菇岛
 *
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Event
public class EnableCustomAltar implements Listener {

    // 定义偏移数组
    public static final int[][] offsets = { // {offsetX, offsetZ}
            {-2, 0}, {2, 0}, {0, -2}, {0, 2},
            {-4, 0}, {4, 0}, {0, -4}, {0, 4}, {3, 3}, {3, -3}, {-3, 3}, {-3, -3},
            {-6, 0}, {6, 0}, {0, -6}, {0, 6}, {3, 5}, {3, -5}, {-3, 5}, {-3, -5}, {5, 3}, {5, -3}, {-5, 3}, {-5, -3}
    };

    @EventHandler
    public void buildAltar(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        if (placedBlock.getType() != Material.LODESTONE) {
            return;
        }
        Block aboveBlock = placedBlock.getRelative(BlockFace.UP);
        if (aboveBlock.getType() != Material.CHEST) {
            return;
        }
        Chest chest = (Chest) aboveBlock.getState();
        Inventory inventory = chest.getInventory();
        int x = placedBlock.getX();
        int y = placedBlock.getY();
        int z = placedBlock.getZ();
        for (Block targetBlock : ChunkUtils.getRegionBlocks(placedBlock.getLocation(), 64, 64, 64)) {
            if (targetBlock.getType() == Material.LODESTONE && !targetBlock.equals(placedBlock)) {
                // 存在多个祭坛
                placedBlock.setType(Material.COBBLESTONE);
                placedBlock.getWorld().strikeLightning(placedBlock.getLocation()); // 召唤闪电
                aboveBlock.setType(Material.AIR); // 设置为AIR方块
                Location particleLocation = placedBlock.getLocation().add(0.5, 1.2, 0.5);
                placedBlock.getWorld().spawnParticle(Particle.SOUL_FIRE_FLAME,
                        particleLocation,
                        50, 0.5, 0.5, 0.5, 0.2);
                return;
            }
        }
        if (containsItems(inventory, Material.SCULK, 12) &&
                containsItems(inventory, Material.CRYING_OBSIDIAN, 8) &&
                containsItems(inventory, Material.END_STONE, 4)) {
            // 方块是磁石，上方是箱子，箱子内有8个哭泣黑曜石
            placedBlock.getWorld().strikeLightning(placedBlock.getLocation()); // 召唤闪电
            aboveBlock.setType(Material.AIR); // 设置为AIR方块
            // 放置末地石、哭泣的黑曜石、幽匿块
            for (int i = 0; i < offsets.length; i++) {
                placedBlock.getWorld()
                        .getBlockAt(x + offsets[i][0], y, z + offsets[i][1])
                        .setType(i <= 3 ? Material.END_STONE : i <= 11 ? Material.CRYING_OBSIDIAN : Material.SCULK);
                Location particleLocation = placedBlock.getLocation().add(0.5, 1.2, 0.5);
                placedBlock.getWorld().spawnParticle(Particle.FLAME,
                        particleLocation,
                        50, 0.5, 0.5, 0.5, 0.2);
            }
        }
    }

    @EventHandler
    public void placeItemToAltar(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
            ItemStack weapon = e.getItem();
            Block clicked = e.getClickedBlock();
            if (clicked == null) return;
            Material type = clicked.getType();
            if (type != Material.END_STONE && type != Material.SCULK &&
                type != Material.CRYING_OBSIDIAN && type != Material.LODESTONE) {
                return;
            }
            if (weapon == null || weapon.getType() == Material.AIR) {
                Altar.removeItem(clicked);
                e.getPlayer().swingMainHand();
                return;
            }
            if (weapon.getType().isBlock()) { // 如果 ItemStack 是方块
                return;
            }
            if (type == Material.END_STONE || type == Material.SCULK || type == Material.CRYING_OBSIDIAN) {
                // Altar是外周轮之一的方块
                Optional.ofNullable(Altar.with(clicked))
                        .ifPresent(altar -> {
                            if (altar.putItem(weapon, clicked)) {
                                e.getPlayer().swingMainHand(); // 执行主手交互动画
                                weapon.setAmount(weapon.getAmount() - 1); // 同时消耗物品
                                clicked.getWorld() // 播放音效
                                        .playSound(clicked.getLocation(),
                                                Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 1.0F);
                            }
                        });
            }
            // 武器类型置于磁石上
            if (type == Material.LODESTONE) {
                double angle;
                switch (weapon.getType()) {
                    case WOODEN_SWORD, STONE_SWORD, IRON_SWORD,
                            DIAMOND_SWORD, NETHERITE_SWORD, GOLDEN_SWORD -> angle = 135;
                    case BOW, CROSSBOW -> angle = 45;
                    case WOODEN_AXE, STONE_AXE, IRON_AXE,
                            DIAMOND_AXE, NETHERITE_AXE, GOLDEN_AXE -> angle = 180;
                    case TRIDENT -> angle = -180;
                    default -> angle = 0;
                }
                if (angle != 0) {
                    Optional.ofNullable(Altar.with(clicked))
                            .ifPresent(altar -> {
                                if (altar.putWeapon(weapon, clicked, angle)) {
                                    e.getPlayer().swingMainHand(); // 执行主手交互动画
                                    weapon.setAmount(weapon.getAmount() - 1); // 同时消耗物品
                                    clicked.getWorld() // 播放音效
                                            .playSound(clicked.getLocation(),
                                                    Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 1.0F);
                                    // 唤醒事件
                                    Bukkit.getServer().getPluginManager().callEvent(new AltarRecipeEvent(e.getPlayer(), weapon, altar));
                                }
                            });
                }
            }
        }
    }

    @EventHandler
    public void onAltarBreak(BlockBreakEvent event) {
        removeAltar(event.getBlock());
    }

    @EventHandler
    public void onPistonExtendAltarPart(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            removeAltar(block);
        }
    }

    @EventHandler
    public void onEntityChangeAltar(EntityChangeBlockEvent event) {
        removeAltar(event.getBlock());
    }

    @EventHandler
    public void onAltarPartExplode(BlockExplodeEvent event) {
        for (Block block : event.blockList()) {
            removeAltar(block);
        }
    }

    @EventHandler
    public void onBlockPistonRetractAltar(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            removeAltar(block);
        }
    }

    @EventHandler
    public void onEntityExplodeAltarPart(EntityExplodeEvent event) {
        for (Block block : event.blockList()) {
            removeAltar(block);
        }
    }

    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        // 判断是否为玩家攻击实体事件
        if (event.getDamager() instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() != Material.AIR) {
                ItemMeta meta = item.getItemMeta();
                if (meta == null) return;
                if (meta.hasLore()) {
                    List<String> lore = meta.getLore();
                    if (lore == null) return;
                    for (String s : lore) {
                        if (s.contains("§8[") && s.contains("§e") && s.contains("§7§o") && s.contains("§8]")) {
                            Bukkit.getServer().getPluginManager().callEvent(
                                    new TriggerSkillEvent(player, item, DefaultSkillHelper.getSkill(s)));
                            break;
                        }
                    }
                }
            }
        }
    }

    private void removeAltar(Block block) {
        if (block.getType() == Material.END_STONE ||
                block.getType() == Material.SCULK ||
                block.getType() == Material.CRYING_OBSIDIAN ||
                block.getType() == Material.LODESTONE) {
            Optional.ofNullable(Altar.with(block))
                    .ifPresent(Altar::destroy);
        }
    }

    private boolean containsItems(Inventory inventory, Material material, int amount) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }
}

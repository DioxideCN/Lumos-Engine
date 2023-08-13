package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.extension.Pair;
import cn.dioxide.spigot.custom.CustomRegister;
import lombok.Getter;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

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

    // 核心检测方块的重偏移与置换
    public static final CoreBlock[] offsetBlocks = {
            CoreBlock.of(2, 1, 2, Material.AMETHYST_BLOCK, Material.PURPLE_STAINED_GLASS),
            CoreBlock.of(2, 1, -2, Material.SCULK_CATALYST, Material.CYAN_STAINED_GLASS),
            CoreBlock.of(-2, 1, 2, Material.END_STONE, Material.YELLOW_STAINED_GLASS),
            CoreBlock.of(-2, 1, -2, Material.NETHER_WART_BLOCK, Material.RED_STAINED_GLASS),
            CoreBlock.center(Material.LODESTONE)
    };

    @EventHandler
    public void placeItemToAltar(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.getHand() == EquipmentSlot.HAND) {
            ItemStack item = e.getItem();
            Block clicked = e.getClickedBlock();
            if (clicked == null) return;
            Material type = clicked.getType();
            if (CoreBlock.isOne(type) == null) return;
            if (item == null || item.getType() == Material.AIR) {
                if (Altar.removeItem(clicked)) {
                    player.swingMainHand();
                }
                return;
            }
            if (item.getType().isBlock()) { // 如果 ItemStack 是方块
                return;
            }
            if (type != Material.LODESTONE) {
                // Altar是外周轮之一的方块
                Optional.ofNullable(Altar.with(clicked))
                        .ifPresent(altar -> {
                            if (altar.putItem(item, clicked)) {
                                player.swingMainHand(); // 执行主手交互动画
                                item.setAmount(item.getAmount() - 1); // 同时消耗物品
                                clicked.getWorld() // 播放音效
                                        .playSound(clicked.getLocation(),
                                                Sound.BLOCK_END_PORTAL_FRAME_FILL, 1.0F, 1.0F);
                            }
                        });
                e.setCancelled(true);
            } else {
                double angle = getAngle(item);
                if (angle != -114514) {
                    Optional.ofNullable(Altar.with(clicked))
                            .ifPresent(altar -> {
                                if (altar.putWeapon(item, clicked, angle, player.getLocation().getYaw())) {
                                    player.swingMainHand(); // 执行主手交互动画
                                    final Material material = item.getType();
                                    item.setAmount(item.getAmount() - 1); // 同时消耗物品
                                    clicked.getWorld() // 播放音效
                                            .playSound(clicked.getLocation(),
                                                    Sound.BLOCK_BEACON_ACTIVATE, 1.0F, 1.0F);
                                    // 计算玩家Factor
                                    Factor factor = Factor.getFromPlayer(player, altar);
                                    Pair<Float, ICustomSkillHandler> awaitOne = Pair.of(Float.MAX_VALUE, null);
                                    CustomRegister.get().forEach((k, p) -> {
                                        p.right().getFactor().forEach(fac -> {
                                            float distance = factor.distance(fac);
                                            if (distance >= 0 &&
                                                    distance <= 0.04F &&
                                                    distance < awaitOne.left() &&
                                                    p.right().satisfyCondition(material)) {
                                                awaitOne.use(distance, p.right());
                                            }
                                        });
                                    });
                                    if (awaitOne.right() != null) {
                                        awaitOne.right().attachRecipe(player, altar);
                                    } else {
                                        Format.use().player().notice(player, "&e[&a强力附魔&e] &7没有合适的强力附魔可以附着到您的武器上");
                                    }
                                }
                            });
                    e.setCancelled(true);
                }
            }
        }
    }

    private static double getAngle(ItemStack item) {
        double angle;
        switch (item.getType()) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD,
                    DIAMOND_SWORD, NETHERITE_SWORD, GOLDEN_SWORD -> angle = 135;
            case BOW, CROSSBOW -> angle = 45;
            case WOODEN_AXE, STONE_AXE, IRON_AXE,
                    DIAMOND_AXE, NETHERITE_AXE, GOLDEN_AXE -> angle = 180;
            case TRIDENT -> angle = -180;
            case SHIELD,
                    DIAMOND_HELMET, DIAMOND_CHESTPLATE, DIAMOND_LEGGINGS, DIAMOND_BOOTS,
                    GOLDEN_HELMET, GOLDEN_CHESTPLATE, GOLDEN_LEGGINGS, GOLDEN_BOOTS,
                    IRON_HELMET, IRON_CHESTPLATE, IRON_LEGGINGS, IRON_BOOTS,
                    LEATHER_HELMET, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_BOOTS,
                    CHAINMAIL_HELMET, CHAINMAIL_CHESTPLATE, CHAINMAIL_LEGGINGS, CHAINMAIL_BOOTS -> angle = 0;
            default -> angle = -114514;
        }
        return angle;
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
                    for (String singleLore : lore) {
                        if (singleLore.contains("§8[") && singleLore.contains("§e") && singleLore.contains("§7§o") && singleLore.contains("§8]")) {
                            Entity damagedEntity = event.getEntity();
                            if (damagedEntity instanceof LivingEntity livingEntity) {
                                Bukkit.getServer().getPluginManager().callEvent(
                                        new TriggerSkillAttackEvent(player,
                                                livingEntity,
                                                item,
                                                singleLore,
                                                DefaultSkillHelper.getSkill(singleLore),
                                                event.getDamage(),
                                                event));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 摧毁整个祭坛
     */
    private void removeAltar(Block block) {
        Material type = block.getType();
        // 如果动了下面的矿物块
        if (type == Material.GOLD_BLOCK || type == Material.IRON_BLOCK ||
                type == Material.DIAMOND_BLOCK || type == Material.EMERALD_BLOCK ||
                type == Material.NETHERITE_BLOCK || type == Material.REDSTONE_BLOCK ||
                type == Material.COAL_BLOCK || type == Material.LAPIS_BLOCK) {
            Block relative = block.getRelative(BlockFace.UP);
            Material topType = relative.getType();
            if (topType == Material.AMETHYST_BLOCK ||
                    topType == Material.SCULK_CATALYST ||
                    topType == Material.END_STONE ||
                    topType == Material.NETHER_WART_BLOCK) {
                doRemoveAltar(relative);
            }
        } else {
            doRemoveAltar(block);
        }
    }

    private void doRemoveAltar(Block block) {
        if (CoreBlock.isOne(block.getType()) != null) {
            Optional.ofNullable(Altar.with(block))
                    .ifPresent(Altar::destroy);
        }
    }

    @Getter
    public static class CoreBlock {
        final int offsetX, offsetY, offsetZ;
        final Material blockType;
        @Nullable final Material replaceType;

        /**
         * 构造一个偏移方块
         */
        public static CoreBlock of(int x, int y, int z, Material type, Material replace) {
            return new CoreBlock(x, y, z ,type, replace);
        }

        /**
         * 构造一个中心移方块
         */
        public static CoreBlock center(Material type) {
            return new CoreBlock(0, 0, 0 ,type, null);
        }

        private CoreBlock(int offsetX, int offsetY, int offsetZ, Material blockType, Material replaceType) {
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.offsetZ = offsetZ;
            this.blockType = blockType;
            this.replaceType = replaceType;
        }

        /**
         * 获取CoreBlock底下的方块，如果不是矿物块类型返回null
         */
        @Nullable
        public Block getBelow(World world, int x, int y, int z) {
            Block belowBlock = world.getBlockAt(x + offsetX, y + offsetY - 1, z + offsetZ);
            return switch (belowBlock.getType()) {
                case GOLD_BLOCK, IRON_BLOCK, DIAMOND_BLOCK,
                        EMERALD_BLOCK, NETHERITE_BLOCK, REDSTONE_BLOCK,
                        COAL_BLOCK, LAPIS_BLOCK -> belowBlock;
                default -> null;
            };
        }

        /**
         * 既然已经明确了基座方块的位置那就直接用这个方法获取基座下的方块
         */
        @Nullable
        public Block getBelow(Block b) {
            Block belowBlock = b.getWorld().getBlockAt(b.getX(), b.getY() - 1, b.getZ());
            return switch (belowBlock.getType()) {
                case GOLD_BLOCK, IRON_BLOCK, DIAMOND_BLOCK,
                        EMERALD_BLOCK, NETHERITE_BLOCK, REDSTONE_BLOCK,
                        COAL_BLOCK, LAPIS_BLOCK -> belowBlock;
                default -> null;
            };
        }

        /**
         * 获取CoreBlock位置上的方块，如果类型不符合返回null
         */
        @Nullable
        public Block getBlock(World world, int x, int y, int z) {
            Block belowBlock = world.getBlockAt(x + offsetX, y + offsetY, z + offsetZ);
            if (!isThis(belowBlock.getType())) return null;
            return belowBlock;
        }

        /**
         * 将这个位置的方块进行替换
         */
        public void doReplace(Block b) {
            if (this.getReplaceType() == null) return;
            b.setType(this.getReplaceType());
            switch (this.getReplaceType()) {
                case RED_STAINED_GLASS -> b.getWorld().spawnParticle(
                        Particle.FLAME, b.getLocation().add(0.5, 0.5, 0.5), 10, 0.125, 0.125, 0.125, 0.03);
                case CYAN_STAINED_GLASS -> b.getWorld().spawnParticle(
                        Particle.SOUL, b.getLocation().add(0.5, 0.5, 0.5), 6, 0.125, 0.125, 0.125, 0.03);
                case YELLOW_STAINED_GLASS -> b.getWorld().spawnParticle(
                        Particle.END_ROD, b.getLocation().add(0.5, 0.5, 0.5), 10, 0.125, 0.125, 0.125, 0.03);
                case PURPLE_STAINED_GLASS -> b.getWorld().spawnParticle(
                        Particle.GLOW, b.getLocation().add(0.5, 0.5, 0.5), 10, 0.05, 0.05, 0.05, 0.03);
            }
        }

        /**
         * 将这个位置的方块进行还原
         */
        public void doRollback(Block block) {
            if (block.getType() != replaceType) return;
            block.setType(this.getBlockType());
        }

        public boolean isThis(Material type) {
            return type == blockType || (replaceType != null && type == replaceType);
        }

        @Nullable
        public static CoreBlock isOne(Material type) {
            for (CoreBlock offset : offsetBlocks) {
                if (offset.isThis(type)) return offset;
            }
            return null;
        }

    }

}

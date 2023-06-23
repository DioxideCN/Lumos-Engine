package cn.dioxide.spigot.event;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Pair;
import cn.dioxide.common.infra.EventType;
import cn.dioxide.common.infra.TrimUpgradeStore;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.LumosStarter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.potion.PotionEffect;

import java.util.*;

/**
 * @author Dioxide.CN
 * @date 2023/6/23
 * @since 1.0
 */
@Event
public class TrimUpgradeEvent implements Listener {

    private static final Random random = new Random();
    // 存储玩家的UUID和他们死亡时的位置
    private final Map<UUID, Pair<Location, Double>> deathLocations = new HashMap<>();
    // 玩家激怒猪灵的行为
    private final Set<UUID> piglinAggroPlayers = new HashSet<>();

    @EventHandler
    public void onEventBuff(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        TrimUpgradeStore.Buff buff = PlayerUtils.whichEventBuff(player);
        if (buff == null || buff.effect == null) {
            return;
        }
        if (random.nextDouble() < buff.chance) {
            Entity attackerHolder = event.getDamager();
            if (attackerHolder instanceof LivingEntity attacker) {
                if (attacker.getType() == EntityType.ENDER_DRAGON ||
                        attacker.getType() == EntityType.WITHER ||
                        attacker.getType() == EntityType.WARDEN) {
                    return;
                }
                attacker.addPotionEffect(
                        new PotionEffect(buff.effect,
                                buff.effectDuration * 20,
                                buff.effectLevel - 1,
                                true,
                                false,
                                false)
                );
            }
        }
    }

    /**
     * 玩家剥削村民能力
     */
    @EventHandler
    public void onTrade(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (event.getClickedInventory() instanceof MerchantInventory) {
            // 检查交易完成
            if (event.getCurrentItem() != null && event.isShiftClick() && event.isLeftClick()) {
                Pair<Boolean, Double> result = PlayerUtils.isSatisfyEvent(player, EventType.EXPLOIT_VILLAGERS);
                if (Boolean.TRUE.equals(result.left())) {
                    System.out.println("概率 " + result.right());
                    // 配置的概率掉落物品
                    if (random.nextDouble() < result.right()) {
                        // 随机选择铁粒或金粒
                        Material dropMaterial = random.nextBoolean() ?
                                Material.IRON_NUGGET :
                                Material.GOLD_NUGGET;
                        // 随机掉落数量 (1-5)
                        int dropAmount = random.nextInt(5);
                        // 掉落物品
                        ItemStack dropItem = new ItemStack(dropMaterial, dropAmount + 1);
                        player.getWorld().dropItemNaturally(player.getLocation(), dropItem);
                    }
                }
            }
        }
    }

    /**
     * 玩家可以直视末影人能力
     */
    @EventHandler
    public void onEndermanAggro(EntityTargetEvent event) {
        // 检查实体是否为末影人
        if (event.getEntity() instanceof Enderman) {
            // 检查目标是否为玩家
            if (event.getTarget() instanceof Player player) {
                // 检查是否为末影人激怒的原因
                if (event.getReason() == EntityTargetEvent.TargetReason.CLOSEST_PLAYER) {
                    Pair<Boolean, Double> result = PlayerUtils.isSatisfyEvent(player, EventType.DOCILE_ENDERMAN);
                    if (Boolean.TRUE.equals(result.left())) {
                        // 取消末影人的激怒
                        event.setCancelled(true);
                    }
                }
            }
        }
    }

    /**
     * 玩家与猪灵互动能力
     */
    @EventHandler
    public void onPiglinAggro(EntityTargetEvent event) {
        // 检查实体是否为Piglin
        if (event.getEntityType() == EntityType.PIGLIN) {
            // 检查目标是否为玩家
            if (event.getTarget() instanceof Player player) {
                // 检查TrimUpgrade
                Pair<Boolean, Double> result = PlayerUtils.isSatisfyEvent(player, EventType.NEUTRAL_PIGLIN);
                // 激怒猪灵的行为分为动了它们的财产、攻击了他们、没有穿戴金装备
                if (Boolean.TRUE.equals(result.left()) &&
                        !piglinAggroPlayers.contains(player.getUniqueId())) {
                    // 取消猪灵的攻击
                    event.setCancelled(true);
                }
            }
        }
    }

    /**
     * 玩家攻击猪灵会产生仇恨，绑定上面的事件
     */
    @EventHandler
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player && (event.getEntity() instanceof Piglin || event.getEntity() instanceof PiglinBrute)) {
            piglinAggroPlayers.add(player.getUniqueId());
            Bukkit.getScheduler().runTaskLater(LumosStarter.INSTANCE,
                    () -> piglinAggroPlayers.remove(player.getUniqueId()),
                    600L); // 30秒后清除状态
        } // 30秒后猪灵的仇恨表完全交由游戏本身处理
    }

    /**
     * 玩家原地复活能力
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Pair<Boolean, Double> result = PlayerUtils.isSatisfyEvent(player, EventType.RESPAWN_HERE);
        if (Boolean.TRUE.equals(result.left())) {
            // 记录玩家死亡时的位置
            deathLocations.put(player.getUniqueId(), Pair.of(player.getLocation(), result.right()));
        }
    }

    /**
     * 玩家原地复活能力，与上面的事件相互绑定
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();
        // 检查是否记录了玩家的死亡位置
        if (deathLocations.containsKey(playerUUID)) {
            // 生成一个0到1之间的随机数
            double chance = random.nextDouble();
            // 如果随机数小于或等于设定的概率，则让玩家原地复活
            if (chance <= deathLocations.get(playerUUID).right()) {
                Location deathLocation = deathLocations.get(playerUUID).left();
                event.setRespawnLocation(deathLocation);
            }
            // 删除记录，以免不必要的内存占用
            deathLocations.remove(playerUUID);
        }
    }

}

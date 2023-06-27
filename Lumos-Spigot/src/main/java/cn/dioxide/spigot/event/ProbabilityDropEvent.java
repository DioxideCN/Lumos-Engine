package cn.dioxide.spigot.event;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.spigot.feature.HorrifyWardenFeature;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Dioxide.CN
 * @date 2023/6/26
 * @since 1.0
 */
@Event
public class ProbabilityDropEvent implements Listener {

    private final Map<UUID, Double> damageMap = new HashMap<>();
    private static final double THRESHOLD = 0.1; // a
    private static final double THETA = 1.847; // θ

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity damagedEntity = event.getEntity();
        if (damagedEntity instanceof LivingEntity) {
            Player player = null;
            // 判断伤害源是否为玩家或玩家发射的投射物
            if (damager instanceof Player) {
                player = (Player) damager;
            } else if (damager instanceof Projectile projectile) {
                ProjectileSource shooter = projectile.getShooter();

                if (shooter instanceof Player) {
                    player = (Player) shooter;
                }
            }
            // 如果伤害源是玩家或来自玩家的投射物，记录伤害
            if (player != null) {
                UUID entityId = damagedEntity.getUniqueId();
                double damage = event.getDamage();
                damageMap.put(entityId, damageMap.getOrDefault(entityId, 0.0) + damage);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        // 不处理盔甲架、物品展示框、船、箱船、矿车、挂画
        if (event.getEntityType() == EntityType.ARMOR_STAND ||
                event.getEntityType() == EntityType.ITEM_FRAME ||
                event.getEntityType() == EntityType.GLOW_ITEM_FRAME ||
                event.getEntityType() == EntityType.BOAT ||
                event.getEntityType() == EntityType.CHEST_BOAT ||
                event.getEntityType() == EntityType.MINECART ||
                event.getEntityType() == EntityType.MINECART_CHEST ||
                event.getEntityType() == EntityType.MINECART_COMMAND ||
                event.getEntityType() == EntityType.MINECART_FURNACE ||
                event.getEntityType() == EntityType.MINECART_HOPPER ||
                event.getEntityType() == EntityType.MINECART_TNT ||
                event.getEntityType() == EntityType.MINECART_MOB_SPAWNER ||
                event.getEntityType() == EntityType.PAINTING ||
                event.getEntityType() == EntityType.PLAYER) {
            return;
        }
        UUID entityId = entity.getUniqueId();
        if (damageMap.containsKey(entityId)) {
            double totalDamage = damageMap.get(entityId);
            AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (maxHealthAttr != null) {
                double maxHealth = maxHealthAttr.getValue();
                double damageRatio = totalDamage / maxHealth;
                damageMap.remove(entityId); // remove this entity from hashmap
                if (!shouldDropItem(damageRatio)) {
                    // remove all drops
                    event.getDrops().clear();
                    return;
                }
                // handle other drops
                HorrifyWardenFeature.onWardenDeath(event);
            }
        } else {
            // clear if no player damage
            event.getDrops().clear();
        }
    }

    private boolean shouldDropItem(double damageRatio) {
        if (damageRatio < THRESHOLD) {
            return false;
        }
        double probability;
        if (damageRatio > 0.1 && damageRatio <= 0.5) {
            // 2x^2
            probability = 4 * Math.pow(damageRatio, 2);
        } else if (damageRatio > 0.5 && damageRatio <= 1) {
            // -(1/x+1.5)+1
            probability = -1 / (damageRatio + 1.5) + 1;
        } else {
            return true;
        }
        return Math.random() < probability - 0.2;
    }

}

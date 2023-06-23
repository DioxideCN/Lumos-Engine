package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
@Event(configKey = "feature.summon-iron-golem")
public class GolemSummonFeature implements Listener {
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // 检查生成的实体是否为铁傀儡
        if (event.getEntityType() == EntityType.IRON_GOLEM) {
            // 检查铁傀儡是否由于村庄机制生成
            if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.VILLAGE_DEFENSE
                    || event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.VILLAGE_INVASION) {
                // 如果是，取消这个事件，从而阻止铁傀儡的生成
                event.setCancelled(true);
            }
        }
    }
}

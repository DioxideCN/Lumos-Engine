package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
@Event(configKey = "feature.prevented-enderman")
public class PreventedEnderman implements Listener {
    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // 检查事件的实体是否为末影人
        if (event.getEntityType() == EntityType.ENDERMAN) {
            // 如果是，取消这个事件，从而阻止末影人拿起方块
            event.setCancelled(true);
        }
    }
}

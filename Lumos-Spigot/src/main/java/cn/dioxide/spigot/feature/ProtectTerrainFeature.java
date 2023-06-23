package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/**
 * @author Dioxide.CN
 * @date 2023/6/8
 * @since 1.0
 */
@Event(configKey = "feature.protect-terrain")
public class ProtectTerrainFeature implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        switch (event.getEntityType()) {
            // 取消爆炸破坏地形，但保留爆炸造成的伤害
            case CREEPER, GHAST -> event.blockList().clear();
            default -> {}
        }
    }
}

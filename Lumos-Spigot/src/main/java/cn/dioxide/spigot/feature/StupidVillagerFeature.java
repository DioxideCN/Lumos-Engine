package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

/**
 * @author Dioxide.CN
 * @date 2023/6/8
 * @since 1.0
 */
@Event(configKey = "feature.stupid-villager")
public class StupidVillagerFeature implements Listener {

    @EventHandler
    public void onVillagerReplenishTrade(VillagerReplenishTradeEvent event) {
        // 取消村民的交易刷新事件
        event.setCancelled(true);
    }

    @EventHandler
    public void onEntityBreed(EntityBreedEvent event) {
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            // 如果繁殖的实体是村民，则取消繁殖事件
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPortal(EntityPortalEvent event) {
        if (event.getEntity().getType() == EntityType.VILLAGER) {
            // 如果进入传送门的实体是村民，则取消事件
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onVillagerCareerChange(VillagerCareerChangeEvent event) {
        Villager villager = event.getEntity();
        // 检查村民是否已经有职业
        if (villager.getProfession() != Villager.Profession.NONE) {
            // 如果村民已经有职业，取消职业变化事件
            event.setCancelled(true);
        }
        // 如果村民还没有职业，允许事件继续，不执行任何操作
    }

    @EventHandler
    public void onEntityTransform(EntityTransformEvent event) {
        // 检查转换类型
        if (event.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
            // 检查实体类型
            if (event.getEntityType() == EntityType.ZOMBIE_VILLAGER &&
                    event.getTransformedEntity() instanceof Villager villager) {
                // 将村民设置为没有职业的村民
                villager.setProfession(Villager.Profession.NITWIT);
            }
        }
    }

}

package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * @author Dioxide.CN
 * @date 2023/6/19
 * @since 1.0
 */
@Event(configKey = "feature.iron-golem")
public class GolemLootTableFeature implements Listener {
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.IRON_GOLEM) {
            // 如果死亡的实体是铁傀儡，则清除掉落的铁锭
            event.getDrops().removeIf(item -> item.getType() == Material.IRON_INGOT);
            // Create a Random object
            Random rand = new Random();
            // Get a random number from 0 to 3
            int nuggetsToDrop = rand.nextInt(4);
            event.getDrops().add(new ItemStack(Material.IRON_NUGGET, nuggetsToDrop));
        }
    }
}

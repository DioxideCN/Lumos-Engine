package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityPortalEvent;

/**
 * @author Dioxide.CN
 * @date 2023/6/27
 * @since 1.0
 */
@Event
public class ForbidEndGateFeature implements Listener {

    @EventHandler
    public void onEntityPortalEnter(EntityPortalEvent event) {
        // 检查实体类型，如果不是玩家，则取消传送
        if (event.getEntity() instanceof Player) {
            return;
        }
        // 检查是否是地狱门
        if (event.getEntity().getLocation().getBlock().getType() == Material.NETHER_PORTAL) {
            return;
        }
        // 放行刷沙机
        // 检查是否是FallingBlock且是沙子或砂砾
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            Material material = fallingBlock.getBlockData().getMaterial();
            if (material == Material.SAND ||
                    material == Material.GRAVEL ||
                    material == Material.WHITE_CONCRETE_POWDER ||
                    material == Material.ORANGE_CONCRETE_POWDER ||
                    material == Material.MAGENTA_CONCRETE_POWDER ||
                    material == Material.LIGHT_BLUE_CONCRETE_POWDER ||
                    material == Material.YELLOW_CONCRETE_POWDER ||
                    material == Material.LIME_CONCRETE_POWDER ||
                    material == Material.PINK_CONCRETE_POWDER ||
                    material == Material.GRAY_CONCRETE_POWDER ||
                    material == Material.LIGHT_GRAY_CONCRETE_POWDER ||
                    material == Material.CYAN_CONCRETE_POWDER ||
                    material == Material.PURPLE_CONCRETE_POWDER ||
                    material == Material.BLUE_CONCRETE_POWDER ||
                    material == Material.BROWN_CONCRETE_POWDER ||
                    material == Material.GREEN_CONCRETE_POWDER ||
                    material == Material.RED_CONCRETE_POWDER ||
                    material == Material.BLACK_CONCRETE_POWDER
            ) {
                return;
            }
        }
        // 阻止非玩家实体通过末地门
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        // Check if the entity is a Wither
        if (event.getEntityType() == EntityType.WITHER) {
            World world = event.getLocation().getWorld();
            double y = event.getLocation().getY();

            if (world != null) {
                // Check if the world is The End or Overworld
                if (world.getEnvironment() == World.Environment.THE_END || world.getEnvironment() == World.Environment.NORMAL) {
                    // Cancel the spawning
                    event.setCancelled(true);
                }

                // Check if the world is Nether and within restricted height range
//                if (world.getEnvironment() == World.Environment.NETHER && ((y >= -1 && y <= 32) || (y >= 164 && y <= 200))) {
//                    // Cancel the spawning
//                    event.setCancelled(true);
//                }
            }
        }
    }

}

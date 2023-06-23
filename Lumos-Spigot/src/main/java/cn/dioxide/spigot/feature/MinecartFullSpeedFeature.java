package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.Material;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

/**
 * @author Dioxide.CN
 * @date 2023/6/20
 * @since 1.0
 */
@Event(configKey = "feature.minecart-full-speed")
public class MinecartFullSpeedFeature implements Listener {
    @EventHandler
    public void onMinecartMove(VehicleMoveEvent event) {
        // 检查移动的实体是否是矿车
        if (event.getVehicle().getType() != EntityType.MINECART) {
            return;
        }
        Minecart minecart = (Minecart) event.getVehicle();

        if (minecart.getPassengers().size() != 1) {
            return;
        }
        // 这里循环只会循环一次
        for (Entity passenger : minecart.getPassengers()) {
            // 检查矿车内是否有玩家
            if (!(passenger instanceof Player)) {
                return;
            }
        }
        // 检查矿车是否在动力铁轨上
        if (minecart.getLocation().getBlock().getType() != Material.POWERED_RAIL) {
            return;
        }
        // 检查动力铁轨是否被激活
        Powerable powerable = (Powerable) minecart.getLocation().getBlock().getBlockData();
        if (!powerable.isPowered()) {
            return;
        }
        // 满速行驶
        minecart.setVelocity(minecart.getVelocity().multiply(5));
    }
}

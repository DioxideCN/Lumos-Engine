package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.infra.RailType;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Powerable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.material.Rails;
import org.bukkit.util.Vector;

/**
 * @author Dioxide.CN
 * @date 2023/6/20
 * @since 1.0
 */
@Event(configKey = "feature.minecart.enable")
public class MinecartFullSpeedFeature implements Listener {
    private final static int BUFFER_LENGTH = 5;
    private final static int ADJUST_LENGTH = 20;
    private final static double NORMAL_SPEED = 0.4;
    private final double maxSpeed = Config.get().feature.minecartSpeedMultiple * NORMAL_SPEED;

    @EventHandler
    @SuppressWarnings("all")
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
        Block curBlock = minecart.getLocation().getBlock();
        // 检查矿车是否在动力铁轨上
        if (curBlock.getType() != Material.POWERED_RAIL) {
            return;
        }
        // 检查动力铁轨是否被激活
        Powerable powerable = (Powerable) curBlock.getBlockData();
        if (!powerable.isPowered()) {
            return;
        }

        Rails curRail = (Rails) curBlock.getState().getData();
        RailType curRailType = RailType.get(curRail);
        if (curRailType != RailType.X_FLAT && curRailType != RailType.Z_FLAT) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        Vector vector = event.getVehicle().getVelocity();
        if (vector.getY() != 0) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }
        double x = vector.getX();
        double z = vector.getZ();

        if (x == 0 && z == 0) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        boolean isX = x != 0 && z == 0;
        boolean n = isX ? x < 0 : z < 0;
        BlockFace direction = isX ? (n ? BlockFace.WEST : BlockFace.EAST) : (n ? BlockFace.NORTH : BlockFace.SOUTH);

        int flatLength = 0;
        while ((curBlock = nextRail(direction, curBlock)) != null && flatLength < BUFFER_LENGTH + ADJUST_LENGTH) {
            RailType railType = RailType.get((Rails) curBlock.getState().getData());
            if (isX) {
                if (railType != RailType.X_FLAT && railType != RailType.X_SLOPE) break;
            } else {
                if (railType != RailType.Z_FLAT && railType != RailType.Z_SLOPE) break;
            }

            flatLength++;
        }

        if (flatLength < BUFFER_LENGTH) {
            minecart.setMaxSpeed(NORMAL_SPEED);
            return;
        }

        int freeLength = flatLength - BUFFER_LENGTH;

        double s = (double) freeLength / ADJUST_LENGTH;
        if (s > 1) s = 1;
        double speed = NORMAL_SPEED + (maxSpeed - NORMAL_SPEED) * s;
        minecart.setMaxSpeed(speed);
    }

    private static Block nextRail(BlockFace direction, Block block) {
        Block b = block.getRelative(direction);
        return isRail(b) ? b : null;
    }

    private static boolean isRail(Block block) {
        Material mat = block.getType();
        return mat == Material.RAIL || mat == Material.ACTIVATOR_RAIL || mat == Material.DETECTOR_RAIL || mat == Material.POWERED_RAIL;
    }
}

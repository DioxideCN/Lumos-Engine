package cn.dioxide.common.infra;

import org.bukkit.block.BlockFace;
import org.bukkit.material.Rails;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
public enum RailType {
    Z_FLAT, X_FLAT, Z_SLOPE, X_SLOPE, X_Z_CURVE, X_NZ_CURVE, NX_Z_CURVE, NX_NZ_CURVE;

    public static RailType get(Rails rail) {
        if (rail.getDirection() == BlockFace.SOUTH) {
            return rail.isOnSlope() ? Z_SLOPE : Z_FLAT;
        } else if (rail.getDirection() == BlockFace.EAST) {
            return rail.isOnSlope() ? X_SLOPE : X_FLAT;
        }
        return switch (rail.getDirection()) {
            case NORTH_EAST -> X_Z_CURVE;
            case NORTH_WEST -> X_NZ_CURVE;
            case SOUTH_EAST -> NX_Z_CURVE;
            case SOUTH_WEST -> NX_NZ_CURVE;
            default -> null;
        };
    }
}

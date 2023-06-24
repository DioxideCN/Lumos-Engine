package cn.dioxide.web.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
public class StaticPlayer {

    /**
     * 是否在线（不属于数据库字段）
     */
    private boolean isOnline;

    /**
     * 玩家名
     */
    private String name;

    /**
     * UUID
     */
    private String uuid;

    /**
     * 等级
     */
    private Integer level;

    /**
     * 下线时的世界
     */
    private String world;

    /**
     * pos x
     */
    private Double x;

    /**
     * pos y
     */
    private Double y;

    /**
     * pos z
     */
    private Double z;

    public static StaticPlayer convert(Player player, boolean isOnline) {
        Location location = player.getLocation();
        return new StaticPlayer(
                isOnline,
                player.getName(),
                player.getUniqueId().toString(),
                player.getLevel(),
                location.getWorld() == null ? "overworld" : location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ());
    }

    public StaticPlayer() {
    }

    public StaticPlayer(boolean isOnline, String name, String uuid, Integer level, String world, Double x, Double y, Double z) {
        this.isOnline = isOnline;
        this.name = name;
        this.uuid = uuid;
        this.level = level;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getWorld() {
        return world;
    }

    public void setWorld(String world) {
        this.world = world;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getZ() {
        return z;
    }

    public void setZ(Double z) {
        this.z = z;
    }
}

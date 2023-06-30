package cn.dioxide.web.entity;

import cn.dioxide.web.config.ItemStackSerializer;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
@Getter
@Setter
@NoArgsConstructor
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

    /**
     * qq
     */
    private @Nullable String qq;

    /**
     * 背包
     */
    @JSONField(serialize = false)
    private @Nullable String inv;

    private @Nullable List<ItemStackSerializer> inventory;

    /**
     * 装备
     */
    @JSONField(serialize = false)
    private @Nullable String equip;

    private @Nullable List<ItemStackSerializer> equipments;

    public static StaticPlayer convert(Player player, boolean isOnline) {
        Location location = player.getLocation();
        // Get the player's inventory data
        List<ItemStackSerializer> inventory = ItemStackSerializer.convert(player.getInventory().getContents());
        List<ItemStackSerializer> equipment = ItemStackSerializer.convert(player.getInventory().getArmorContents());

        // Serialize the inventory data
        String inventoryJson = JSON.toJSONString(inventory);
        String equipmentJson = JSON.toJSONString(equipment);

        return new StaticPlayer(
                isOnline,
                player.getName(),
                player.getUniqueId().toString(),
                player.getLevel(),
                location.getWorld() == null ? "overworld" : location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                inventoryJson,
                equipmentJson);
    }

    private StaticPlayer(boolean isOnline,
                         String name,
                         String uuid,
                         Integer level,
                         String world,
                         Double x,
                         Double y,
                         Double z,
                         @Nullable String inv,
                         @Nullable String equip) {
        this.isOnline = isOnline;
        this.name = name;
        this.uuid = uuid;
        this.level = level;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.inv = inv;
        this.equip = equip;

        if (this.inv != null) {
            this.inventory = JSON.parseArray(this.inv, ItemStackSerializer.class);
        }
        if (this.equip != null) {
            this.equipments = JSON.parseArray(this.equip, ItemStackSerializer.class);
        }
    }

    public void setInv(@Nullable String inv) {
        this.inv = inv;
        if (this.inv != null) {
            this.inventory = JSON.parseArray(this.inv, ItemStackSerializer.class);
        }
    }

    public void setEquip(@Nullable String equip) {
        this.equip = equip;
        if (this.equip != null) {
            this.equipments = JSON.parseArray(this.equip, ItemStackSerializer.class);
        }
    }
}

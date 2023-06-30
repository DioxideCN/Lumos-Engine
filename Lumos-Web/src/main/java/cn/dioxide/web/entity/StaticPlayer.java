package cn.dioxide.web.entity;

import cn.dioxide.common.extension.Pair;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
@Getter
@Setter
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
     * 所在的世界
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
    private @NotNull List<CompoundTag> inventory;
    private @NotNull String inv;

    /**
     * 装备
     */
    private @NotNull List<CompoundTag> equipment;
    private @NotNull String equip;

    public static StaticPlayer convert(Player player, boolean isOnline) {
        Location location = player.getLocation();
        // Get the player's inventory data
        List<CompoundTag> inventory = Arrays
                .stream(player.getInventory().getContents())
                .map(StaticPlayer::getItemNBTAsJson)
                .toList();
        List<CompoundTag> equipment = Arrays
                .stream(player.getInventory().getArmorContents())
                .map(StaticPlayer::getItemNBTAsJson)
                .toList();
        return new StaticPlayer(
                isOnline,
                player.getName(),
                player.getUniqueId().toString(),
                player.getLevel(),
                location.getWorld() == null ? "overworld" : location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                inventory,
                equipment);
    }

    private StaticPlayer(boolean isOnline,
                         String name,
                         String uuid,
                         Integer level,
                         String world,
                         Double x,
                         Double y,
                         Double z,
                         @NotNull List<CompoundTag> inventory,
                         @NotNull List<CompoundTag> equipment) {
        this.isOnline = isOnline;
        this.name = name;
        this.uuid = uuid;
        this.level = level;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.inventory = inventory;
        this.equipment = equipment;

        ObjectMapper objectMapper = new ObjectMapper();
        Pair<List<String>, List<String>> iePair = compoundTagToJSON(inventory, equipment);
        try {
            this.inv =objectMapper.writeValueAsString(iePair.left());
            this.equip =objectMapper.writeValueAsString(iePair.right());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static CompoundTag getItemNBTAsJson(ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(itemStack);
        return nmsCopy.save(new CompoundTag());
    }

    public String toJSONString() {
        ObjectMapper objectMapper = new ObjectMapper();
        Pair<List<String>, List<String>> iePair = compoundTagToJSON(inventory, equipment);
        // 创建一个包含所有字段的 map
        Map<String, Object> map = new HashMap<>();
        map.put("isOnline", isOnline);
        map.put("name", name);
        map.put("uuid", uuid);
        map.put("level", level);
        map.put("world", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("qq", qq);
        map.put("inventory", iePair.left());
        map.put("equipment", iePair.right());
        // 将 map 序列化为 JSON 字符串
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Pair<List<String>, List<String>> compoundTagToJSON(
            List<CompoundTag> inventory,
            List<CompoundTag> equipment) {
        // 将 NBT tags 转换为它们的字符串表示
        List<String> inventoryStrings;
        inventoryStrings = new ArrayList<>();
        for (CompoundTag tag : inventory) {
            inventoryStrings.add(tag.toString());
        }
        List<String> equipmentStrings;
        equipmentStrings = new ArrayList<>();
        for (CompoundTag tag : equipment) {
            equipmentStrings.add(tag.toString());
        }
        return Pair.of(inventoryStrings, equipmentStrings);
    }
}

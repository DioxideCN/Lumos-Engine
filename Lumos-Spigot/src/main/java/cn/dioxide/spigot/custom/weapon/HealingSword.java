package cn.dioxide.spigot.custom.weapon;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.spigot.custom.CustomItem;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

/**
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Event
@Custom("healing_sword")
public class HealingSword implements Listener {

    // 治愈半径 治愈概率 治愈量
    private static final ItemStack item = CustomItem.with(Material.IRON_SWORD)
            .lore("&8[ &e治愈光环 &7&o+(random 3-8) +(random 15-85)% +(random 8-20)% &8]")
            .build();

}

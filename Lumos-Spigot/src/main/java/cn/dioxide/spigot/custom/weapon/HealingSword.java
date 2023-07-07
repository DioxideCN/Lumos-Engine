package cn.dioxide.spigot.custom.weapon;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.spigot.custom.CustomItem;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;

/**
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Event
@Custom("healing_sword")
public class HealingSword implements Listener {

    // 治愈半径 治愈概率 治愈量
    private static final CustomItem item = CustomItem.with(Material.IRON_SWORD)
            .lore("&8[ &e治愈光环 &7&o+%r(3-8) +%r(15-85)% +%r(8-20)% &8]")
            .attribute("attack_damage",
                    4.0D,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND,
                    Attribute.GENERIC_ATTACK_DAMAGE)
            .attribute("attack_speed",
                    4.0D,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlot.HAND,
                    Attribute.GENERIC_ATTACK_DAMAGE);
}

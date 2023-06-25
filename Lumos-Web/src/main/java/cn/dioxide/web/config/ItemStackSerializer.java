package cn.dioxide.web.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dioxide.CN
 * @date 2023/6/25
 * @since 1.0
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ItemStackSerializer {

    private int slot; // 添加一个slot属性
    private String displayName;
    private String material;
    private Map<String, Integer> enchantments;
    private List<String> lore;

    public static ItemStackSerializer convert(ItemStack itemStack, int slot) { // 添加一个slot参数
        if (itemStack == null) { // 检查itemStack是否为空
            return new ItemStackSerializer(slot, null, null, null, null);
        }

        ItemMeta meta = itemStack.getItemMeta();
        Map<String, Integer> enchantmentsMap = new HashMap<>();
        for (Map.Entry<Enchantment, Integer> entry : itemStack.getEnchantments().entrySet()) {
            enchantmentsMap.put(entry.getKey().getKey().getKey(), entry.getValue());
        }
        return new ItemStackSerializer(
                slot,
                meta != null ? meta.getDisplayName() : null,
                itemStack.getType().toString(),
                enchantmentsMap,
                meta != null ? meta.getLore() : null
        );
    }

    public static List<ItemStackSerializer> convert(ItemStack[] itemStacks) { // 使用List而不是数组
        List<ItemStackSerializer> result = new ArrayList<>();
        for (int i = 0; i < itemStacks.length; i++) {
            ItemStack itemStack = itemStacks[i];
            result.add(convert(itemStack, i)); // 传递槽位ID
        }
        return result;
    }

}

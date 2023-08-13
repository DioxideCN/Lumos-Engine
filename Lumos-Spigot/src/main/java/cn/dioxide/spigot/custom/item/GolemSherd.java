package cn.dioxide.spigot.custom.item;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Recipe;
import cn.dioxide.spigot.LumosStarter;
import cn.dioxide.spigot.custom.CustomItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

/**
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
@Custom("golem_sherd")
public class GolemSherd {

    private static final ItemStack item = CustomItem.with(Material.FRIEND_POTTERY_SHERD)
            .name("&7[&6傀儡残垣&7]")
            .lore("&7&o残垣碎片可以驱动强力附魔台")
            .lore("&7&o需要 &8&o雕刻南瓜 铁块 &7&o合成")
            .build();

    @Recipe
    public static ShapedRecipe registerCustomRecipe() {
        NamespacedKey key = new NamespacedKey(LumosStarter.INSTANCE, "golem_sherd");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("A","B");
        recipe.setIngredient('A', Material.CARVED_PUMPKIN);
        recipe.setIngredient('B', Material.IRON_BLOCK);
        return recipe;
    }

}

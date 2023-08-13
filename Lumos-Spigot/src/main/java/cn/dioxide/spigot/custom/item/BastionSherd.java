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
@Custom("bastion_sherd")
public class BastionSherd {

    private static final ItemStack item = CustomItem.with(Material.BURN_POTTERY_SHERD)
            .name("&7[&6堡垒残垣&7]")
            .lore("&7&o残垣碎片可以驱动强力附魔台")
            .lore("&7&o需要 &8&o金锭 镶金黑石 &7&o合成")
            .build();

    @Recipe
    public static ShapedRecipe registerCustomRecipe() {
        NamespacedKey key = new NamespacedKey(LumosStarter.INSTANCE, "bastion_sherd");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("A","B");
        recipe.setIngredient('A', Material.GOLD_INGOT);
        recipe.setIngredient('B', Material.GILDED_BLACKSTONE);
        return recipe;
    }

}

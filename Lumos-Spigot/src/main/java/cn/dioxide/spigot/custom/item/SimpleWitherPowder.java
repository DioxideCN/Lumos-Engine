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
@Custom("simple_wither_powder")
public class SimpleWitherPowder {

    private static final CustomItem item = CustomItem.with(Material.GRAY_DYE)
            .name("&f简易凋灵粉尘")
            .lore("&7&o合成物 锻造物");

    @Recipe
    public static ShapedRecipe registerCustomRecipe() {
        NamespacedKey key = new NamespacedKey(LumosStarter.INSTANCE, "simple_wither_powder");
        ShapedRecipe recipe = new ShapedRecipe(key, item.build());
        recipe.shape("AAA", "BBB", " B ");
        recipe.setIngredient('A', Material.WITHER_SKELETON_SKULL);
        recipe.setIngredient('B', Material.SOUL_SAND);
        return recipe;
    }

}

package cn.dioxide.spigot.custom.item;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Recipe;
import cn.dioxide.spigot.LumosStarter;
import cn.dioxide.spigot.custom.CustomItem;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ShapedRecipe;

/**
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
@Custom("simple_end_powder")
public class SimpleEndPowder {

    private static final CustomItem item = CustomItem.with(Material.GRAY_DYE)
            .name("&f简易末影粉尘")
            .lore("&7&o合成物 锻造物");

    @Recipe
    public static ShapedRecipe registerCustomRecipe() {
        NamespacedKey key = new NamespacedKey(LumosStarter.INSTANCE, "simple_end_powder");
        ShapedRecipe recipe = new ShapedRecipe(key, item.build());
        recipe.shape(" A ", "BBC", " D ");
        recipe.setIngredient('A', Material.ELYTRA);
        recipe.setIngredient('B', Material.BLACK_CONCRETE);
        recipe.setIngredient('C', Material.DRAGON_HEAD);
        recipe.setIngredient('D', Material.END_CRYSTAL);
        return recipe;
    }

}

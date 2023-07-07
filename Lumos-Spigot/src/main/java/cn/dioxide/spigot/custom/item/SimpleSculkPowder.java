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
@Custom("simple_sculk_powder")
public class SimpleSculkPowder {

    private static final ItemStack item = CustomItem.with(Material.GRAY_DYE)
            .name("&f简易幽匿粉尘")
            .lore("&7&o合成物 锻造物")
            .build();

    @Recipe
    public static ShapedRecipe registerCustomRecipe() {
        NamespacedKey key = new NamespacedKey(LumosStarter.INSTANCE, "simple_sculk_powder");
        ShapedRecipe recipe = new ShapedRecipe(key, item);
        recipe.shape("ABA", " C ", " D ");
        recipe.setIngredient('A', Material.SCULK_SENSOR);
        recipe.setIngredient('B', Material.SCULK_SHRIEKER);
        recipe.setIngredient('C', Material.SCULK_CATALYST);
        recipe.setIngredient('D', Material.SCULK);
        return recipe;
    }

}

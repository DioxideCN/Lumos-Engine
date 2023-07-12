package cn.dioxide.spigot.custom;

import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public interface ICustomSkillHandler {

    @NotNull List<ItemStack> endGearRecipe();

    @NotNull List<ItemStack> netherGearRecipe();

    @NotNull List<ItemStack> sculkGearRecipe();

    @EventHandler
    void onAltarRecipeCall(AltarRecipeEvent e);

    @EventHandler
    void onSkillTrigger(TriggerSkillEvent e);

}

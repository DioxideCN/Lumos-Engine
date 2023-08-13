package cn.dioxide.spigot.custom.structure;

import cn.dioxide.spigot.custom.structure.Factor;
import cn.dioxide.spigot.custom.structure.TriggerSkillAttackEvent;
import cn.dioxide.spigot.custom.structure.Altar;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public interface ICustomSkillHandler {

    @NotNull List<Factor> getFactor();

    @NotNull String getEntry();

    void attachRecipe(Player player, Altar altar);

    boolean satisfyCondition(Material material);

    @Nullable ItemStack endStoneOne();

    @Nullable ItemStack netherWartOne();

    @Nullable ItemStack sculkCatalystOne();

    @Nullable ItemStack amethystOne();

    @EventHandler
    void onSkillTrigger(TriggerSkillAttackEvent e);

}

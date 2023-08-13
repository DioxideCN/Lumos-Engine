package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.custom.structure.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/8/6
 * @since 1.0
 */
@Event
@Custom(value = "exorcism_power", type = CustomType.SKILL_TYPE, skillName = "祛魔之力")
public class ExorcismPowerSkill implements Listener, ICustomSkillHandler {
    @Override
    public @NotNull List<Factor> getFactor() {
        return new ArrayList<>(){{
            add(Factor.with(Quality.STRONG, 0.55F, 0.5F, World.Environment.THE_END));
        }};
    }

    @Override
    public @NotNull String getEntry() {
        return "&8[ &e祛魔之力 &7&o+%r(10-100) &8]";
    }

    @Override
    public void attachRecipe(Player player, Altar altar) {
        DefaultSkillHelper.attachSkillToWeapon(this, altar);
    }

    @Override
    public boolean satisfyCondition(Material material) {
        return PlayerUtils.isSword(material) || PlayerUtils.isAxe(material);
    }

    @Override
    public @Nullable ItemStack endStoneOne() {
        return null;
    }

    @Override
    public @Nullable ItemStack netherWartOne() {
        return null;
    }

    @Override
    public @Nullable ItemStack sculkCatalystOne() {
        return null;
    }

    @Override
    public @Nullable ItemStack amethystOne() {
        return null;
    }

    @Override
    @EventHandler
    public void onSkillTrigger(TriggerSkillAttackEvent e) {

    }
}

package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.custom.*;
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
@Custom(value = "soul_counseling", type = CustomType.SKILL_TYPE, skillName = "灵魂疏导")
public class SoulCounselingSkill implements Listener, ICustomSkillHandler {

    @Override
    public @NotNull List<Factor> getFactor() {
        return new ArrayList<>(){{
            add(Factor.with(Quality.STRONG, 0.27F, 2.0F, World.Environment.NETHER));
        }};
    }

    @Override
    public @NotNull String getEntry() {
        return "&8[ &e灵魂疏导 &7&o+%r(15-85) +%r(25-80)% &8]";
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
        return CustomRegister.get("simple_wither_powder");
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

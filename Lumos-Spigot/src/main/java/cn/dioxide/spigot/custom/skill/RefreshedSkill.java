package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.common.util.CalcUtils;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.custom.structure.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
@Custom(value = "refreshed", type = CustomType.SKILL_TYPE, skillName = "精神焕发")
public class RefreshedSkill implements Listener, ICustomSkillHandler {

    @Override
    public @NotNull List<Factor> getFactor() {
        return new ArrayList<>(){{
            add(Factor.with(Quality.STRONG, 0.26F, 0.65F, World.Environment.NORMAL));
        }};
    }

    @Override
    public @NotNull String getEntry() {
        return "&8[ &e精神焕发 &7&o+%r(23-58) &8]";
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
        if (e.getPair().left().equals("精神焕发")) {
            if (!CalcUtils.checkProbability(getChance(e.getLore()))) {
                return;
            }
            Player player = e.getPlayer();
            LivingEntity entity = e.getEntity();
            if (entity.getHealth() - e.getDamageEvent().getFinalDamage() <= 0) {
                player.addPotionEffects(new ArrayList<>() {{
                    add(new PotionEffect(PotionEffectType.INCREASE_DAMAGE,
                            60, 0, true, false, true));
                    add(new PotionEffect(PotionEffectType.SPEED,
                            60, 0, true, false, true));
                }});
            }
        }
    }

    // 恢复半径
    private double getChance(String skillLore) {
        return DefaultSkillHelper.getNumberFromLore(skillLore.split(" ")[2]);
    }

}

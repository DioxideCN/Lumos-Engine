package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.common.util.CalcUtils;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.custom.*;
import cn.dioxide.spigot.custom.structure.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Event
@Custom(value = "healing_skill", type = CustomType.SKILL_TYPE, skillName = "治愈光环")
public class HealingSkill implements Listener, ICustomSkillHandler {

    @Override
    public @NotNull List<Factor> getFactor() {
        return new ArrayList<>(){{
            add(Factor.with(Quality.STRONG, 0.12F, 0.65F, World.Environment.NORMAL));
        }};
    }

    @Override
    public @NotNull String getEntry() {
        // 治愈半径 治愈概率 治愈量
        return "&8[ &e治愈光环 &7&o+%r(3-8) +%r(15-85)% +%r(5-50)% &8]";
    }

    @Override
    public void attachRecipe(Player player, Altar altar) {
        DefaultSkillHelper.attachSkillToWeapon(this, altar);
    }

    // 只能附加到剑和斧上
    @Override
    public boolean satisfyCondition(Material material) {
        return PlayerUtils.isSword(material) || PlayerUtils.isAxe(material);
    }

    @Override
    @EventHandler
    public void onSkillTrigger(TriggerSkillAttackEvent e) {
        if (e.getPair().left().equals("治愈光环")) {
            if (!CalcUtils.checkProbability(getChance(e.getLore()))) {
                return;
            }
            Player player = e.getPlayer();
            double distance = getDistance(e.getLore());
            double percentage = getPercentage(e.getLore());
            DefaultAnimation.circleParticle(player.getLocation(), distance + 0.5, false, Particle.END_ROD);
            double baseDamage = CalcUtils.getBaseDamage(e.getWeapon());
            double extra = Math.min(e.getDamage() / baseDamage, 1.0);
            // 恢复血量
            for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
                if (entity instanceof Player nearbyPlayer) {
                    AttributeInstance maxHealthAttr = nearbyPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH);
                    if (maxHealthAttr == null) continue;
                    double maxHealth = maxHealthAttr.getValue();
                    double healthToRestore = (maxHealth - nearbyPlayer.getHealth()) * (percentage / 100.0) * extra;
                    double newHealth = nearbyPlayer.getHealth() + healthToRestore;
                    if (newHealth > maxHealth) {
                        newHealth = maxHealth;
                    }
                    nearbyPlayer.setHealth(newHealth);
                    Location loc = nearbyPlayer.getLocation().add(0, 1.8, 0);
                    nearbyPlayer.getWorld().spawnParticle(
                            Particle.HEART, loc, 1, 0, 0, 1, 0.1, null, true);
                }
            }
        }
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
        return CustomRegister.get("amethyst_sherd");
    }

    // 恢复半径
    private double getDistance(String skillLore) {
        return DefaultSkillHelper.getNumberFromLore(skillLore.split(" ")[2]);
    }

    // 恢复概率
    private double getChance(String skillLore) {
        return DefaultSkillHelper.getNumberFromLore(skillLore.split(" ")[3]);
    }

    // 恢复百分比
    private double getPercentage(String skillLore) {
        return DefaultSkillHelper.getNumberFromLore(skillLore.split(" ")[4]);
    }

}

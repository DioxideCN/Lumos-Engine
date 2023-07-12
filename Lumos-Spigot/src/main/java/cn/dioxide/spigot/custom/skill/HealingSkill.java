package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.common.util.CalcUtils;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.custom.*;
import cn.dioxide.spigot.custom.structure.Altar;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // 治愈半径 治愈概率 治愈量
    private static final String entry = "&8[ &e治愈光环 &7&o+%r(3-8) +%r(15-85)% +%r(5-50)% &8]";

    @Override
    @EventHandler
    public void onAltarRecipeCall(AltarRecipeEvent e) {
        Altar altar = e.getAltar();
        if (altar.match(this)) {
            Optional.ofNullable(altar.getWeapon()).ifPresent(weapon -> {
                Material type = weapon.getType();
                // 只能附加到剑和斧上
                if (!PlayerUtils.isSword(type) && !PlayerUtils.isAxe(type)) return;
                altar.setWeapon(
                        CustomItem.with(weapon)
                                .attach(entry) // 附着
                                .enchant(Enchantment.DURABILITY, 1, true)
                                .build());
                DefaultAnimation.spawnBallParticle(altar.getLoc().add(0.5, 1.5, 0.5));
                assert altar.getLoc().getWorld() != null;
                altar.getLoc().getWorld() // 播放音效
                        .playSound(altar.getLoc(),
                                Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            });
        }
    }

    @Override
    @EventHandler
    public void onSkillTrigger(TriggerSkillEvent e) {
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
    public @NotNull List<ItemStack> endGearRecipe() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull List<ItemStack> netherGearRecipe() {
        return new ArrayList<>() {{
            add(CustomRegister.get("simple_wither_powder"));
        }};
    }

    @Override
    public @NotNull List<ItemStack> sculkGearRecipe() {
        return new ArrayList<>();
    }

    private static final Pattern pattern = Pattern.compile("\\d+"); // 匹配一个或多个数字

    // 恢复半径
    private static double getDistance(String skillLore) {
        return getInteger(skillLore.split(" ")[2]);
    }

    // 恢复概率
    private static double getChance(String skillLore) {
        return getInteger(skillLore.split(" ")[3]);
    }

    // 恢复百分比
    private static double getPercentage(String skillLore) {
        return getInteger(skillLore.split(" ")[4]);
    }

    private static double getInteger(String s) {
        Pattern pattern = Pattern.compile("\\+([0-9]*\\.?[0-9]+)"); // 正则表达式，匹配以+开始的小数
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        } else {
            throw new IllegalArgumentException("No valid number found in the input string.");
        }
    }

}

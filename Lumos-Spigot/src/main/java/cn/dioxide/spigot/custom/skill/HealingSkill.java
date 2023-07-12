package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Custom;
import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.infra.CustomType;
import cn.dioxide.spigot.custom.*;
import cn.dioxide.spigot.custom.structure.Altar;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Event
@Custom(value = "healing_skill", type = CustomType.SKILL_TYPE, skillName = "治愈光环")
public class HealingSkill implements Listener, ICustomSkillHandler {

    // 治愈半径 治愈概率 治愈量
    private static final String entry = "&8[ &e治愈光环 &7&o+%r(3-8) +%r(15-85)% +%r(8-20)% &8]";

    @Override
    @EventHandler
    public void onAltarRecipeCall(AltarRecipeEvent e) {
        Altar altar = e.getAltar();
        if (altar.match(this)) {
            DefaultAnimation.spawnBallParticle(altar.getLoc().add(0.5, 1.5, 0.5));
            assert altar.getLoc().getWorld() != null;
            altar.getLoc().getWorld() // 播放音效
                    .playSound(altar.getLoc(),
                            Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
            Optional.ofNullable(altar.getWeapon()).ifPresent(weapon -> altar.setWeapon(
                    CustomItem.with(weapon)
                            .attach(entry) // 附着
                            .enchant(Enchantment.DURABILITY, 1, true)
                            .build()));
        }
    }

    @Override
    @EventHandler
    public void onSkillTrigger(TriggerSkillEvent e) {

    }

    @Override
    public @NotNull List<ItemStack> endGearRecipe() {
        return new ArrayList<>() {{
            add(CustomRegister.get("simple_end_powder"));
        }};
    }

    @Override
    public @NotNull List<ItemStack> netherGearRecipe() {
        return new ArrayList<>();
    }

    @Override
    public @NotNull List<ItemStack> sculkGearRecipe() {
        return new ArrayList<>();
    }
}

package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.common.extension.Pair;
import cn.dioxide.spigot.LumosStarter;
import cn.dioxide.spigot.custom.CustomItem;
import cn.dioxide.spigot.custom.CustomRegister;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dioxide.CN
 * @date 2023/7/12
 * @since 1.0
 */
public class DefaultSkillHelper {

    public static Pair<String, ICustomSkillHandler> getSkill(@NotNull final String lore) {
        AtomicReference<Pair<String, ICustomSkillHandler>> res = new AtomicReference<>(null);
        String[] split = lore.split(" ");
        if (split.length < 4) throw new RuntimeException("Illegal skill lore");
        String name = split[1];
        CustomRegister.get().forEach((k, p) -> {
            if (name.contains(p.left())) {
                res.set(p);
            }
        });
        return res.get();
    }

    public static void attachSkillToWeapon(ICustomSkillHandler handler, Altar altar) {
        if (altar.match(handler)) {
            Optional.ofNullable(altar.getWeapon()).ifPresent(weapon -> {
                DefaultAnimation.spawnBallParticle(altar.getLoc());
                Bukkit.getScheduler().runTaskLater(
                        LumosStarter.INSTANCE,
                        () -> {
                            if (altar.match(handler)) {
                                altar.setWeapon(
                                        CustomItem.with(weapon)
                                                .attach(handler.getEntry()) // 附着
                                                .build());
                            }
                        }, 40L);
            });
        }
    }

    @Nullable
    @Unsafe(proposer = "Dioxide_CN")
    public static ItemStack simpleAttach(ItemStack weapon, ICustomSkillHandler handler) {
        if (handler.satisfyCondition(weapon.getType())) {
            return CustomItem.with(weapon)
                    .attach(handler.getEntry()) // 附着
                    .build();
        } else {
            return null;
        }
    }

    public static double getNumberFromLore(String s) {
        Pattern pattern = Pattern.compile("\\+([0-9]*\\.?[0-9]+)"); // 正则表达式，匹配以+开始的小数
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group(1));
        } else {
            throw new IllegalArgumentException("No valid number found in the input string.");
        }
    }

}

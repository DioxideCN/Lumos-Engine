package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.extension.Pair;
import cn.dioxide.spigot.custom.CustomRegister;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Dioxide.CN
 * @date 2023/7/12
 * @since 1.0
 */
public class DefaultSkillHelper {

    public static Pair<String, Class<?>> getSkill(@NotNull final String lore) {
        AtomicReference<Pair<String, Class<?>>> res = new AtomicReference<>(null);
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

}

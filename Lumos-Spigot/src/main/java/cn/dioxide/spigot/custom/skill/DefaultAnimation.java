package cn.dioxide.spigot.custom.skill;

import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public class DefaultAnimation {

    public static void spawnBallParticle(Location location) {
        // 创建烟花实体
        if (location.getWorld() == null) return;
        // 创建烟花效果（颜色，形状等）
        FireworkEffect effect1 = FireworkEffect.builder()
                .withColor(Color.RED, Color.WHITE, Color.ORANGE, Color.BLUE)
                .with(FireworkEffect.Type.BALL)
                .flicker(false)
                .build();
        FireworkEffect effect2 = FireworkEffect.builder()
                .withColor(Color.PURPLE, Color.OLIVE, Color.AQUA, Color.YELLOW)
                .with(FireworkEffect.Type.BALL)
                .flicker(false)
                .build();
        Firework firework = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta meta = firework.getFireworkMeta();
        meta.addEffects(effect1, effect2);
        firework.setFireworkMeta(meta);
        firework.detonate();
    }

}

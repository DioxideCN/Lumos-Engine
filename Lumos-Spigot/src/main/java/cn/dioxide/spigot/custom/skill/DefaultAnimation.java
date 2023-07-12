package cn.dioxide.spigot.custom.skill;

import cn.dioxide.common.annotation.Unsafe;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public class DefaultAnimation {

    private static final double PI = Math.PI;

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

    /**
     * 生成圆形粒子效果
     *
     * @param location 生成位置
     * @param radius 半径
     * @param filled 是否实心圆
     * @param p 粒子类型
     */
    @Unsafe(proposer = "Dioxide_CN")
    public static void circleParticle(Location location, double radius, boolean filled, Particle p) {
        World world = location.getWorld();
        if (world == null) return;
        for (double t = 0; t <= 2 * PI; t += PI / 64) {
            double x = radius * Math.sin(t);
            double z = radius * Math.cos(t);
            Location loc = new Location(world, location.getX() + x, location.getY(), location.getZ() + z);
            spawnParticle(world, p, loc, 1, 0, 0, 0, 0, true);
            // 是否填充圆环
            if (filled) {
                for (double i = 0; i < radius; i += 0.5) {
                    double innerX = i * Math.sin(t);
                    double innerZ = i * Math.cos(t);
                    World innerWorld =
                            new Location(world, location.getX() + innerX, location.getY(), location.getZ() + innerZ).getWorld();
                    if (innerWorld == null) return;
                    spawnParticle(innerWorld, p, loc, 1, 0, 0, 0, 0, true);
                }
            }
        }
    }

    private static void spawnParticle(World world, Particle particle, Location location, int count, double offsetX, double offsetY, double offsetZ, double speed, boolean force) {
        world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, speed, null, force);
    }

}

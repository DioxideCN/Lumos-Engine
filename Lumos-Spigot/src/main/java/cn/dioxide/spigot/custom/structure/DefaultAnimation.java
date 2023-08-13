package cn.dioxide.spigot.custom.structure;

import cn.dioxide.common.annotation.Unsafe;
import cn.dioxide.spigot.LumosStarter;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * @author Dioxide.CN
 * @date 2023/7/11
 * @since 1.0
 */
public class DefaultAnimation {

    private static final double PI = Math.PI;

    public static void spawnBallParticle(Location location) {
        World world = location.getWorld();
        if (world == null) return;
        Random rand = new Random();
        int randomNum = rand.nextInt((180 - 80) + 1) + 80;
        world.spawnParticle(Particle.ENCHANTMENT_TABLE, location.add(0.5, 2.5, 0.5), randomNum, 0.125, 0.125, 0.125, 3);
        Bukkit.getScheduler().runTaskLater(
                JavaPlugin.getProvidingPlugin(LumosStarter.class),
                () -> {
                    world.playSound(location, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 1.0F, 1.0F);
                    world.spawnParticle(Particle.END_ROD, location.add(0, -1.3, 0), 65, 0.125, 0.025, 0.125, 0.2);
                },
                32L);
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

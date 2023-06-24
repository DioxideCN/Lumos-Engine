package cn.dioxide.spigot.scheduler;

import cn.dioxide.common.annotation.LoopThis;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.infra.EffectTarget;
import cn.dioxide.common.infra.TrimUpgradeStore;
import cn.dioxide.common.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

/**
 * @author Dioxide.CN
 * @date 2023/6/8
 * @since 1.0
 */
public class TrimUpgradeScheduler {

    /**
     * 每2.5秒执行一次，降低服务器压力
     */
    @LoopThis(period = 50L)
    public static void effectEnhancer() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            effectPlayer(player);
        }
    }

    // 整个运算逻辑最多遍历16次，空间换时间
    private static void effectPlayer(@NotNull Player p) {
        // 统计并合并玩家满足的增益
        HashMap<TrimPattern, Integer> countResult = PlayerUtils.archivePlayerArmor(p);
        // pattern纹理穿戴的数量为count，最多遍历4次
        countResult.forEach((pattern, count) -> {
            TrimUpgradeStore store = Config.TRIM_UPGRADE_MAP.get(pattern);
            // 最多遍历4次
            for (TrimUpgradeStore.Buff buff : store.buffList) {
                if (buff.count == 0) {
                    // 预先结束条件，矛盾的多件套数量
                    continue;
                }
                if (count != buff.count) {
                    // 预先结束条件，不属于这个多件套效果
                    continue;
                }
                if (buff.effect == null) {
                    // 预先结束条件，不具备effect效果
                    continue;
                }
                if (buff.effectTarget != EffectTarget.SELF) {
                    // 预先结束条件，effect不是自身
                    continue;
                }
                // 如果玩家已经拥有了buff.effect并且amplifier比(buff.effectLevel-1)更高就不做任何操作
                if (p.hasPotionEffect(buff.effect)) {
                    PotionEffect existingEffect = p.getPotionEffect(buff.effect);
                    if (existingEffect != null && existingEffect.getAmplifier() > buff.effectLevel - 1) {
                        break;
                    }
                }
                // 给予等级药水效果
                p.addPotionEffect(
                        new PotionEffect(buff.effect,
                                20*8,
                                buff.effectLevel - 1,
                                true,
                                false,
                                false));
            }
        });
    }

}

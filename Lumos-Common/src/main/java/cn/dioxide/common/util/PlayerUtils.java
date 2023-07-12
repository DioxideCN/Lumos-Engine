package cn.dioxide.common.util;

import cn.dioxide.common.extension.Config;
import cn.dioxide.common.extension.Pair;
import cn.dioxide.common.infra.EffectTarget;
import cn.dioxide.common.infra.EventType;
import cn.dioxide.common.infra.TrimUpgradeStore;
import com.google.common.util.concurrent.AtomicDouble;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Dioxide.CN
 * @date 2023/6/22
 * @since 1.0
 */
public class PlayerUtils {

    public static boolean isSword(Material type) {
        return switch (type) {
            case WOODEN_SWORD, STONE_SWORD, IRON_SWORD, DIAMOND_SWORD, NETHERITE_SWORD -> true;
            default -> false;
        };
    }

    public static boolean isAxe(Material type) {
        return switch (type) {
            case WOODEN_AXE, STONE_AXE, IRON_AXE, DIAMOND_AXE, NETHERITE_AXE -> true;
            default -> false;
        };
    }

    /**
     * 获取在线所有人的玩家名
     * @return 在线所有人的玩家名集合
     */
    public static List<String> getOnlinePlayerNameList() {
        return Bukkit
                .getOnlinePlayers()
                .stream()
                .map(Player::getName)
                .collect(Collectors.toList());
    }

    /**
     * 返回玩家满足的Trim增益类型和几件套
     * @param p 玩家
     * @return 返回玩家的增益满足情况
     */
    public static HashMap<TrimPattern, Integer> archivePlayerArmor(Player p) {
        PlayerInventory inv = p.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chestplate = inv.getChestplate();
        ItemStack leggings = inv.getLeggings();
        ItemStack boots = inv.getBoots();
        return archiveArmor(helmet, chestplate, leggings, boots);
    }

    private static HashMap<TrimPattern, Integer> archiveArmor(ItemStack ...armors) {
        HashMap<TrimPattern, Integer> countMap = new HashMap<>(4);
        // 遍历每个盔甲
        for (ItemStack armor : armors) {
            if (armor == null) continue;
            ItemMeta itemMeta = armor.getItemMeta();
            if (!(itemMeta instanceof ArmorMeta armorMeta)) continue;
            ArmorTrim trim = armorMeta.getTrim();
            if (trim == null) continue;
            // 盔甲纹饰
            TrimPattern trimPattern = trim.getPattern();
            // 纹饰材质
            TrimMaterial trimMaterial = trim.getMaterial();
            // 盔甲材质
            String armorMaterial = armor.getType().name().split("_")[0].toLowerCase();
            TrimUpgradeStore store = Config.TRIM_UPGRADE_MAP.get(trimPattern);
            if ((store.trimMaterials.contains(trimMaterial) || store.trimMaterials.size() == 0) &&
                    (store.armorMaterial.contains(armorMaterial) || store.armorMaterial.size() == 0)) {
                // 计数+1
                countMap.merge(trimPattern, 1, Integer::sum);
            }
        }
        // 筛选出符合条件的条目
        countMap.entrySet().removeIf(entry -> {
            TrimUpgradeStore store = Config.TRIM_UPGRADE_MAP.get(entry.getKey());
            int value = entry.getValue();
            int maxCount = 0;
            boolean found = false;
            // 这里最多遍历4次
            for (TrimUpgradeStore.Buff buff : store.buffList) {
                if (buff.count == 0) {
                    // 预先结束条件，矛盾的多件套数量
                    continue;
                }
                if (value >= buff.count) {
                    maxCount = buff.count;
                    found = true;
                } else {
                    break;
                }
            }
            if (found) {
                entry.setValue(maxCount);
            }
            return !found;
        });
        // 此时的countMap保留了满足几件套增益的项目
        return countMap;
    }

    /**
     * 玩家是否满足这个事件
     * @param player 哪个玩家
     * @param eventType 哪个事件
     * @return 返回左边是否满足，右边为概率
     */
    public static Pair<Boolean, Double> isSatisfyEvent(Player player, EventType eventType) {
        HashMap<TrimPattern, Integer> archived = PlayerUtils.archivePlayerArmor(player);
        AtomicBoolean flag = new AtomicBoolean(false);
        AtomicDouble chance = new AtomicDouble(0D);
        archived.forEach((pattern, count) -> {
            TrimUpgradeStore store = Config.TRIM_UPGRADE_MAP.get(pattern);
            for (TrimUpgradeStore.Buff buff : store.buffList) {
                if (buff.count == 0) {
                    // 预先结束条件，矛盾的多件套数量
                    continue;
                }
                if (buff.event == null) {
                    continue; // 预先终止，没有事件
                }
                if (buff.count != count) {
                    continue; // 预先终止，不是这个多件套效果
                }
                if (buff.event != eventType) {
                    continue; // 预先终止，不是这个事件
                }
                System.out.println("有事件");
                flag.set(true);
                System.out.println("chance " + buff.chance);
                chance.set(buff.chance);
            }
        });
        return Pair.of(flag.get(), chance.get());
    }

    /**
     * 判断玩家满足了哪个effect-target效果
     * @param player 玩家
     * @return 返回左边为store右边为几件套效果
     */
    public static TrimUpgradeStore.Buff whichEventBuff(Player player) {
        HashMap<TrimPattern, Integer> archived =
                PlayerUtils.archivePlayerArmor(player);
        AtomicReference<TrimUpgradeStore.Buff> result =
                new AtomicReference<>(null);
        archived.forEach((pattern, count) -> {
            TrimUpgradeStore store = Config.TRIM_UPGRADE_MAP.get(pattern);
            for (TrimUpgradeStore.Buff buff : store.buffList) {
                if (buff.count == 0) {
                    // 预先结束条件，矛盾的多件套数量
                    continue;
                }
                if (buff.effect == null) {
                    continue; // 预先终止，没有提供药水效果
                }
                if (buff.effectTarget != EffectTarget.ATTACKER) {
                    continue; // 预先终止，没有指定effectTarget为ATTACKER
                }
                result.set(buff);
            }
        });
        return result.get();
    }
}

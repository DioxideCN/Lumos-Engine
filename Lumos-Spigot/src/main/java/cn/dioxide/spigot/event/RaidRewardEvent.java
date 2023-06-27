package cn.dioxide.spigot.event;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.spigot.LumosStarter;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Dioxide.CN
 * @date 2023/6/25
 * @since 1.0
 */
public class RaidRewardEvent implements Listener {

    @EventHandler
    public void onRaidFinish(RaidFinishEvent event) {
        if (event.getRaid().getTotalWaves() >= 1) {
            for (Player player : event.getWinners()) {
                // 检查玩家是否已经拥有成就
                Advancement advancement = LumosStarter.INSTANCE.getServer().getAdvancement(NamespacedKey.minecraft("adventure/calamity_buster"));
                if (advancement != null) {
                    AdvancementProgress progress = player.getAdvancementProgress(advancement);
                    if (!progress.isDone()) {
                        // 给予玩家经验修补的附魔书
                        ItemStack enchantedBook = new ItemStack(Material.ENCHANTED_BOOK);
                        enchantedBook.addUnsafeEnchantment(Enchantment.MENDING, 1);
                        player.getInventory().addItem(enchantedBook);

                        // 授予玩家成就
                        for (String criteria : progress.getRemainingCriteria()) {
                            progress.awardCriteria(criteria);
                        }
                    }
                }
            }
        }
    }

}

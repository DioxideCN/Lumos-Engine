package cn.dioxide.spigot.feature;

import cn.dioxide.common.annotation.Event;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * @author Dioxide.CN
 * @date 2023/6/19
 * @since 1.0
 */
@Event(configKey = "feature.crafting-table")
public class CraftingTableFeature implements Listener {
    private final Random random = new Random();

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
            Material type = event.getBlock().getType();
            // 检查是否是工作台
            if (type == Material.CRAFTING_TABLE) {
                // 阻止默认的方块掉落
                event.setDropItems(false);
                // 掉落1-3个橡木木板
                int amount = random.nextInt(3) + 1; // random.nextInt(3) 会返回0，1，2，所以我们需要加1
                ItemStack planks = new ItemStack(Material.OAK_PLANKS, amount);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), planks);
            }
            // 检查是否是火把、灵魂火把、红石火把、拉杆
            if (type == Material.TORCH ||
                    type == Material.SOUL_TORCH ||
                    type == Material.REDSTONE_TORCH ||
                    type == Material.LEVER) {
                // 阻止默认的方块掉落
                event.setDropItems(false);
                // 掉落1根木棍
                ItemStack stick = new ItemStack(Material.STICK, 1);
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), stick);
            }
        }
    }
}

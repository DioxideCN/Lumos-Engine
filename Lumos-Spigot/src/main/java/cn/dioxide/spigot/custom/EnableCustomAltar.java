package cn.dioxide.spigot.custom;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.spigot.custom.structure.Altar;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MainHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 在不同的生物群系会产生不同的意志，意志会影响祭坛的加成效果
 * 寒霜意志：冰川
 * 炙热意志：沙漠
 * 繁茂意志：繁茂洞穴
 * 腐坏意志：蘑菇岛
 *
 * @author Dioxide.CN
 * @date 2023/7/7
 * @since 1.0
 */
@Event
public class EnableCustomAltar implements Listener {

    // 定义偏移数组
    public static final int[][] offsets = { // {offsetX, offsetZ}
            {-2, 0}, {2, 0}, {0, -2}, {0, 2},
            {-4, 0}, {4, 0}, {0, -4}, {0, 4}, {3, 3}, {3, -3}, {-3, 3}, {-3, -3},
            {-6, 0}, {6, 0}, {0, -6}, {0, 6}, {3, 5}, {3, -5}, {-3, 5}, {-3, -5}, {5, 3}, {5, -3}, {-5, 3}, {-5, -3}
    };

    @EventHandler
    public void buildAltar(BlockPlaceEvent event) {
        Block placedBlock = event.getBlockPlaced();
        if (placedBlock.getType() != Material.LODESTONE) {
            return;
        }
        Block aboveBlock = placedBlock.getRelative(BlockFace.UP);
        if (aboveBlock.getType() != Material.CHEST) {
            return;
        }
        Chest chest = (Chest) aboveBlock.getState();
        Inventory inventory = chest.getInventory();
        if (containsItems(inventory, Material.SCULK, 12) &&
                containsItems(inventory, Material.CRYING_OBSIDIAN, 8) &&
                containsItems(inventory, Material.END_STONE, 4)) {
            // 方块是磁石，上方是箱子，箱子内有8个哭泣黑曜石
            placedBlock.getWorld().strikeLightning(placedBlock.getLocation()); // 召唤闪电
            aboveBlock.setType(Material.AIR); // 设置为AIR方块
            int x = placedBlock.getX();
            int y = placedBlock.getY();
            int z = placedBlock.getZ();
            // 放置末地石、哭泣的黑曜石、幽匿块
            for (int i = 0; i < offsets.length; i++) {
                placedBlock.getWorld()
                        .getBlockAt(x + offsets[i][0], y, z + offsets[i][1])
                        .setType(i <= 3 ? Material.END_STONE : i <= 11 ? Material.CRYING_OBSIDIAN : Material.SCULK);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 如果玩家右键点击了方块
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getHand() == EquipmentSlot.HAND) {
            System.out.println(Altar.isAltar(event.getClickedBlock()));
        }
    }

    private boolean containsItems(Inventory inventory, Material material, int amount) {
        int count = 0;
        for (ItemStack item : inventory.getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }
}

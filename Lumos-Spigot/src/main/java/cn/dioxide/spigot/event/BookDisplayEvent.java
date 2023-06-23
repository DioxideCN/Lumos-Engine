package cn.dioxide.spigot.event;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.util.ColorUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Objects;

@Event
public class BookDisplayEvent implements Listener {
    @EventHandler
    public void playerClickItemFrame2ReadBook(PlayerInteractEntityEvent e) {
        if (e.getRightClicked().getType() != EntityType.ITEM_FRAME)
            return;

        ItemFrame itemFrame = (ItemFrame) e.getRightClicked();
        if (itemFrame.getItem().getType() != Material.WRITTEN_BOOK) {
            return;
        }

        e.getPlayer().openBook(itemFrame.getItem());
    }

    @EventHandler
    public synchronized void playerPutBook2Wall(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        ItemStack awaitBook = e.getPlayer().getInventory().getItemInMainHand(); // 成书
        if (!awaitBook.getType().equals(Material.WRITTEN_BOOK) ||
                awaitBook.getItemMeta() == null ||
                awaitBook.getItemMeta().getLore() == null ||
                awaitBook.getItemMeta().getLore().size() < 1) {
            return;
        }
        if (!awaitBook
                .getItemMeta()
                .getLore()
                .get(awaitBook.getItemMeta().getLore().size() - 1)
                .contains("右键方块放置激活的成书")) {
            return;
        }
        if (e.getClickedBlock() == null) {
            return;
        }
        if (Objects.equals(e.getClickedBlock().getType(), Material.AIR)) {
            return;
        }
        if (!e.getPlayer().hasPermission("hover.display.book.place")) {
            Format.use().player().notice(e.getPlayer(), "&c你没有放置该成书的权限");
            return;
        }
        Block clickedBlock = e.getClickedBlock();      // 方块类型
        BlockFace clickedBlockFace = e.getBlockFace(); // 方块朝向

        // 初始化位置
        int itemFramePositionX = clickedBlock.getX();
        int itemFramePositionY = clickedBlock.getY();
        int itemFramePositionZ = clickedBlock.getZ();
        BlockFace itemFrameFacing = BlockFace.UP;
        // 确定物品展示框的朝向
        switch (clickedBlockFace) {
            case UP -> itemFramePositionY++;
            case DOWN -> {
                itemFramePositionY--;
                itemFrameFacing = BlockFace.DOWN;
            }
            case WEST -> {
                itemFramePositionX--;
                itemFrameFacing = BlockFace.WEST;
            }
            case EAST -> {
                itemFramePositionX++;
                itemFrameFacing = BlockFace.EAST;
            }
            case NORTH -> {
                itemFramePositionZ--;
                itemFrameFacing = BlockFace.NORTH;
            }
            case SOUTH -> {
                itemFramePositionZ++;
                itemFrameFacing = BlockFace.SOUTH;
            }
        }

        BookMeta awaitBookMeta = (BookMeta) awaitBook.getItemMeta();
        if (awaitBookMeta == null)
            return;
        awaitBookMeta.setDisplayName(ColorUtils.replace("&b" + awaitBookMeta.getTitle()));
        awaitBookMeta.getLore().remove(awaitBookMeta.getLore().size() - 1);
        awaitBook.setItemMeta(awaitBookMeta);

        Location location = new Location(clickedBlock.getWorld(), itemFramePositionX, itemFramePositionY, itemFramePositionZ);
        ItemFrame itemFrame = (ItemFrame) clickedBlock.getWorld().spawnEntity(location, EntityType.ITEM_FRAME);
        itemFrame.setFixed(true);
        ItemStack awaitBook_temp = awaitBook.clone();
        awaitBook_temp.setAmount(1);
        itemFrame.setItem(awaitBook_temp);
        itemFrame.setFacingDirection(itemFrameFacing);
        // 添加 owner.player_name 的tag方便后续owner移除
        itemFrame.addScoreboardTag("owner." + e.getPlayer().getName());
        if (!e.getPlayer().isOp() || !Config.get().display.book.consume) {
            // 非OP需要扣除
            awaitBook.setAmount(awaitBook.getAmount() - 1);
        }
    }

    @EventHandler
    public void playerRemoveBookFromWall(HangingBreakByEntityEvent e) {
        if (e.getEntity() instanceof ItemFrame itemFrame &&
                e.getRemover() instanceof Player player) {
            // 是否被固定并且tag包含 owner.player_name
            if ((itemFrame.isFixed() &&
                    itemFrame.getScoreboardTags().contains("owner." + player.getName())) ||
                    player.isOp()) {
                // 掉落展示框中的书本
                ItemStack item = itemFrame.getItem();
                itemFrame.getWorld().dropItemNaturally(itemFrame.getLocation(), item);
                itemFrame.remove();
            } else {
                Format.use().player().notice(player, "&c该成书展示架不属于你");
                e.setCancelled(true);
            }
        }
    }
}

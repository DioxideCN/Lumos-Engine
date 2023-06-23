package cn.dioxide.spigot.service;

import cn.dioxide.common.extension.Format;
import cn.dioxide.common.util.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/6/18
 * @since 1.0
 */
public class DisplayBook {

    public boolean enableHoverBook(Player p) {
        if (!p.hasPermission("hover.display.book.enable")) {
            Format.use().player().noticePrefix(p, "&c你没有权限，无法激活该图书");
            return true;
        }

        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        if (!mainHandItem.getType().equals(Material.WRITTEN_BOOK)) {
            p.sendMessage(ColorUtils.replace("&c你没有手持一本可供激活的成书"));
            return true;
        }
        if (mainHandItem.getItemMeta() == null) {
            p.sendMessage(ColorUtils.replace("&c你所手持的图书没有有效的内容"));
            return true;
        }
        BookMeta bookMeta = (BookMeta) mainHandItem.getItemMeta();

        if (!bookMeta.hasTitle()) {
            p.sendMessage(ColorUtils.replace("&c你所手持的图书没有有效的标题"));
        }

        List<String> bookLoreList = bookMeta.getLore() == null ? new ArrayList<>() : bookMeta.getLore(); // 书籍注释
        bookLoreList.add(ColorUtils.replace("&a右键方块放置激活的成书"));
        bookMeta.setLore(bookLoreList);
        mainHandItem.setItemMeta(bookMeta);
        Format.use().player().noticePrefix(p, "&7你手中的成书已被&a激活");
        return true;
    }

    public boolean disableHoverBook(Player p) {
        if (!p.hasPermission("hover.display.book.disable")) {
            Format.use().player().noticePrefix(p, "&c你没有权限，无法禁用该图书");
            return true;
        }
        ItemStack mainHandItem = p.getInventory().getItemInMainHand();
        if (!mainHandItem.getType().equals(Material.WRITTEN_BOOK)) {
            Format.use().player().noticePrefix(p, "&c你没有手持一本可供禁用的已激活成书");
            return true;
        }
        if (mainHandItem.getItemMeta() == null) {
            Format.use().player().noticePrefix(p, "&c禁用过程中出现错误");
            return true;
        }
        BookMeta bookMeta = (BookMeta) mainHandItem.getItemMeta();
        List<String> bookLore = bookMeta.getLore();

        if (bookLore == null || bookLore.size() < 1 || !bookLore.get(bookLore.size() - 1).contains("右键方块放置激活的成书")) {
            Format.use().player().noticePrefix(p, "&c禁用过程中出现错误");
            return true;
        }
        bookLore.remove(bookLore.size() - 1);
        bookMeta.setLore(bookLore);
        mainHandItem.setItemMeta(bookMeta);
        Format.use().player().noticePrefix(p, "&7你手中的成书已被&c禁用");
        return true;
    }

    public boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&fBook &7创建展示图书指南");
        Format.use().player().noticeCommand(p, "display book help", "展示图书指南");
        Format.use().player().noticeCommand(p, "display book enable", "将手中的成书激活为可创建状态");
        Format.use().player().noticePermission(p, "lumos.display.book.enable");
        Format.use().player().noticeCommand(p, "display book disable", "禁用手中的处于激活状态的图书");
        Format.use().player().noticePermission(p, "lumos.display.book.disable");
        Format.use().player().noticeDefault(p, "右键放置激活的图书");
        Format.use().player().noticePermission(p, "lumos.display.book.place");
        return true;
    }

}

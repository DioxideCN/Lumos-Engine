package cn.dioxide.spigot.command;

import cn.dioxide.common.annotation.Executor;
import cn.dioxide.common.extension.Format;
import cn.dioxide.spigot.service.DisplayBlock;
import cn.dioxide.spigot.service.DisplayBook;
import cn.dioxide.spigot.service.DisplayItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Executor(name = "display")
public class MainCommand implements TabExecutor {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!label.equals("display")) {
            return false;
        }
        // display help
        if (args.length == 0) {
            return this.pluginHelper(player);
        }

        DisplayBook displayBook = new DisplayBook();
        DisplayItem displayItem = new DisplayItem();
        DisplayBlock displayBlock = new DisplayBlock();
        if (args.length == 1) {
            if ("help".equals(args[0])) {
                return this.pluginHelper(player);
            }
            if ("block".equals(args[0])) {
                return displayBlock.pluginHelper(player);
            }
            if ("item".equals(args[0])) {
                displayItem.pluginHelper(player);
            }
            if ("book".equals(args[0])) {
                return displayBook.pluginHelper(player);
            }
        }
        // display <module> <action>
        if (args.length == 2) {
            if ("book".equals(args[0])) {
                switch (args[1]) {
                    case "enable" -> { // display book enable
                        return displayBook.enableHoverBook(player);
                    }
                    case "disable" -> { // display book disable
                        return displayBook.disableHoverBook(player);
                    }
                    case "help" -> { // display book help
                        return displayBook.pluginHelper(player);
                    }
                    default -> { // display book ?
                        return false;
                    }
                }
            }
            if ("help".equals(args[1])) {
                if ("item".equals(args[0])) {
                    return displayItem.pluginHelper(player);
                }
                if ("block".equals(args[0])) {
                    return displayBlock.pluginHelper(player);
                }
            }
            if ("recycle".equals(args[1])) {
                if ("item".equals(args[0])) {
                    return displayItem.recycleItemDisplay(player);
                }
                if ("block".equals(args[0])) {
                    return displayBlock.recycleItemDisplay(player);
                }
            }
        }
        if (args.length >= 10 && args.length <= 12) {
            // display item <x y z> <rx ry rz> <sx sy sz> <type> <true/false>
            if ("item".equals(args[0])) {
                return displayItem.placeItemDisplay(
                        player,
                        args,
                        args.length == 11 ? args[10] : "fixed",
                        args.length == 12 && Boolean.parseBoolean(args[11]));
            }
            // display block <x y z> <rx ry rz> <s> <type> <true/false>
            if ("block".equals(args[0]) && args.length <= 11) {
                return displayBlock.placeItemDisplay(
                        player,
                        args,
                        args.length == 11 && Boolean.parseBoolean(args[10]));
            }
        }
        return false;
    }

    private boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&7插件指南 &f版本: &71.0.0");
        Format.use().player().noticeCommand(p, "display book help", "图书创建指南");
        Format.use().player().noticeCommand(p, "display item help", "物品展示指南");
        Format.use().player().noticeCommand(p, "display block help", "方块展示指南");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabHelper = Arrays.asList("help", "book", "item", "block");
        List<String> bookHelper = Arrays.asList("disable", "enable", "help");
        // display <?>
        if (args.length == 1) {
            return tabHelper;
        }
        if (args.length == 2) {
            // display book <?>
            if ("book".equals(args[0])) {
                return bookHelper;
            }
        }
        if (args.length >= 2) {
            if ("item".equals(args[0])) {
                return DisplayItem.tab(args, (Player) sender);
            }
            if ("block".equals(args[0])) {
                return DisplayBlock.tab(args, (Player) sender);
            }
        }
        return null;
    }
}

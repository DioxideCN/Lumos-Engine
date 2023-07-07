package cn.dioxide.spigot.command;

import cn.dioxide.common.annotation.Executor;
import cn.dioxide.common.extension.Format;
import cn.dioxide.spigot.custom.CustomItem;
import cn.dioxide.spigot.custom.CustomRegister;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/7/6
 * @since 1.0
 */
@Executor(name = "custom")
public class CustomCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return false;
        }
        if (!label.equals("custom")) {
            return false;
        }
        if (args.length == 0) {
            return pluginHelper(player);
        }

        if (args.length == 2) {
            if ("get".equals(args[0])) {
                if (CustomRegister.getKeySet().contains(args[1])) {
                    player.getInventory().addItem(CustomRegister.get(args[1]));
                } else {
                    Format.use().player().noticePrefix(player, "&c物品不存在");
                }
                return true;
            }
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabHelper = Arrays.asList("help", "get", "give");
        if (args.length == 1) {
            return tabHelper;
        }
        if (args.length == 2) {
            if ("get".equals(args[0])) {
                return CustomRegister.getKeySet();
            }
        }
        return null;
    }

    public static boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&7插件指南 &f版本: &71.0.0");
        Format.use().player().noticeCommand(p, "custom get <item>", "获取物品");
        return true;
    }

}

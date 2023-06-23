package cn.dioxide.spigot.command;

import cn.dioxide.common.annotation.Executor;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.infra.WhiteList;
import cn.dioxide.common.util.PlayerUtils;
import cn.dioxide.spigot.service.WhiteListHelper;
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
 * @date 2023/6/23
 * @since 1.0
 */
@Executor(name = "lumos")
public class LumosCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!label.equals("lumos")) {
            return false;
        }
        // player usage
        if (sender instanceof Player player) {
            if (args.length == 0) {
                return pluginHelper(player);
            }
            if (args.length == 1) {
                if ("display".equals(args[0])) {
                    return DisplayCommand.pluginHelper(player);
                }
                if ("whitelist".equals(args[0])) {
                    return WhiteListHelper.pluginHelper(player);
                }
            }
        }
        // lumos whitelist add|remove|enable|disable
        if ((args.length == 2 || args.length == 3) && "whitelist".equals(args[0])) {
            return WhiteListHelper.handleWhiteListCommand(sender, args);
        }
        return false;
    }

    public static boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&7插件指南 &f版本: &71.0.0");
        Format.use().player().noticeCommand(p, "lumos", "插件指南");
        Format.use().player().noticeCommand(p, "lumos display", "展示实体创建指南");
        Format.use().player().noticeCommand(p, "lumos whitelist", "白名单指南");
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> tabHelper = Arrays.asList("help", "display", "whitelist", "reload");
        List<String> wlHelper = Arrays.asList("enable", "disable", "add", "remove");
        if (args.length == 1) {
            return tabHelper;
        }
        if (args.length == 2 && "whitelist".equals(args[0])) {
            return wlHelper;
        }
        return null;
    }
}

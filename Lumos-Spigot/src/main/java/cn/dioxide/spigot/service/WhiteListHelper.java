package cn.dioxide.spigot.service;

import cn.dioxide.common.extension.Config;
import cn.dioxide.common.extension.Format;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dioxide.CN
 * @date 2023/6/23
 * @since 1.0
 */
public class WhiteListHelper {

    // 处理白名单
    public static boolean handleWhiteListCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 2) {
            if ("enable".equals(args[1])) {
                Config.get().feature.enableWhitelist = true;
                Config.get().set(0, "feature.enable-whitelist", true);
                if (sender instanceof Player p) {
                    Format.use().player().noticePrefix(p, "&7白名单已&a启用");
                } else {
                    Format.use().plugin().info("&7白名单已&a启用");
                }
                return true;
            }
            if ("disable".equals(args[1])) {
                Config.get().feature.enableWhitelist = false;
                Config.get().set(0, "feature.enable-whitelist", false);
                if (sender instanceof Player p) {
                    Format.use().player().noticePrefix(p, "&7白名单已&c禁用");
                } else {
                    Format.use().plugin().info("&7白名单已&c禁用");
                }
                return true;
            }
            return false;
        }
        // lumos whitelist add Dioxide_CN
        if (args.length == 3) {
            if ("add".equals(args[1])) {
                if (Config.get().whiteList.users.contains(args[2])) {
                    if (sender instanceof Player p) {
                        Format.use().player().noticePrefix(p, "&f" + args[2] + "&7已存在于白名单中");
                    } else {
                        Format.use().plugin().info("&f" + args[2] + "&7已存在于白名单中");
                    }
                    return true;
                }
                Config.get().whiteList.users.add(args[2]);
                Config.get().set(2, "users", Config.get().whiteList.users);
                if (sender instanceof Player p) {
                    Format.use().player().noticePrefix(p, "&f" + args[2] + "&7已添加至白名单");
                } else {
                    Format.use().plugin().info("&f" + args[2] + "&7已添加至白名单");
                }
                return true;
            }
            if ("remove".equals(args[1])) {
                if (!Config.get().whiteList.users.contains(args[2])) {
                    if (sender instanceof Player p) {
                        Format.use().player().noticePrefix(p, "&f" + args[2] + "&7不在白名单中");
                    } else {
                        Format.use().plugin().info("&f" + args[2] + "&7不在白名单中");
                    }
                    return true;
                }
                Config.get().whiteList.users.remove(args[2]);
                Config.get().set(2, "users", Config.get().whiteList.users);
                if (sender instanceof Player p) {
                    Format.use().player().noticePrefix(p, "&f" + args[2] + "&7已从白名单移除");
                } else {
                    Format.use().plugin().info("&f" + args[2] + "&7已从白名单移除");
                }
                return true;
            }
            return false;
        }
        return false;
    }

    public static boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&7插件指南 &f版本: &71.0.0");
        Format.use().player().noticeCommand(p, "lumos whitelist enable", "启用白名单");
        Format.use().player().noticeCommand(p, "lumos whitelist disable", "禁用白名单");
        Format.use().player().noticeCommand(p, "lumos whitelist add <name>", "添加玩家白名单");
        Format.use().player().noticeCommand(p, "lumos whitelist remove <name>", "移除玩家白名单");
        return true;
    }

}

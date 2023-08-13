package cn.dioxide.spigot.command;

import cn.dioxide.common.annotation.Executor;
import cn.dioxide.common.extension.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/7/27
 * @since 1.0
 */
@Executor(name = "snake")
public class SnakeCommand implements TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!label.equals("snake")) {
            return false;
        }
        if (args.length == 2) {
            if ("run".equals(args[0])) {
                Config.get().snakeConfigs.get(args[1]).move();
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> help = List.of("run");
        if (args.length == 1) {
            return help;
        }
        return Config.get().snakeConfigs.keySet().stream().toList();
    }

}

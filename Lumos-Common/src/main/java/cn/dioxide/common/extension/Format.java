package cn.dioxide.common.extension;

import cn.dioxide.common.exception.FormatNotInitException;
import cn.dioxide.common.util.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class Format {
    private volatile static Format instance;
    private volatile static PlayerMessageType playerMessageType;
    private volatile static PluginMessageType pluginMessageType;

    private static final Object lock = new Object();

    private final Logger logger;
    private final String prefix;

    public PlayerMessageType player() {
        return playerMessageType;
    }

    public PluginMessageType plugin() {
        return pluginMessageType;
    }

    public static Format use() {
        if (instance.logger == null) {
            throw new FormatNotInitException();
        }
        return instance;
    }

    private Format(JavaPlugin plugin, String prefix) {
        this.logger = plugin.getLogger();
        this.prefix = prefix;
    }

    /**
     * 初始化Logger
     * @param plugin AbstractPluginStarter
     */
    public static void init(JavaPlugin plugin, String prefix) {
        initInstance(plugin, prefix);
        initServerMessageType();
        initPlayerMessageType();
    }

    private static void initInstance(JavaPlugin plugin, String prefix) {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new Format(plugin, prefix);
                }
            }
        }
    }

    private static void initPlayerMessageType() {
        if (playerMessageType == null) {
            synchronized (lock) {
                if (playerMessageType == null) {
                    playerMessageType = new PlayerMessageType();
                }
            }
        }
    }

    private static void initServerMessageType() {
        if (pluginMessageType == null) {
            synchronized (lock) {
                if (pluginMessageType == null) {
                    pluginMessageType = new PluginMessageType();
                }
            }
        }
    }

    public static class PlayerMessageType {
        public void notice(@NotNull Player player, @NotNull String message) {
            player.sendMessage(ColorUtils.replace(message));
        }

        public void noticePrefix(@NotNull Player player, @NotNull String message) {
            player.sendMessage(ColorUtils.replace(instance.prefix + " " + message));
        }

        public void noticeDefault(@NotNull Player player, @NotNull String description) {
            player.sendMessage(ColorUtils.replace("&3> &7" + description));
        }

        public void noticeCommand(@NotNull Player player, @NotNull String command, @NotNull String description) {
            player.sendMessage(ColorUtils.replace("&3> &a/" + command + " &8- &7" + description));
        }

        public void noticePermission(@NotNull Player player, @NotNull String permission) {
            player.sendMessage(ColorUtils.replace("&e  - &7" + permission));
        }

        private PlayerMessageType() {}
    }

    public static class PluginMessageType {
        public void info(@NotNull String message) {
            instance.logger.info(ColorUtils.replace(message));
        }

        public void finder(@NotNull String key, @NotNull Object value) {
            instance.logger.info(ColorUtils.replace("&3> &b" + key + "&8: &7" + value));
        }

        public void warn(@NotNull String message) {
            instance.logger.log(Level.WARNING, ColorUtils.replace(message));
        }

        public void server(@NotNull String message) {
            instance.logger.log(Level.SEVERE, ColorUtils.replace(message));
        }

        private PluginMessageType() {}
    }
}

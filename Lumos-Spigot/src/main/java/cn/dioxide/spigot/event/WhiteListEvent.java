package cn.dioxide.spigot.event;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.util.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

/**
 * @author Dioxide.CN
 * @date 2023/6/23
 * @since 1.0
 */
@Event(configKey = "feature.enable-whitelist")
public class WhiteListEvent implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoin(PlayerLoginEvent e) {
        Player player = e.getPlayer();
        // 不拦截OP
        if (player.isOp()) {
            e.allow();
            return;
        }
        // 其它的在白名单中拦截
        if (Config.get().whiteList.users.contains(player.getName())) {
            e.allow();
        } else {
            e.disallow(
                    Result.KICK_WHITELIST,
                    ColorUtils.replace(Config.get().whiteList.kickMessage));
        }
    }
}

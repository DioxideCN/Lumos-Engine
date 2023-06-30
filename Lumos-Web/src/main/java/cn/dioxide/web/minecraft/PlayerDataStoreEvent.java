package cn.dioxide.web.minecraft;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.ApplicationConfig;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
@Event
public class PlayerDataStoreEvent implements Listener {

    PlayerMapper playerMapper = MapperConfig.use().getInstance(PlayerMapper.class);

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if (ApplicationConfig.use().enable) {
            Player player = e.getPlayer();
            // 从数据库中获取
            System.out.println(player.getName());
            StaticPlayer dbPlayer = playerMapper.select(player.getName());
            System.out.println(dbPlayer);
            if (dbPlayer == null) {
                // 是新数据 存入
                dbPlayer = StaticPlayer.convert(player, false);
                playerMapper.insert(dbPlayer);
            } else {
                // 不是新数据 更新
                dbPlayer = StaticPlayer.convert(player, false);
                playerMapper.update(dbPlayer);
            }
            MapperConfig.use().commit();
        }
    }

}

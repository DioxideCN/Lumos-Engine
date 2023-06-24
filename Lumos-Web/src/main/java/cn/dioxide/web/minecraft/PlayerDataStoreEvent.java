package cn.dioxide.web.minecraft;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
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
    public void onJoin(PlayerQuitEvent e) {
        System.out.println("玩家离开游戏");
        // 存入或更新到数据库
        StaticPlayer staticPlayer = StaticPlayer.convert(e.getPlayer(), false);
        System.out.println(playerMapper.select(staticPlayer.getName()));
        if (playerMapper.select(staticPlayer.getName()) == null) {
            playerMapper.insert(staticPlayer);
        } else {
            playerMapper.update(staticPlayer);
        }
        MapperConfig.use().commit();
    }

}

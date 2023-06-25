package cn.dioxide.web.minecraft;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.web.config.ItemStackSerializer;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
import com.alibaba.fastjson2.JSON;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

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
        Player player = e.getPlayer();
        // Get the player's inventory data
        List<ItemStackSerializer> inventory = ItemStackSerializer.convert(player.getInventory().getContents());
        List<ItemStackSerializer> equipment = ItemStackSerializer.convert(player.getInventory().getArmorContents());

        // Serialize the inventory data
        String inventoryJson = JSON.toJSONString(inventory);
        String equipmentJson = JSON.toJSONString(equipment);

        // 从数据库中获取
        StaticPlayer awaiter = playerMapper.select(player.getName());
        if (awaiter == null) {
            // 存入
            awaiter = StaticPlayer.convert(player, false);
            playerMapper.insert(awaiter);
        } else {
            // 更新
            awaiter.setInv(inventoryJson);
            awaiter.setEquip(equipmentJson);
            playerMapper.update(awaiter);
        }
        MapperConfig.use().commit();
    }

}

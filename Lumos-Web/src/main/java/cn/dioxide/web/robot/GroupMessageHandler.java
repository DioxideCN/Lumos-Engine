package cn.dioxide.web.robot;

import cn.dioxide.common.annotation.Event;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.extension.Format;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.controller.ServerApiService;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
import me.dreamvoid.miraimc.api.MiraiBot;
import me.dreamvoid.miraimc.api.bot.MiraiGroup;
import net.minecraft.nbt.CompoundTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.dreamvoid.miraimc.bukkit.event.message.passive.MiraiGroupMessageEvent;

import java.util.*;

/**
 * @author Dioxide.CN
 * @date 2023/6/30
 * @since 1.0
 */
@Event
public class GroupMessageHandler implements Listener {

    PlayerMapper playerMapper = MapperConfig.use().getInstance(PlayerMapper.class);

    @EventHandler
    public void getServerDetail(MiraiGroupMessageEvent e) {
        for (Long group : Config.get().robot.group) {
            if (group.equals(e.getGroupID())) {
                MiraiGroup useGroup = MiraiBot.getBot(e.getBotID()).getGroup(e.getGroupID());
                String message = e.getMessage();
                if (!message.startsWith("#")) return;
                message = message.substring(1);
                if ("在线人数".equals(message)) {
                    getServerInfo(useGroup);
                    return;
                }

                if (message.length() > 1) {
                    if (message.startsWith("白名单")) {
                        addToWhiteList(useGroup, e.getSenderID(), message);
                        return;
                    }
                    if ("我的信息".equals(message)) {
                        getPrivateInfo(useGroup, e.getSenderID(), e.getSenderName());
                        return;
                    }
                }
            }
        }
    }

    // 查看个人信息
    private void getPrivateInfo(MiraiGroup useGroup, long operator, String operatorName) {
        StaticPlayer dbPlayer = playerMapper.selectByQQ(String.valueOf(operator));
        StringBuilder stringBuilder = new StringBuilder();
        List<String> messages = new LinkedList<>();
        if (dbPlayer == null) {
            useGroup.sendMessage("请先在服务器中使用/lumos bind <qq>绑定你的QQ");
            return;
        }
        Player player = Bukkit.getPlayer(dbPlayer.getName());
        messages.add("@" + operatorName + " 的信息：");
        if (player == null) {
            // 玩家离线
            messages.add("玩家名：[离线]");
            return;
        } else {
            StaticPlayer convertor = StaticPlayer.convert(player, true);



            messages.add("玩家名：[在线]" + player.getName());
            messages.add("等级：Lv." + player.getLevel());
            messages.add("位置：世界 " + player.getLocation().getWorld() +
                    " X " + String.format("%.2f", player.getLocation().getX()) +
                    " Y " + String.format("%.2f", player.getLocation().getY()) +
                    " Z " + String.format("%.2f", player.getLocation().getZ()));
            assert convertor.getInventory() != null;
            for (CompoundTag compoundTag : convertor.getInventory()) {
                if (compoundTag.toString().contains("Count:0b")) continue;
                messages.add(compoundTag.toString());
            }
        }
        for (String message : messages) {
            if (messages.get(messages.size() - 1).equalsIgnoreCase(message)) {
                stringBuilder.append(message.replaceAll("§\\S", ""));
            } else {
                stringBuilder.append(message.replaceAll("§\\S", "")).append("\n");
            }
        }
        useGroup.sendMessage(stringBuilder.toString());
    }

    // 获取在线人数
    private void getServerInfo(MiraiGroup useGroup) {
        List<String> messages = new LinkedList<>();
        StringJoiner joiner = new StringJoiner("，");
        StringBuilder stringBuilder = new StringBuilder();
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            count++;
            joiner.add(player.getName());
        }

        float[] serverTPS = ServerApiService.getServerTPS();
        messages.add("在线人数：" + count + "/" + (count + 1));
        messages.add("在线列表：" + joiner);
        messages.add("服务器TPS：" + String.format("%.2f", serverTPS[0]) + "(1min) " +
                String.format("%.2f", serverTPS[1]) + "(5min) " +
                String.format("%.2f", serverTPS[2]) + "(15min)");
        for (String message : messages) {
            if (messages.get(messages.size() - 1).equalsIgnoreCase(message)) {
                stringBuilder.append(message.replaceAll("§\\S", ""));
            } else {
                stringBuilder.append(message.replaceAll("§\\S", "")).append("\n");
            }
        }
        useGroup.sendMessage(stringBuilder.toString());
    }

    // 添加白名单
    private void addToWhiteList(MiraiGroup useGroup, long operator, String message) {
        for (Long owner : Config.get().robot.owner) {
            // 是操作员
            if (owner.equals(operator)) {
                String regex = "^[A-Za-z0-9_]{3,24}$";
                String[] split = message.split(" ");
                if (split.length != 3) return;
                String action = split[1];
                String playerName = split[2];
                if (!playerName.matches(regex)) {
                    useGroup.sendMessage(playerName + "无效");
                    return;
                }
                if ("添加".equals(action)) {
                    if (Config.get().whiteList.users.contains(playerName)) {
                        useGroup.sendMessage(playerName + "已经在白名单中了");
                        return;
                    }
                    Config.get().whiteList.users.add(playerName);
                    Config.get().set(2, "users", Config.get().whiteList.users);
                    useGroup.sendMessage(playerName + "已添加至白名单");
                    return;
                } else if ("移除".equals(action)) {
                    if (!Config.get().whiteList.users.contains(playerName)) {
                        useGroup.sendMessage(playerName + "不在白名单中");
                        return;
                    }
                    Config.get().whiteList.users.remove(playerName);
                    Config.get().set(2, "users", Config.get().whiteList.users);
                    useGroup.sendMessage(playerName + "已从白名单移除");
                    return;
                } else {
                    useGroup.sendMessage("该操作缺少参数");
                    return;
                }
            }
        }
    }

}

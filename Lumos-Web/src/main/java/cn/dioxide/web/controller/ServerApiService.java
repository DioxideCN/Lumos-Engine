package cn.dioxide.web.controller;

import cn.dioxide.web.annotation.ServletMapping;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import me.clip.placeholderapi.PlaceholderAPI;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dioxide.CN
 * @date 2023/6/25
 * @since 1.0
 */
@ServletMapping("/api/server")
public class ServerApiService extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        resp.setContentType("application/json");
        // 发送响应
        resp.getWriter().write(objectMapper.writeValueAsString(getServerInfo()));
    }

    public ServerInfo getServerInfo() {
        int onlinePlayersCount = Bukkit.getOnlinePlayers().size(); // 获取在线玩家数量
        List<String> onlinePlayersList = new ArrayList<>(); // 获取在线玩家列表

        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayersList.add(player.getName());
        }
        return new ServerInfo(getServerTPS(), onlinePlayersCount, onlinePlayersList);
    }

    // NMS来获取TPS
    public static float[] getServerTPS() {
        float tps1 = new BigDecimal(
                PlaceholderAPI
                        .setPlaceholders(null, "%server_tps_1%")
                        .replace("*", ""))
                .floatValue();
        float tps5 = new BigDecimal(
                PlaceholderAPI
                        .setPlaceholders(null, "%server_tps_5%")
                        .replace("*", ""))
                .floatValue();
        float tps15 = new BigDecimal(
                PlaceholderAPI
                        .setPlaceholders(null, "%server_tps_15%")
                        .replace("*", ""))
                .floatValue();
        // 过去 1 分钟、5 分钟和 15 分钟的平均 TPS。
        return new float[]{tps1, tps5, tps15};
    }

    public record ServerInfo(float[] tps,
                             int onlinePlayersCount,
                             List<String> onlinePlayersList) {
    }

}

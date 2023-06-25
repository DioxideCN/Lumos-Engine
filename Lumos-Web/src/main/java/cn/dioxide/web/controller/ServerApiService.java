package cn.dioxide.web.controller;

import cn.dioxide.web.annotation.ServletMapping;
import com.alibaba.fastjson2.JSON;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R1.CraftServer;
import org.bukkit.entity.Player;

import java.io.IOException;
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
        resp.setContentType("application/json");
        // 发送响应
        resp.getWriter().write(JSON.toJSONString(getServerInfo()));
    }

    public ServerInfo getServerInfo() {
        double[] tps = getServerTPS(); // 获取服务器的TPS
        int onlinePlayersCount = Bukkit.getOnlinePlayers().size(); // 获取在线玩家数量
        List<String> onlinePlayersList = new ArrayList<>(); // 获取在线玩家列表

        for (Player player : Bukkit.getOnlinePlayers()) {
            onlinePlayersList.add(player.getName());
        }

        return new ServerInfo(tps, onlinePlayersCount, onlinePlayersList);
    }

    // NMS来获取TPS
    private double[] getServerTPS() {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        MinecraftServer minecraftServer = craftServer.getServer();
        return minecraftServer.recentTps;
    }

    public record ServerInfo(double[] tps,
                             int onlinePlayersCount,
                             List<String> onlinePlayersList) {
    }

}

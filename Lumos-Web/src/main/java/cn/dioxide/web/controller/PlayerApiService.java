package cn.dioxide.web.controller;

import cn.dioxide.web.annotation.ServletMapping;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
@ServletMapping("/api/player/*")
public class PlayerApiService extends HttpServlet {
    PlayerMapper playerMapper = MapperConfig.use().getInstance(PlayerMapper.class);
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 设置响应内容类型为 JSON
        resp.setContentType("application/json");
        // 获取请求路径信息
        String pathInfo = req.getPathInfo();
        // 检查路径信息是否存在
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpStatus.BAD_REQUEST_400);
            resp.getWriter().write("{\"error\": \"Player name must be provided in the URL\"}");
            return;
        }
        // 从路径信息中获取玩家名称
        String playerName = pathInfo.substring(1);
        // 尝试获取在线玩家
        Player player = Bukkit.getPlayer(playerName);
        // 检查玩家是否在线
        if (player != null) {
            // 发送响应
            StaticPlayer onlinePlayer = StaticPlayer.convert(player, true);
            resp.getWriter().write(onlinePlayer.toJSONString());
        } else {
            // 尝试获取离线玩家
            StaticPlayer offlinePlayer = playerMapper.select(playerName);
            // 检查离线玩家是否存在
            if (offlinePlayer != null) {
                // 发送响应
                resp.getWriter().write(offlinePlayer.toJSONString());
            } else {
                resp.setStatus(HttpStatus.NOT_FOUND_404);
                resp.getWriter().write("{\"error\": \"Player not found\"}");
            }
        }
    }
}

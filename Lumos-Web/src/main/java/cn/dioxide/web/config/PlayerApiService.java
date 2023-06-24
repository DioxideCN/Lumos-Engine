package cn.dioxide.web.config;

import cn.dioxide.web.annotation.ServletMapping;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.eclipse.jetty.http.HttpStatus;

import java.io.IOException;
import java.util.UUID;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
@ServletMapping("/api/player/*")
public class PlayerApiService extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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

        // 使用 Bukkit API 获取玩家
        Player player = Bukkit.getPlayer(playerName);

        // 检查玩家是否存在
        if (player == null) {
            resp.setStatus(HttpStatus.NOT_FOUND_404);
            resp.getWriter().write("{\"error\": \"Player not found\"}");
            return;
        }

        // 获取玩家的 UUID
        UUID playerUUID = player.getUniqueId();

        // 创建 JSON 响应
        String jsonResponse = String.format("{\"playerName\": \"%s\", \"uuid\": \"%s\"}", playerName, playerUUID.toString());

        // 设置响应内容类型为 JSON
        resp.setContentType("application/json");

        // 发送响应
        resp.getWriter().write(jsonResponse);
    }
}

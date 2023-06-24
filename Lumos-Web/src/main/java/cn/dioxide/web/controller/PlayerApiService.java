package cn.dioxide.web.controller;

import cn.dioxide.web.annotation.ServletMapping;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
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

    PlayerMapper playerMapper = MapperConfig.use().getInstance(PlayerMapper.class);

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
        // 尝试获取在线玩家
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        // 检查玩家是否在线
        if (onlinePlayer != null) {
            // 设置响应内容类型为 JSON
            resp.setContentType("application/json");
            // 发送响应
            resp.getWriter().write(new Gson().toJson(StaticPlayer.convert(onlinePlayer, true)));
        } else {
            // 尝试获取离线玩家
            StaticPlayer staticPlayer = playerMapper.select(playerName);
            // 检查离线玩家是否存在
            if (staticPlayer != null) {
                // 设置响应内容类型为 JSON
                resp.setContentType("application/json");
                // 发送响应
                resp.getWriter().write(new Gson().toJson(staticPlayer));
            } else {
                resp.setStatus(HttpStatus.NOT_FOUND_404);
                resp.getWriter().write("{\"error\": \"Player not found\"}");
            }
        }
    }

}

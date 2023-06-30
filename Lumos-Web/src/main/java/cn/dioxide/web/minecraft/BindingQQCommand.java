package cn.dioxide.web.minecraft;

import cn.dioxide.common.extension.Format;
import cn.dioxide.web.config.MapperConfig;
import cn.dioxide.web.entity.StaticPlayer;
import cn.dioxide.web.mapper.PlayerMapper;
import org.bukkit.entity.Player;



/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
public class BindingQQCommand {

    static final PlayerMapper playerMapper = MapperConfig.use().getInstance(PlayerMapper.class);

    public static boolean bindingQQ(Player player, String qq) {
        // 验证QQ号码格式
        String regex = "[1-9][0-9]{4,10}";
        if (!qq.matches(regex)) {
            Format.use().player().noticePrefix(player, "&c你的QQ账号不符合规范");
            return false;
        }
        // 检查QQ是否已绑定
        StaticPlayer qPlayer = playerMapper.selectByQQ(qq);
        if (qPlayer != null) {
            Format.use().player().noticePrefix(player, "&c该QQ账号已被 " + qPlayer.getName() + " 绑定");
            return true;
        }
        // 检查玩家是否已存在于数据库
        StaticPlayer staticPlayer = playerMapper.select(player.getName());
        if (staticPlayer == null) {
            // 数据库中不存在，需要创建
            staticPlayer = StaticPlayer.convert(player, true);
            staticPlayer.setQq(qq);
            playerMapper.insert(staticPlayer);
        } else {
            // 数据库中已存在，需要更新
            if (staticPlayer.getQq() == null || staticPlayer.getQq().isEmpty()) {
                staticPlayer.setQq(qq);
                playerMapper.update(staticPlayer);
            } else {
                // TODO 后面会作为可配置项
                Format.use().player().noticePrefix(player, "&c你只能绑定一次账号，如需修改请联系管理员");
                return true;
            }
        }
        // 提交更改
        MapperConfig.use().commit();
        // 通知玩家绑定成功
        Format.use().player().noticePrefix(player, "&a绑定成功");
        return true;
    }


    public static boolean pluginHelper(Player p) {
        Format.use().player().noticePrefix(p, "&7插件指南 &f版本: &71.0.0");
        Format.use().player().noticeCommand(p, "lumos bind <QQ>", "绑定QQ账号");
        return true;
    }

}

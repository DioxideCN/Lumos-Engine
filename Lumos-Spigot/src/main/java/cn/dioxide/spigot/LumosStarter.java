package cn.dioxide.spigot;

import cn.dioxide.common.extension.BeanHolder;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.extension.Config;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
public class LumosStarter extends JavaPlugin {

    public static LumosStarter INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Format.init(this, "&7[&3&lLumos&b&lEngine&7]");
        Config.init(this, true);
        BeanHolder.init(this);
    }

    @Override
    public void onDisable() {
        Format.use().plugin().info("&cPlugin has been disabled");
        super.onDisable();
    }

}

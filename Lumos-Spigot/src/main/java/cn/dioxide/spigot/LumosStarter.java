package cn.dioxide.spigot;

import cn.dioxide.common.annotation.ScanPackage;
import cn.dioxide.common.extension.ApplicationConfig;
import cn.dioxide.common.extension.BeanHolder;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.extension.Format;
import cn.dioxide.web.infra.LocalWebEngine;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
@ScanPackage({"cn.dioxide.web"})
public class LumosStarter extends JavaPlugin {

    public static LumosStarter INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Format.init(this, "&7[&3&lLumos&b&lEngine&7]");
        Config.init(this, true);
        BeanHolder.init(this);
        if (ApplicationConfig.use().enable) {
            LocalWebEngine.init(this);
        }
    }

    @Override
    public void onDisable() {
        LocalWebEngine.stop();
        Format.use().plugin().info("&cPlugin has been disabled");
        super.onDisable();
    }

}

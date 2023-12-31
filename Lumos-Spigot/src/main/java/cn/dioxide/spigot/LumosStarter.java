package cn.dioxide.spigot;

import cn.dioxide.common.annotation.ScanPackage;
import cn.dioxide.common.extension.ApplicationConfig;
import cn.dioxide.common.extension.BeanHolder;
import cn.dioxide.common.extension.Config;
import cn.dioxide.common.extension.Format;
import cn.dioxide.spigot.custom.CustomRegister;
import cn.dioxide.web.infra.LocalWebEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Dioxide.CN
 * @date 2023/6/21
 * @since 1.0
 */
@ScanPackage({"cn.dioxide.web"})
public class LumosStarter extends JavaPlugin {

    public static LumosStarter INSTANCE;
    public static Plugin PAPI_INSTANCE;

    @Override
    public void onEnable() {
        INSTANCE = this;
        Format.init(this, "&7[&3&lLumos&b&lEngine&7]"); // 初始化Formatter工具
        Config.init(this, true); // 加载配置类
        BeanHolder.init(this); // 初始化Bean容器
        if (ApplicationConfig.use().enable) {
            LocalWebEngine.init(this); // 初始化Jetty容器
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            // PlaceholderAPI 不可用
            Bukkit.getPluginManager().disablePlugin(this);
        } else {
            PAPI_INSTANCE = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        }

        CustomRegister.init();
    }

    @Override
    public void onDisable() {
        LocalWebEngine.stop();
        Format.use().plugin().info("&cPlugin has been disabled");
        super.onDisable();
    }

}

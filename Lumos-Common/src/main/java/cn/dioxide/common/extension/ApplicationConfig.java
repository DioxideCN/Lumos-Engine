package cn.dioxide.common.extension;

import org.bukkit.configuration.file.YamlConfiguration;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
public class ApplicationConfig {

    public boolean enable; // 是否启用web容器
    public int port; // web容器端口

    // application -> application.yml
    public void init(YamlConfiguration application) {
        if (application == null) {
            return;
        }

        this.enable = application.getBoolean("server.enable", false);
        this.port = application.getInt("server.port", 8090);
    }

    protected volatile static ApplicationConfig INSTANCE = null;
    protected ApplicationConfig() {}
    public static ApplicationConfig use() {
        if (INSTANCE == null) {
            synchronized (ApplicationConfig.class) {
                if (INSTANCE == null) INSTANCE = new ApplicationConfig();
            }
        }
        return INSTANCE;
    }

}

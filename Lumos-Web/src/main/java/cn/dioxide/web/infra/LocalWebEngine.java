package cn.dioxide.web.infra;

import cn.dioxide.common.extension.ApplicationConfig;
import cn.dioxide.common.extension.Format;
import cn.dioxide.common.extension.ReflectFactory;
import cn.dioxide.web.annotation.ServletMapping;
import jakarta.servlet.http.HttpServlet;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dioxide.CN
 * @date 2023/6/24
 * @since 1.0
 */
public class LocalWebEngine {

    @Getter
    private static LocalWebEngine instance;
    @Getter
    private Server server;
    @Getter
    private JavaPlugin plugin;

    public static void init(@NotNull JavaPlugin plugin) {
        Format.use().plugin().info("Starting jetty server...");
        instance = new LocalWebEngine();
        if (ApplicationConfig.use().enable) {
            instance.start(plugin);
        }
    }

    private void start(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.server = new Server();
        // try-with-resource
        try (ServerConnector connector = new ServerConnector(this.server)) {
            connector.setPort(8090);
            this.server.setConnectors(new Connector[]{connector});
        }

        // 创建并配置ServletContextHandler
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        handleServletBean(context);
        // 将ServletContextHandler设置为服务器的handler
        this.server.setHandler(context);

        try {
            this.server.start();
        } catch (Exception e) {
            e.printStackTrace();
            Format.use().plugin().server("&cFailed to start local server!");
        }
    }

    private void handleServletBean(final ServletContextHandler context) {
        // 包扫描自动注入servlet
        for (Class<?> clazz : ReflectFactory.use().getClassSet()) {
            if (!clazz.getName().contains("cn.dioxide.web")) {
                continue;
            }
            if (HttpServlet.class.isAssignableFrom(clazz)) {
                ServletMapping mapping = clazz.getAnnotation(ServletMapping.class);
                if (mapping != null) {
                    try {
                        HttpServlet servletInstance = (HttpServlet) clazz.getDeclaredConstructor().newInstance();
                        context.addServlet(new ServletHolder(servletInstance), mapping.value());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Stop the integrated web server.
     */
    public static void stop() {
        try {
            instance.server.stop();
        } catch (Exception e) {
            e.printStackTrace();
            Format.use().plugin().server("&cFailed to stop local server!");
        }
    }

}
